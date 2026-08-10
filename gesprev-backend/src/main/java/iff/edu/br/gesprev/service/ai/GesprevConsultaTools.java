package iff.edu.br.gesprev.service.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import iff.edu.br.gesprev.dto.ChecklistDocumentoDTO;
import iff.edu.br.gesprev.dto.HistoricoProcessoDTO;
import iff.edu.br.gesprev.entity.AtoAposentadoria;
import iff.edu.br.gesprev.entity.Cts;
import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.MemoriaCalculo;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.Servidor;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.Sexo;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import iff.edu.br.gesprev.repository.DocumentoRepository;
import iff.edu.br.gesprev.repository.AtoAposentadoriaRepository;
import iff.edu.br.gesprev.repository.CtsRepository;
import iff.edu.br.gesprev.repository.MemoriaCalculoRepository;
import iff.edu.br.gesprev.service.ChecklistDocumentoService;
import iff.edu.br.gesprev.service.HistoricoProcessoService;
import iff.edu.br.gesprev.service.ProcessoAposentadoriaService;

@Component
public class GesprevConsultaTools {

    private final ProcessoAposentadoriaService processoService;
    private final ChecklistDocumentoService checklistService;
    private final HistoricoProcessoService historicoService;
    private final DocumentoRepository documentoRepository;
    private final MemoriaCalculoRepository memoriaRepository;
    private final CtsRepository ctsRepository;
    private final AtoAposentadoriaRepository atoRepository;

    public GesprevConsultaTools(
            ProcessoAposentadoriaService processoService,
            ChecklistDocumentoService checklistService,
            HistoricoProcessoService historicoService,
            DocumentoRepository documentoRepository,
            MemoriaCalculoRepository memoriaRepository,
            CtsRepository ctsRepository,
            AtoAposentadoriaRepository atoRepository) {
        this.processoService = processoService;
        this.checklistService = checklistService;
        this.historicoService = historicoService;
        this.documentoRepository = documentoRepository;
        this.memoriaRepository = memoriaRepository;
        this.ctsRepository = ctsRepository;
        this.atoRepository = atoRepository;
    }

    @Tool(name = "consultar_processo_gesprev", description = "Consulta dados atuais de um processo do GESPREV pelo numero. Use para status, servidor, documentos, checklist, historico e memoria de calculo. Ferramenta somente leitura.")
    public ResumoProcesso consultarProcesso(
            @ToolParam(description = "Numero exato do processo, somente digitos") int numeroProcesso) {
        ProcessoAposentadoria processo = processoService
                .obterProcessoAposentadoriaEntidadePorNumero(numeroProcesso);
        Long processoId = processo.getId();
        Servidor servidor = processo.getServidor();

        List<ResumoDocumento> documentos = documentoRepository.findByProcessoId(processoId).stream()
                .map(this::resumirDocumento)
                .toList();
        List<ChecklistDocumentoDTO> checklist = checklistService
                .obterChecklistDocumentosPorProcessoId(processoId);
        List<HistoricoProcessoDTO> historico = historicoService
                .obterHistoricosPorProcessoId(processoId);
        ResumoMemoria memoria = memoriaRepository.findByProcessoId(processoId)
                .map(this::resumirMemoria)
                .orElse(null);
        ResumoCts cts = ctsRepository.findByProcessoId(processoId)
                .map(this::resumirCts)
                .orElse(null);
        ResumoAto ato = atoRepository.findByProcessoId(processoId)
                .map(this::resumirAto)
                .orElse(null);

        return new ResumoProcesso(
                processoId,
                processo.getNumeroProcesso(),
                processo.getStatus(),
                processo.getDtCriacao(),
                processo.getDtAtualizacao(),
                new ResumoServidor(
                        servidor.getId(),
                        servidor.getNome(),
                        servidor.getSexo(),
                        calcularIdade(servidor.getDtNascimento()),
                        servidor.getPis(),
                        servidor.getMatricula(),
                        servidor.getCargo(),
                        servidor.getOrgao()),
                checklist,
                documentos,
                historico,
                memoria,
                cts,
                ato);
    }

