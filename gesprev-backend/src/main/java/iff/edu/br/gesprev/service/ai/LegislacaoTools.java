package iff.edu.br.gesprev.service.ai;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LegislacaoTools {

    private static final int LIMITE_RESULTADOS = 2;
    private static final int LIMITE_CARACTERES_TRECHO = 1500;
    private static final Logger LOGGER = LoggerFactory.getLogger(LegislacaoTools.class);
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${gesprev.rag.url:http://localhost:8001}")
    private String ragServiceUrl;

    public LegislacaoTools(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    @Tool(name = "buscar_legislacao_previdenciaria", description = "Busca trechos na base de legislacao previdenciaria. Use para regras, emendas constitucionais e fundamentos legais. Nao use para andamento de processo especifico.")
    public String buscarLegislacao(
            @ToolParam(description = "Pergunta ou tema juridico previdenciario") String consulta) {
        try {
            String consultaNormalizada = normalizarConsulta(consulta);
            LOGGER.info("Consultando RAG em {} com consulta={}", ragServiceUrl, consultaNormalizada);
            String responseBody = restClient.post()
                    .uri(ragServiceUrl + "/buscar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("consulta", consultaNormalizada, "limit", LIMITE_RESULTADOS))
                    .retrieve()
                    .body(String.class);
            LOGGER.info("RAG respondeu com sucesso");
            return formatarResultados(responseBody);
        } catch (RestClientResponseException e) {
            return "Base legislativa indisponivel no momento: Servico RAG retornou status "
                    + e.getStatusCode().value() + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Base legislativa indisponivel no momento: " + e.getMessage();
        }
    }

    private String normalizarConsulta(String consulta) throws Exception {
        if (consulta == null || consulta.isBlank()) {
            throw new IllegalArgumentException("Consulta legislativa vazia");
        }

        String texto = consulta.trim();
        if (texto.startsWith("{") && texto.endsWith("}")) {
            Map<?, ?> argumentos = objectMapper.readValue(texto, Map.class);
            Object valor = argumentos.get("consulta");
            if (valor == null) {
                valor = argumentos.get("query");
            }
            if (valor != null) {
                texto = String.valueOf(valor).trim();
            }
        }

        if (texto.isBlank()) {
            throw new IllegalArgumentException("Consulta legislativa vazia");
        }
        return texto;
    }

    private String formatarResultados(String responseBody) throws Exception {
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<?> resultados = (List<?>) responseMap.get("resultados");
        StringBuilder contexto = new StringBuilder();
        if (resultados != null) {
            for (Object item : resultados) {
                Map<?, ?> resultado = (Map<?, ?>) item;
                contexto.append("Fonte: ").append(resultado.get("fonte")).append("\n");
                String texto = String.valueOf(resultado.get("texto"));
                if (texto.length() > LIMITE_CARACTERES_TRECHO) {
                    texto = texto.substring(0, LIMITE_CARACTERES_TRECHO) + " [...]";
                }
                contexto.append(texto).append("\n\n");
            }
        }
        return contexto.isEmpty() ? "Nenhum trecho relevante foi encontrado." : contexto.toString();
    }
}
