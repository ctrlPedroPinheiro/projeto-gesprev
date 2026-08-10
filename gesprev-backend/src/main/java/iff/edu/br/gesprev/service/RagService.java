package iff.edu.br.gesprev.service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import iff.edu.br.gesprev.exception.ChatIndisponivelException;
import iff.edu.br.gesprev.service.ai.AiGateway;
import iff.edu.br.gesprev.service.ai.GesprevConsultaTools;
import iff.edu.br.gesprev.service.ai.LegislacaoTools;

@Service
public class RagService {

    private static final Pattern NUMERO_PROCESSO = Pattern.compile("\\b(\\d{4,10})\\b");

    private static final String INSTRUCAO = """
            Voce e o assistente do GESPREV, especializado em aposentadoria de servidores municipais.
            Responda em portugues, de forma clara, objetiva e sem inventar informacoes.
            Para perguntas sobre um processo, use os dados GESPREV consultados e informe o numero consultado.
            Para perguntas sobre conformidade com emenda constitucional, confira sexo, idade, CTS/tempo,
            natureza, tipo de calculo, memoria, documentos, ato e o contexto legislativo recuperado.
            Separe a resposta em: conclusao preliminar, fundamentos, pendencias/alertas e proximos passos.
            Nao declare conformidade definitiva se faltar documento validado, CTS, memoria, ato ou base legal suficiente.
            Para totais do sistema, use os dados estatisticos consultados.
            Para regras e fundamentos legais, use o contexto legislativo recuperado e cite a fonte retornada.
            As ferramentas sao somente leitura. Nunca afirme que alterou, validou ou excluiu dados.
            Se uma informacao nao retornar, diga claramente que ela nao foi encontrada.
            """;

    private final AiGateway aiGateway;
    private final GesprevConsultaTools gesprevConsultaTools;
    private final LegislacaoTools legislacaoTools;
    private final ObjectMapper objectMapper;

    public RagService(
            AiGateway aiGateway,
            GesprevConsultaTools gesprevConsultaTools,
            LegislacaoTools legislacaoTools,
            ObjectMapper objectMapper) {
        this.aiGateway = aiGateway;
        this.gesprevConsultaTools = gesprevConsultaTools;
        this.legislacaoTools = legislacaoTools;
        this.objectMapper = objectMapper;
    }

    public String responder(String pergunta) {
        return responder(pergunta, null);
    }

    public String responder(String pergunta, Integer numeroProcesso) {
        if (pergunta == null || pergunta.isBlank()) {
            throw new IllegalArgumentException("A pergunta e obrigatoria");
        }

        String perguntaContextualizada = numeroProcesso == null
                ? pergunta
                : "Processo " + numeroProcesso + ". " + pergunta;
        String contextoLegislativo = legislacaoTools.buscarLegislacao(perguntaContextualizada);
        String instrucaoComContexto = INSTRUCAO + """

                Contexto recuperado da base legislativa para a pergunta atual:
                ---
                %s
                ---
                Use esse contexto somente quando for pertinente. Cite exatamente o nome da fonte informado.
                Se o contexto nao responder a pergunta, diga que a informacao nao foi encontrada.
                """.formatted(contextoLegislativo);

        String contextoGesprev = consultarContextoGesprev(pergunta, numeroProcesso);
        if (!contextoGesprev.isBlank()) {
            instrucaoComContexto += """

                    Dados atuais consultados na API GESPREV para esta pergunta:
                    ---
                    %s
                    ---
                    Esses dados sao somente leitura. Use-os como fonte principal para responder sobre o sistema.
                    """.formatted(contextoGesprev);
        }

        try {
            return aiGateway.responder(instrucaoComContexto, perguntaContextualizada);
        } catch (Exception e) {
            throw new ChatIndisponivelException(
                    "O assistente de IA está indisponível no momento. Verifique a conexão com o servidor do modelo/RAG e tente novamente.",
                    e);
        }
    }

    private String consultarContextoGesprev(String pergunta, Integer numeroProcessoInformado) {
        String texto = pergunta.toLowerCase(Locale.ROOT);
        try {
            if (numeroProcessoInformado != null) {
                return objectMapper.writeValueAsString(gesprevConsultaTools.consultarProcesso(numeroProcessoInformado));
            }
            if (texto.contains("processo")) {
                Matcher matcher = NUMERO_PROCESSO.matcher(pergunta);
                if (matcher.find()) {
                    int numero = Integer.parseInt(matcher.group(1));
                    return objectMapper.writeValueAsString(gesprevConsultaTools.consultarProcesso(numero));
                }
            }
            if (texto.contains("estatistica") || texto.contains("estatística")
                    || texto.contains("quantos processos") || texto.contains("total de processos")) {
                return objectMapper.writeValueAsString(gesprevConsultaTools.consultarEstatisticas());
            }
        } catch (Exception e) {
            return "Consulta GESPREV nao encontrou os dados solicitados: " + e.getMessage();
        }
        return "";
    }
}