    @Tool(name = "consultar_estatisticas_gesprev", description = "Consulta as quantidades de processos por situacao no GESPREV. Ferramenta somente leitura.")
    public Map<String, Long> consultarEstatisticas() {
        return Map.of(
                "total", processoService.contarTotal(),
                "cadastrados", processoService.contarPorStatus(StatusProcesso.CADASTRADO),
                "pendentesDocumento", processoService.contarPorStatus(StatusProcesso.PENDENTE_DOCUMENTO),
                "emAnalise", processoService.contarPorStatus(StatusProcesso.EM_ANALISE),
                "emCalculo", processoService.contarPorStatus(StatusProcesso.EM_CALCULO),
                "finalizados", processoService.contarPorStatus(StatusProcesso.FINALIZADO),
                "rejeitados", processoService.contarPorStatus(StatusProcesso.REJEITADO));
    }

    private ResumoDocumento resumirDocumento(Documento documento) {
        return new ResumoDocumento(
                documento.getId(),
                documento.getTipoDocumento(),
                documento.getStatusVLM(),
                documento.getNomeOriginal(),
                documento.getDtUpload());
    }

    private ResumoMemoria resumirMemoria(MemoriaCalculo memoria) {
        return new ResumoMemoria(
                memoria.getTipoCalculo().toString(),
                memoria.getMediaAritmetica(),
                memoria.getValorBeneficio(),
                memoria.getProporcionalidade());
    }

    private ResumoCts resumirCts(Cts cts) {
        return new ResumoCts(
                cts.getInicioContribuicao(),
                cts.getFimContribuicao(),
                cts.getTempoAverbacao(),
                cts.getTotalBruto(),
                cts.getFaltas(),
                cts.getTotalDias(),
                cts.getTempoLegivel());
    }

    private ResumoAto resumirAto(AtoAposentadoria ato) {
        BigDecimal valorTotalProventos = ato.getProventos().stream()
                .map(provento -> provento.getValor() == null ? BigDecimal.ZERO : provento.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoAto(
                ato.getNumeroPortaria(),
                ato.getAnoPortaria(),
                ato.getNaturezaAposentadoria().name(),
                ato.getTipoCalculo().name(),
                ato.getEmendaConstitucional().name(),
                ato.getReferenciaSalarial(),
                valorTotalProventos,
                ato.getDataFinalizacao(),
                ato.getDtGeracao());
    }

    private Integer calcularIdade(LocalDate nascimento) {
        if (nascimento == null) {
            return null;
        }
        return Period.between(nascimento, LocalDate.now()).getYears();
    }

    public record ResumoProcesso(
            Long id,
            int numeroProcesso,
            StatusProcesso status,
            LocalDateTime dtCriacao,
            LocalDateTime dtAtualizacao,
            ResumoServidor servidor,
            List<ChecklistDocumentoDTO> checklist,
            List<ResumoDocumento> documentos,
            List<HistoricoProcessoDTO> historico,
            ResumoMemoria memoriaCalculo,
            ResumoCts cts,
            ResumoAto ato) {
    }

    public record ResumoServidor(Long id, String nome, Sexo sexo, Integer idadeAnos, String pis, String matricula, String cargo, String orgao) {
    }

    public record ResumoDocumento(
            Long id,
            TipoDocumento tipo,
            StatusVLM statusVlm,
            String nomeOriginal,
            LocalDateTime dtUpload) {
    }

    public record ResumoMemoria(
            String tipoCalculo,
            BigDecimal mediaAritmetica,
            BigDecimal valorBeneficio,
            BigDecimal proporcionalidade) {
    }

    public record ResumoCts(
            LocalDate inicioContribuicao,
            LocalDate fimContribuicao,
            int tempoAverbacao,
            int totalBruto,
            int faltas,
            int totalDias,
            String tempoLegivel) {
    }

    public record ResumoAto(
            int numeroPortaria,
            int anoPortaria,
            String naturezaAposentadoria,
            String tipoCalculo,
            String emendaConstitucional,
            String referenciaSalarial,
            BigDecimal valorTotalProventos,
            LocalDate dataFinalizacao,
            LocalDateTime dtGeracao) {
    }
}
