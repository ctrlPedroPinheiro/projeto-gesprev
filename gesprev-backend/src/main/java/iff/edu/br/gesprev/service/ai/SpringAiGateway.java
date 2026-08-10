package iff.edu.br.gesprev.service.ai;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import iff.edu.br.gesprev.entity.enums.TipoDocumento;

@Service
@ConditionalOnProperty(prefix = "gesprev.ai", name = "provider", havingValue = "openai-compatible")
public class SpringAiGateway implements AiGateway {

    private static final Logger log = LoggerFactory.getLogger(SpringAiGateway.class);

    private final ChatClient chatClient;
    private final RestClient restClient;
    private final PdfMediaService pdfMediaService;
    private final DocumentoTextoService documentoTextoService;
    private final ObjectMapper objectMapper;
    private final String llmModel;
    private final String vlmModel;
    private final String ollamaNativeUrl;
    private final int vlmNumContext;
    private final int vlmNumPredict;

    public SpringAiGateway(
            ChatClient.Builder chatClientBuilder,
            RestClient.Builder restClientBuilder,
            PdfMediaService pdfMediaService,
            DocumentoTextoService documentoTextoService,
            ObjectMapper objectMapper,
            @Value("${gesprev.ai.llm-model}") String llmModel,
            @Value("${gesprev.ai.vlm-model}") String vlmModel,
            @Value("${gesprev.ai.vlm.num-context:32768}") int vlmNumContext,
            @Value("${gesprev.ai.vlm.num-predict:8192}") int vlmNumPredict,
            @Value("${spring.ai.openai.base-url}") String openAiBaseUrl) {
        this.chatClient = chatClientBuilder.build();
        this.restClient = restClientBuilder.build();
        this.pdfMediaService = pdfMediaService;
        this.documentoTextoService = documentoTextoService;
        this.objectMapper = objectMapper;
        this.llmModel = llmModel;
        this.vlmModel = vlmModel;
        this.ollamaNativeUrl = openAiBaseUrl.replaceFirst("/v1/?$", "");
        this.vlmNumContext = vlmNumContext;
        this.vlmNumPredict = vlmNumPredict;
    }

    @Override
    public String extrairDocumento(Path arquivo, TipoDocumento tipoDocumento, String prompt) {
        List<String> falhas = new ArrayList<>();
        long inicio = System.currentTimeMillis();
        log.info("IA: iniciando extracao do documento tipo={} arquivo={}", tipoDocumento, arquivo.getFileName());

        try {
            log.info("IA: renderizando documento para imagens tipo={}", tipoDocumento);
            List<Media> paginas = pdfMediaService.carregar(arquivo);
            log.info("IA: documento renderizado tipo={} paginas={}", tipoDocumento, paginas.size());
            if (!paginas.isEmpty()) {
                String transcricao = transcreverComOllamaNativo(paginas);
                if (!transcricao.isBlank()) {
                    log.info("IA: transcricao visual concluida tipo={} caracteres={}", tipoDocumento, transcricao.length());
                    String json = extrairComTexto(transcricao, prompt);
                    log.info("IA: extracao concluida via VLM tipo={} tempoMs={}", tipoDocumento, System.currentTimeMillis() - inicio);
                    return json;
                }
                log.warn("IA: transcricao visual vazia tipo={}", tipoDocumento);
            }
        } catch (Exception e) {
            log.warn("IA: falha no fluxo VLM tipo={} erro={}", tipoDocumento, mensagem(e));
            falhas.add("VLM: " + mensagem(e));
        }

        String textoOcr = documentoTextoService.extrairComOcr(arquivo);
        if (!textoOcr.isBlank()) {
            try {
                log.info("IA: usando OCR como fallback tipo={} caracteres={}", tipoDocumento, textoOcr.length());
                String json = extrairComTexto(textoOcr, prompt);
                log.info("IA: extracao concluida via OCR tipo={} tempoMs={}", tipoDocumento, System.currentTimeMillis() - inicio);
                return json;
            } catch (Exception e) {
                log.warn("IA: falha no fallback OCR tipo={} erro={}", tipoDocumento, mensagem(e));
                falhas.add("OCR/LLM: " + mensagem(e));
            }
        }

        String textoPesquisavel = documentoTextoService.extrairTextoPesquisavel(arquivo);
        if (!textoPesquisavel.isBlank()) {
            try {
                log.info("IA: usando texto pesquisavel como fallback tipo={} caracteres={}", tipoDocumento, textoPesquisavel.length());
                String json = extrairComTexto(textoPesquisavel, prompt);
                log.info("IA: extracao concluida via PDF pesquisavel tipo={} tempoMs={}", tipoDocumento, System.currentTimeMillis() - inicio);
                return json;
            } catch (Exception e) {
                log.warn("IA: falha no fallback PDF pesquisavel tipo={} erro={}", tipoDocumento, mensagem(e));
                falhas.add("PDF pesquisavel/LLM: " + mensagem(e));
            }
        }

        String detalhe = falhas.isEmpty()
                ? "nenhuma estrategia conseguiu extrair texto"
                : String.join("; ", falhas);
        throw new RuntimeException("Falha ao processar o documento: " + detalhe);
    }

