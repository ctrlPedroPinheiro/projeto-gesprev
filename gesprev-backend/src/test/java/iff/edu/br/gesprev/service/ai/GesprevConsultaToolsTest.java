package iff.edu.br.gesprev.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.Servidor;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.Sexo;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import iff.edu.br.gesprev.repository.AtoAposentadoriaRepository;
import iff.edu.br.gesprev.repository.CtsRepository;
import iff.edu.br.gesprev.repository.DocumentoRepository;
import iff.edu.br.gesprev.repository.MemoriaCalculoRepository;
import iff.edu.br.gesprev.service.ChecklistDocumentoService;
import iff.edu.br.gesprev.service.HistoricoProcessoService;
import iff.edu.br.gesprev.service.ProcessoAposentadoriaService;

@ExtendWith(MockitoExtension.class)
class GesprevConsultaToolsTest {

    @Mock
    private ProcessoAposentadoriaService processoService;
    @Mock
    private ChecklistDocumentoService checklistService;
    @Mock
    private HistoricoProcessoService historicoService;
    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private MemoriaCalculoRepository memoriaRepository;
    @Mock
    private CtsRepository ctsRepository;
    @Mock
    private AtoAposentadoriaRepository atoRepository;

    private GesprevConsultaTools tools;

    @BeforeEach
    void configurar() {
        tools = new GesprevConsultaTools(
                processoService,
                checklistService,
                historicoService,
                documentoRepository,
                memoriaRepository,
                ctsRepository,
                atoRepository);
    }

    @Test
    void deveConsultarProcessoSemExporDadosSensiveisOuCaminhos() throws Exception {
        Servidor servidor = new Servidor(
                5L,
                "Maria da Silva",
                LocalDate.of(1980, 1, 1),
                "123.456.789-00",
                "123.45678.90-1",
                Sexo.FEMININO,
                "maria@example.com",
                "MAT-10",
                "Analista",
                "Prefeitura",
                LocalDate.of(2005, 1, 1));
        ProcessoAposentadoria processo = new ProcessoAposentadoria(
                10L,
                2026003,
                LocalDateTime.now(),
                StatusProcesso.EM_ANALISE,
                LocalDateTime.now(),
                servidor);
        Documento documento = new Documento(
                20L,
                "interno.pdf",
                "uploads/processo_10/segredo.pdf",
                TipoDocumento.FICHA_FUNCIONAL,
                StatusVLM.VALIDADO,
                LocalDateTime.now(),
                processo,
                "ficha.pdf",
                "{\"cpf\":\"123.456.789-00\"}");

        when(processoService.obterProcessoAposentadoriaEntidadePorNumero(2026003)).thenReturn(processo);
        when(documentoRepository.findByProcessoId(10L)).thenReturn(List.of(documento));
        when(checklistService.obterChecklistDocumentosPorProcessoId(10L)).thenReturn(List.of());
        when(historicoService.obterHistoricosPorProcessoId(10L)).thenReturn(List.of());
        when(memoriaRepository.findByProcessoId(10L)).thenReturn(Optional.empty());
        when(ctsRepository.findByProcessoId(10L)).thenReturn(Optional.empty());
        when(atoRepository.findByProcessoId(10L)).thenReturn(Optional.empty());

        GesprevConsultaTools.ResumoProcesso resumo = tools.consultarProcesso(2026003);
        String json = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(resumo);

        assertEquals(StatusProcesso.EM_ANALISE, resumo.status());
        assertEquals("Maria da Silva", resumo.servidor().nome());
        assertEquals(Sexo.FEMININO, resumo.servidor().sexo());
        assertFalse(json.contains("123.456.789-00"));
        assertFalse(json.contains("maria@example.com"));
        assertFalse(json.contains("uploads/"));
        assertFalse(json.contains("jsonExtraido"));
    }
}
