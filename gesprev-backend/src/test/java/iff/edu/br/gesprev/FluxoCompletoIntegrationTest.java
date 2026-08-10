package iff.edu.br.gesprev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iff.edu.br.gesprev.repository.AtoAposentadoriaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoCompletoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtoAposentadoriaRepository atoAposentadoriaRepository;

    @Test
    void deveExecutarFluxoCompletoEExcluirBancoEArquivos() throws Exception {
        String tokenAnalista = login("900.000.000-01");
        String tokenDiretor = login("900.000.000-00");
        int numeroProcesso = 800000 + (int) (System.currentTimeMillis() % 100000);

        String processoJson = """
                {
                  "nome":"Servidor Fluxo Automatizado",
                  "dtNascimento":"1980-05-12",
                  "cpf":"888.777.666-55",
                  "sexo":"MASCULINO",
                  "email":"fluxo.automatizado@example.com",
                  "matricula":"TESTE-001",
                  "cargo":"Analista Previdenciario",
                  "orgao":"Instituto de Previdencia",
                  "dtAdmissao":"2005-03-01",
                  "numeroProcesso":%d
                }
                """.formatted(numeroProcesso);

        MvcResult criacao = mockMvc.perform(post("/api/processos-aposentadoria/com-servidor")
                        .header("Authorization", bearer(tokenAnalista))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CADASTRADO"))
                .andReturn();
        long processoId = json(criacao).get("id").asLong();

        List<Long> documentos = new ArrayList<>();
        List<Path> arquivos = new ArrayList<>();
        for (String tipo : List.of("FICHA_FUNCIONAL", "FICHA_FINANCEIRA", "HOLERITE", "CTS")) {
            MockMultipartFile pdf = new MockMultipartFile(
                    "arquivo",
                    tipo.toLowerCase() + ".pdf",
                    "application/pdf",
                    ("%PDF-1.4 arquivo de teste " + tipo).getBytes(StandardCharsets.US_ASCII));

            MvcResult upload = mockMvc.perform(multipart("/api/documentos/upload")
                            .file(pdf)
                            .param("processoId", Long.toString(processoId))
                            .param("tipoDocumento", tipo)
                            .header("Authorization", bearer(tokenAnalista)))
                    .andExpect(status().isCreated())
                    .andReturn();
            JsonNode documento = json(upload);
            documentos.add(documento.get("id").asLong());
            arquivos.add(Path.of(documento.get("caminhoArquivo").asText()).toAbsolutePath());
        }

        for (Long documentoId : documentos) {
            mockMvc.perform(post("/api/vlm/processar/{id}", documentoId)
                            .header("Authorization", bearer(tokenAnalista)))
                    .andExpect(status().isOk());
            mockMvc.perform(patch("/api/vlm/validar/{id}", documentoId)
                            .header("Authorization", bearer(tokenAnalista)))
                    .andExpect(status().isNoContent());
        }

        mockMvc.perform(get("/api/processos-aposentadoria/{id}", processoId)
                        .header("Authorization", bearer(tokenAnalista)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        mockMvc.perform(post("/api/memorias-calculo/calcular/{id}", processoId)
                        .param("tipoCalculo", "INTEGRAL")
                        .header("Authorization", bearer(tokenAnalista)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCalculo").value("INTEGRAL"));

        String atoJson = """
                {
                  "processoId":%d,
                  "naturezaAposentadoria":"VOLUNTARIA",
                  "tipoCalculo":"INTEGRAL",
                  "emendaConstitucional":"EC_103",
                  "referenciaSalarial":"Classe D",
                  "dataFinalizacao":"%s"
                }
                """.formatted(processoId, LocalDate.now());

        mockMvc.perform(post("/api/atos-aposentadoria/gerar")
                        .header("Authorization", bearer(tokenDiretor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atoJson))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());

        Long atoId = atoAposentadoriaRepository.findByProcessoId(processoId).orElseThrow().getId();
        mockMvc.perform(post("/api/atos-aposentadoria/gerar")
                        .header("Authorization", bearer(tokenDiretor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atoJson))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty());
        assertThat(atoAposentadoriaRepository.findByProcessoId(processoId).orElseThrow().getId())
                .isEqualTo(atoId);

        mockMvc.perform(delete("/api/processos-aposentadoria/{id}", processoId)
                        .header("Authorization", bearer(tokenDiretor)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/processos-aposentadoria/{id}", processoId)
                        .header("Authorization", bearer(tokenDiretor)))
                .andExpect(status().isNotFound());
        assertThat(arquivos).allMatch(path -> !Files.exists(path));
    }

    private String login(String cpf) throws Exception {
        String body = """
                {"cpf":"%s","senha":"senha123"}
                """.formatted(cpf);
        MvcResult result = mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return json(result).get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