    private String extrairComTexto(String textoExtraido, String prompt) {
        log.info("IA: iniciando conversao texto-para-JSON modelo={} caracteres={}", llmModel, textoExtraido.length());
        return chamarOllamaNativoTexto(
                llmModel,
                """
                Voce extrai dados de documentos previdenciarios a partir de texto transcrito/OCR/PDF.
                Nao invente valores ausentes, ilegiveis ou ambiguos.
                Retorne somente JSON valido no formato solicitado pelo usuario.
                """,
                """
                /no_think
                Texto extraido do documento:
                ---
                %s
                ---

                %s
                """.formatted(textoExtraido, prompt),
                Math.max(vlmNumPredict, 8192));
    }

    private String transcreverComOllamaNativo(List<Media> paginas) {
        StringBuilder transcricao = new StringBuilder();
        for (int indice = 0; indice < paginas.size(); indice++) {
            long inicioPagina = System.currentTimeMillis();
            log.info("IA: enviando pagina ao VLM pagina={}/{} modelo={}", indice + 1, paginas.size(), vlmModel);
            String textoPagina = transcreverPagina(paginas.get(indice), indice + 1);
            if (!textoPagina.isBlank()) {
                transcricao.append("\n\n[PAGINA ")
                        .append(indice + 1)
                        .append("]\n")
                        .append(textoPagina.trim());
            }
            log.info("IA: pagina processada pelo VLM pagina={}/{} caracteres={} tempoMs={}",
                    indice + 1, paginas.size(), textoPagina.length(), System.currentTimeMillis() - inicioPagina);
        }
        return transcricao.toString().trim();
    }

    private String transcreverPagina(Media pagina, int numeroPagina) {
        String imagem = Base64.getEncoder().encodeToString(pagina.getDataAsByteArray());
        Map<String, Object> mensagem = Map.of(
                "role", "user",
                "content", "/no_think\n"
                        + "Transcreva fielmente todo o texto visivel desta pagina de um documento previdenciario. "
                        + "Preserve linhas, rotulos, datas e valores. Nao resuma, nao interprete e nao produza JSON. "
                        + "Quando algo estiver ilegivel, escreva [ILEGIVEL]. Pagina " + numeroPagina + ".",
                "images", List.of(imagem));
        Map<String, Object> requisicao = Map.of(
                "model", vlmModel,
                "messages", List.of(mensagem),
                "stream", false,
                "think", false,
                "options", Map.of(
                        "num_ctx", vlmNumContext,
                        "num_predict", vlmNumPredict,
                        "temperature", 0));
        try {
            String corpo = restClient.post()
                    .uri(ollamaNativeUrl + "/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requisicao)
                    .retrieve()
                    .body(String.class);
            if (corpo == null || corpo.isBlank()) {
                return "";
            }
            JsonNode resposta = objectMapper.readTree(corpo);
            return resposta.path("message").path("content").asText("");
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Ollama retornou " + e.getStatusCode().value()
                    + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Resposta invalida do Ollama: " + e.getMessage(), e);
        }
    }

    private String chamarOllamaNativoTexto(String modelo, String instrucaoSistema, String pergunta, int numPredict) {
        Map<String, Object> requisicao = Map.of(
                "model", modelo,
                "messages", List.of(
                        Map.of("role", "system", "content", instrucaoSistema),
                        Map.of("role", "user", "content", pergunta)),
                "stream", false,
                "think", false,
                "options", Map.of(
                        "num_ctx", vlmNumContext,
                        "num_predict", numPredict,
                        "temperature", 0));
        try {
            String corpo = restClient.post()
                    .uri(ollamaNativeUrl + "/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requisicao)
                    .retrieve()
                    .body(String.class);
            if (corpo == null || corpo.isBlank()) {
                throw new RuntimeException("Ollama retornou corpo vazio");
            }
            JsonNode resposta = objectMapper.readTree(corpo);
            String conteudo = resposta.path("message").path("content").asText("");
            if (conteudo.isBlank()) {
                throw new RuntimeException("Ollama retornou message.content vazio: " + corpo);
            }
            log.info("IA: Ollama retornou conteudo textual modelo={} caracteres={}", modelo, conteudo.length());
            return conteudo;
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Ollama retornou " + e.getStatusCode().value()
                    + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Resposta invalida do Ollama: " + e.getMessage(), e);
        }
    }

    private String mensagem(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    @Override
    public String responder(String instrucaoSistema, String pergunta) {
        return chatClient.prompt()
                .system(instrucaoSistema)
                .user(pergunta)
                .options(OpenAiChatOptions.builder().model(llmModel).maxTokens(4096))
                .call()
                .content();
    }

    @Override
    public String responderComFerramentas(String instrucaoSistema, String pergunta, Object... ferramentas) {
        return chatClient.prompt()
                .system(instrucaoSistema)
                .user(pergunta)
                .tools(ferramentas)
                .options(OpenAiChatOptions.builder().model(llmModel).maxTokens(4096))
                .call()
                .content();
    }

    @Override
    public boolean simulado() {
        return false;
    }

    @Override
    public String provedor() {
        return "openai-compatible";
    }
}
