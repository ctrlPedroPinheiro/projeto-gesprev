package iff.edu.br.gesprev.service;

import iff.edu.br.gesprev.dto.MemoriaCalculoDTO;
import iff.edu.br.gesprev.dto.FolhaCalculoDTO;
import iff.edu.br.gesprev.dto.ProventoMemoriaDTO;
import iff.edu.br.gesprev.entity.FichaFinanceira;
import iff.edu.br.gesprev.entity.Folha;
import iff.edu.br.gesprev.entity.Holerite;
import iff.edu.br.gesprev.entity.MemoriaCalculo;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.Provento;
import iff.edu.br.gesprev.entity.enums.TipoCalculo;
import iff.edu.br.gesprev.repository.FichaFinanceiraRepository;
import iff.edu.br.gesprev.repository.HoleriteRepository;
import iff.edu.br.gesprev.repository.MemoriaCalculoRepository;
import iff.edu.br.gesprev.security.UsuarioAutenticado;
import iff.edu.br.gesprev.repository.ChecklistDocumentoRepository;
import iff.edu.br.gesprev.repository.DocumentoRepository;
import iff.edu.br.gesprev.entity.ChecklistDocumento;
import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.enums.StatusVLM;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class MemoriaCalculoService {

    private final MemoriaCalculoRepository memoriaCalculoRepository;
    private final ProcessoAposentadoriaService processoAposentadoriaService;
    private final HoleriteRepository holeriteRepository;
    private final FichaFinanceiraRepository fichaFinanceiraRepository;
    private final FatorAtualizacaoService fatorAtualizacaoService;
    private final ProcessoFluxoService processoFluxoService;
    private final UsuarioAutenticado usuarioAutenticado;
    private final ChecklistDocumentoRepository checklistDocumentoRepository;
    private final DocumentoRepository documentoRepository;

    public MemoriaCalculoService(MemoriaCalculoRepository memoriaCalculoRepository, ProcessoAposentadoriaService processoAposentadoriaService, HoleriteRepository holeriteRepository, FichaFinanceiraRepository fichaFinanceiraRepository, FatorAtualizacaoService fatorAtualizacaoService, ProcessoFluxoService processoFluxoService, UsuarioAutenticado usuarioAutenticado, ChecklistDocumentoRepository checklistDocumentoRepository, DocumentoRepository documentoRepository) {
        this.memoriaCalculoRepository = memoriaCalculoRepository;
        this.processoAposentadoriaService = processoAposentadoriaService;
        this.holeriteRepository = holeriteRepository;
        this.fichaFinanceiraRepository = fichaFinanceiraRepository;
        this.fatorAtualizacaoService = fatorAtualizacaoService;
        this.processoFluxoService = processoFluxoService;
        this.usuarioAutenticado = usuarioAutenticado;
        this.checklistDocumentoRepository = checklistDocumentoRepository;
        this.documentoRepository = documentoRepository;
    }

    @Transactional
    public MemoriaCalculoDTO calcular(Long processoId, String tipoCalculo) {
        ProcessoAposentadoria processo = processoAposentadoriaService
                .obterProcessoAposentadoriaEntidadePorId(processoId);

        List<ChecklistDocumento> checklist = checklistDocumentoRepository.findByProcessoId(processoId);
        
        if (checklist.isEmpty()) {
            throw new RuntimeException("Nenhum checklist encontrado para este processo");
        }

        boolean checklistIncompleto = checklist.stream()
                .anyMatch(item -> !item.isEntregue() || !item.isValido());

        if (checklistIncompleto) {
            throw new RuntimeException("Todos os documentos devem estar entregues e validados antes de gerar a memória de cálculo");
        }

        List<Documento> documentos = documentoRepository.findByProcessoId(processoId);

        if (documentos.isEmpty()) {
            throw new RuntimeException("Nenhum documento encontrado para este processo");
        }

        boolean documentosPendentes = documentos.stream()
                .anyMatch(doc -> doc.getStatusVLM() != StatusVLM.VALIDADO);

        if (documentosPendentes) {
            throw new RuntimeException("Todos os documentos devem estar com status VALIDADO antes de gerar a memória de cálculo");
        }

        MemoriaCalculoDTO resultado = switch (tipoCalculo.toUpperCase()) {
            case "INTEGRAL" -> calcularIntegral(processo);
            case "PROPORCIONAL" -> calcularProporcional(processo);
            default -> throw new RuntimeException("Tipo de cálculo inválido: " + tipoCalculo);
        };

        processoFluxoService.avancarParaEmCalculo(
                processoId,
                usuarioAutenticado.obterUsuarioId()
        );

        return resultado;
    }

    private MemoriaCalculoDTO calcularIntegral(ProcessoAposentadoria processo) {
        Holerite holerite = holeriteRepository.findByProcessoId(processo.getId()).stream()
                .max(Comparator.comparing(this::obterCompetenciaHolerite))
                .orElseThrow(() -> new RuntimeException("Nenhum holerite encontrado para o processo"));

        BigDecimal valorBeneficio = holerite.calcularValorTotalProventos();

        MemoriaCalculo memoriaCalculo = salvarMemoriaCalculo(
                processo,
                TipoCalculo.INTEGRAL,
                valorBeneficio,
                valorBeneficio,
                BigDecimal.ONE,
                holerite
        );

        return converterEntidade(memoriaCalculo);
    }

    private YearMonth obterCompetenciaHolerite(Holerite holerite) {
        try {
            return YearMonth.parse(holerite.getMesReferencia(), DateTimeFormatter.ofPattern("MM/yyyy"));
        } catch (Exception e) {
            throw new RuntimeException("Competencia invalida no holerite " + holerite.getId(), e);
        }
    }

    private MemoriaCalculoDTO calcularProporcional(ProcessoAposentadoria processo) {
        List<Folha> folhas = obterFolhasDoProcesso(processo.getId());

        if (folhas.isEmpty()) {
            throw new RuntimeException("Ficha financeira sem folhas para cálculo");
        }

        List<BigDecimal> contribuicoesCorrigidas = new ArrayList<>();
        for (Folha folha : folhas) {
            BigDecimal contribuicao = folha.getLiquido();
            LocalDate mesCompetencia = obterCompetenciaFolha(folha).atDay(1);
            BigDecimal fator = fatorAtualizacaoService.obterFator(mesCompetencia);
            BigDecimal contribuicaoCorrigida = contribuicao.multiply(fator).setScale(2, RoundingMode.HALF_UP);

            contribuicoesCorrigidas.add(contribuicaoCorrigida);
        }

        contribuicoesCorrigidas.sort(Comparator.reverseOrder());
        int quantidade80Porcento = (int) Math.ceil(contribuicoesCorrigidas.size() * 0.80);
        List<BigDecimal> maiores80 = contribuicoesCorrigidas.subList(0, quantidade80Porcento);

        BigDecimal soma = maiores80.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        BigDecimal mediaAritmetica = soma.divide(BigDecimal.valueOf(maiores80.size()), 2, RoundingMode.HALF_UP);

        BigDecimal proporcionalidade = BigDecimal.valueOf(maiores80.size()).divide(BigDecimal.valueOf(contribuicoesCorrigidas.size()), 4, RoundingMode.HALF_UP);

        MemoriaCalculo memoriaCalculo = salvarMemoriaCalculo(
                processo,
                TipoCalculo.PROPORCIONAL,
                mediaAritmetica,
                mediaAritmetica,
                proporcionalidade,
                null
        );

        return converterEntidade(memoriaCalculo);
    }

    private List<Folha> obterFolhasDoProcesso(Long processoId) {
        List<FichaFinanceira> fichas = fichaFinanceiraRepository.findByProcessoId(processoId);
        if (fichas.isEmpty()) {
            throw new RuntimeException("Nenhuma ficha financeira encontrada para o processo");
        }
        return fichas.stream()
                .flatMap(ficha -> ficha.getFolhas().stream())
                .sorted(Comparator.comparing(this::obterCompetenciaFolha))
                .toList();
    }

    private YearMonth obterCompetenciaFolha(Folha folha) {
        String competencia = folha.getCompetencia();
        try {
            if (competencia.contains("/")) {
                String[] partes = competencia.split("/");
                return YearMonth.of(folha.getAnoReferencia(), Integer.parseInt(partes[0]));
            }
            return YearMonth.of(folha.getAnoReferencia(), Integer.parseInt(competencia));
        } catch (Exception e) {
            throw new RuntimeException("Competencia invalida na folha " + folha.getId(), e);
        }
    }

    private MemoriaCalculo salvarMemoriaCalculo(
            ProcessoAposentadoria processo,
            TipoCalculo tipoCalculo,
            BigDecimal mediaAritmetica,
            BigDecimal valorBeneficio,
            BigDecimal proporcionalidade,
            Holerite holerite) {

        MemoriaCalculo memoriaCalculo = memoriaCalculoRepository.findByProcessoId(processo.getId()).orElse(new MemoriaCalculo());

        memoriaCalculo.setMediaAritmetica(mediaAritmetica);
        memoriaCalculo.setValorBeneficio(valorBeneficio);
        memoriaCalculo.setProporcionalidade(proporcionalidade);
        memoriaCalculo.setTipoCalculo(tipoCalculo);
        memoriaCalculo.setProcesso(processo);
        memoriaCalculo.setHolerite(holerite);

        return memoriaCalculoRepository.save(memoriaCalculo);
    }

    @Transactional(readOnly = true)
    public MemoriaCalculoDTO obterMemoriaCalculoPorId(Long id) {
        MemoriaCalculo memoriaCalculo = memoriaCalculoRepository.findById(id).orElseThrow(() -> new RuntimeException("Memória de cálculo não encontrada"));
        return converterEntidade(memoriaCalculo);
    }

    @Transactional(readOnly = true)
    public MemoriaCalculoDTO obterMemoriaCalculoPorProcessoId(Long processoId) {
        MemoriaCalculo memoriaCalculo = memoriaCalculoRepository.findByProcessoId(processoId).orElseThrow(() -> new RuntimeException("Memória de cálculo não encontrada"));
        return converterEntidade(memoriaCalculo);
    }

    public void deletarMemoriaCalculo(Long id) {
        if (!memoriaCalculoRepository.existsById(id)) {
            throw new RuntimeException("Memória de cálculo não encontrada");
        }
        memoriaCalculoRepository.deleteById(id);
    }

    public MemoriaCalculoDTO converterEntidade(MemoriaCalculo memoriaCalculo) {
        Holerite holerite = memoriaCalculo.getHolerite();
        List<ProventoMemoriaDTO> vencimentos = holerite == null
                ? List.of()
                : holerite.getProventos().stream()
                        .filter(Objects::nonNull)
                        .filter(Provento::isVencimento)
                        .map(p -> new ProventoMemoriaDTO(
                                p.getId(), p.getTipoProvento(), p.getReferencia(), p.getValor()))
                        .toList();
        List<FolhaCalculoDTO> folhasCalculo = memoriaCalculo.getTipoCalculo() == TipoCalculo.PROPORCIONAL
                ? montarDetalhesFolhas(memoriaCalculo.getProcesso().getId())
                : List.of();
        return new MemoriaCalculoDTO(
                memoriaCalculo.getId(),
                memoriaCalculo.getMediaAritmetica(),
                memoriaCalculo.getValorBeneficio(),
                memoriaCalculo.getProporcionalidade(),
                memoriaCalculo.getTipoCalculo().toString(),
                memoriaCalculo.getProcesso().getId(),
                holerite == null ? null : holerite.getId(),
                holerite == null ? null : holerite.getMesReferencia(),
                holerite == null ? null : holerite.calcularValorTotalProventos(),
                vencimentos,
                folhasCalculo
        );
    }

    private List<FolhaCalculoDTO> montarDetalhesFolhas(Long processoId) {
        record Detalhe(Folha folha, BigDecimal fator, BigDecimal corrigido) {}

        List<Detalhe> detalhes = obterFolhasDoProcesso(processoId).stream()
                .map(folha -> {
                    BigDecimal fator = fatorAtualizacaoService.obterFator(obterCompetenciaFolha(folha).atDay(1));
                    BigDecimal corrigido = folha.getLiquido().multiply(fator).setScale(2, RoundingMode.HALF_UP);
                    return new Detalhe(folha, fator, corrigido);
                })
                .sorted(Comparator.comparing(Detalhe::corrigido).reversed())
                .toList();
        int quantidadeUtilizada = (int) Math.ceil(detalhes.size() * 0.80);
        List<FolhaCalculoDTO> resultado = new ArrayList<>();
        for (int i = 0; i < detalhes.size(); i++) {
            Detalhe detalhe = detalhes.get(i);
            Folha folha = detalhe.folha();
            resultado.add(new FolhaCalculoDTO(
                    folha.getAnoReferencia(),
                    folha.getCompetencia(),
                    folha.getLiquido(),
                    detalhe.fator(),
                    detalhe.corrigido(),
                    i < quantidadeUtilizada));
        }
        return resultado;
    }
}
