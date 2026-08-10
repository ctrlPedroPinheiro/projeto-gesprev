package iff.edu.br.gesprev.service;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import iff.edu.br.gesprev.dto.FichaFuncionalDTO;
import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import iff.edu.br.gesprev.repository.DocumentoRepository;
import iff.edu.br.gesprev.service.ai.AiGateway;

@Service
public class VLMService {

    private final DocumentoRepository documentoRepository;
    private final ObjectMapper objectMapper;
    private final AiGateway aiGateway;

    public VLMService(DocumentoRepository documentoRepository, ObjectMapper objectMapper, AiGateway aiGateway) {
        this.documentoRepository = documentoRepository;
        this.objectMapper = objectMapper;
        this.aiGateway = aiGateway;
    }

    public FichaFuncionalDTO processarFichaFuncional(Long documentoId) {
        Documento documento = buscarDocumento(documentoId);
        String json = processar(documento, TipoDocumento.FICHA_FUNCIONAL);

        try {
            return objectMapper.readValue(json, FichaFuncionalDTO.class);
        } catch (Exception e) {
            marcarErro(documento);
            throw new RuntimeException("Dados extraidos da ficha funcional sao invalidos: " + e.getMessage(), e);
        }
    }

    public String processarDocumento(Long documentoId) {
        Documento documento = buscarDocumento(documentoId);
        return processar(documento, documento.getTipoDocumento());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> preprocessarFichaFuncionalAbertura(Path arquivo) {
        try {
            String resposta = aiGateway.extrairDocumento(
                    arquivo,
                    TipoDocumento.FICHA_FUNCIONAL,
                    buildPromptAberturaFichaFuncional());
            String json = normalizarJson(resposta);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao preprocessar ficha funcional para abertura do processo: " + e.getMessage(), e);
        }
    }

    private String processar(Documento documento, TipoDocumento tipoDocumento) {
        try {
            String resposta = aiGateway.extrairDocumento(
                    Path.of(documento.getCaminhoArquivo()),
                    tipoDocumento,
                    buildPrompt(tipoDocumento));
            String json = normalizarJson(resposta);

            documento.setJsonExtraido(json);
            documento.setStatusVLM(StatusVLM.PROCESSADO);
            documentoRepository.save(documento);
            return json;
        } catch (Exception e) {
            marcarErro(documento);
            throw new RuntimeException("Erro ao processar documento com VLM: " + e.getMessage(), e);
        }
    }

    private Documento buscarDocumento(Long documentoId) {
        return documentoRepository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento nao encontrado"));
    }

    private void marcarErro(Documento documento) {
        documento.setStatusVLM(StatusVLM.ERRO);
        documentoRepository.save(documento);
    }

    private String normalizarJson(String resposta) throws Exception {
        if (resposta == null || resposta.isBlank()) {
            throw new IllegalArgumentException("O VLM retornou uma resposta vazia");
        }

        String json = resposta.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        return objectMapper.writeValueAsString(objectMapper.readTree(json));
    }

    private String buildPrompt(TipoDocumento tipoDocumento) {
        return switch (tipoDocumento) {
            case FICHA_FUNCIONAL -> """
                    Extraia os dados e retorne apenas JSON valido neste formato:
                    {"matricula":"","nome":"","dtNascimento":"yyyy-MM-dd","cpf":"","pis":"","sexo":"MASCULINO|FEMININO","email":"",
                    "cargo":"","orgao":"","dtAdmissao":"yyyy-MM-dd"}
                    Para PIS/PASEP/NIT, retorne no campo pis se estiver presente.
                    Para sexo, use MASCULINO ou FEMININO. Se nao estiver explicito, use null.
                    Use null quando um valor nao estiver legivel ou nao existir.
                    """;
            case HOLERITE -> """
                    Extraia os dados e retorne apenas JSON valido neste formato:
                    {"mesReferencia":"MM/yyyy","proventos":[{"id":null,"descricao":"","referencia":"",
                    "valor":0.00,"vencimento":true}],"totalVencimentos":0.00}
                    Use null quando um valor nao estiver legivel ou nao existir.
                    """;
            case FICHA_FINANCEIRA -> """
                    A ficha pode ter varias paginas, anos e mais de uma folha na mesma competencia
                    (folha geral, ferias, decimo terceiro ou folha complementar).
                    Extraia cada bloco de folha separadamente. Para vencimentos, descontos e liquido,
                    use os totais exibidos no rodape do respectivo bloco. Nao consolide competencias repetidas.
                    Retorne apenas JSON valido neste formato:
                    {"folhas":[{"anoReferencia":2026,"competencia":"MM/yyyy","vencimentos":0.00,
                    "descontos":0.00,"liquido":0.00}]}
                    Use null quando um valor nao estiver legivel ou nao existir.
                    """;
            case CTS -> """
                    Extraia os dados e retorne apenas JSON valido neste formato:
                    {"inicioContribuicao":"dd/MM/yyyy","fimContribuicao":"dd/MM/yyyy","tempoAverbacao":0,
                    "totalBruto":0,"faltas":0,"totalDias":0,"tempoLegivel":""}
                    Use null quando um valor nao estiver legivel ou nao existir.
                    """;
        };
    }

    private String buildPromptAberturaFichaFuncional() {
        return """
                Extraia da ficha funcional os dados úteis para abertura de um processo de aposentadoria.
                Retorne somente JSON valido neste formato:
                {"nome":"","dtNascimento":"yyyy-MM-dd","cpf":"","pis":"","sexo":"MASCULINO|FEMININO|null","email":"",
                "matricula":"","cargo":"","orgao":"","dtAdmissao":"yyyy-MM-dd"}

                Regras:
                - Para campos de servidor, use null quando o dado nao estiver claro ou nao existir.
                - Extraia PIS, PASEP ou NIT para o campo pis quando aparecer.
                - Para dtNascimento e dtAdmissao, use yyyy-MM-dd.
                - Nao invente CPF, sexo, datas, matricula, cargo ou orgao.
                """;
    }
}
