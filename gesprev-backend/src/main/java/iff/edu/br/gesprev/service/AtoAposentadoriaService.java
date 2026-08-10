package iff.edu.br.gesprev.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import iff.edu.br.gesprev.dto.AtoAposentadoriaDTO;
import iff.edu.br.gesprev.entity.AtoAposentadoria;
import iff.edu.br.gesprev.entity.MemoriaCalculo;
import iff.edu.br.gesprev.entity.Provento;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.SequencialPortaria;
import iff.edu.br.gesprev.entity.Servidor;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import iff.edu.br.gesprev.repository.SequencialPortariaRepository;
import iff.edu.br.gesprev.repository.AtoAposentadoriaRepository;
import iff.edu.br.gesprev.repository.MemoriaCalculoRepository;
import iff.edu.br.gesprev.repository.UsuarioRepository;
import iff.edu.br.gesprev.security.UsuarioAutenticado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AtoAposentadoriaService {

    private final ProcessoAposentadoriaService processoAposentadoriaService;
    private final SequencialPortariaRepository sequencialPortariaRepository;
    private final ProcessoFluxoService processoFluxoService;
    private final UsuarioAutenticado usuarioAutenticado;
    private final AtoAposentadoriaRepository atoAposentadoriaRepository;
    private final MemoriaCalculoRepository memoriaCalculoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final String TIMBRE_PATH = "src/main/resources/static/timbre.png";
    private static final String MUNICIPIO = "Itaocara";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AtoAposentadoriaService(ProcessoAposentadoriaService processoAposentadoriaService, SequencialPortariaRepository sequencialPortariaRepository, ProcessoFluxoService processoFluxoService, UsuarioAutenticado usuarioAutenticado, AtoAposentadoriaRepository atoAposentadoriaRepository, MemoriaCalculoRepository memoriaCalculoRepository, UsuarioRepository usuarioRepository) {
        this.processoAposentadoriaService = processoAposentadoriaService;
        this.sequencialPortariaRepository = sequencialPortariaRepository;
        this.processoFluxoService = processoFluxoService;
        this.usuarioAutenticado = usuarioAutenticado;
        this.atoAposentadoriaRepository = atoAposentadoriaRepository;
        this.memoriaCalculoRepository = memoriaCalculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public byte[] gerarAto(AtoAposentadoriaDTO dto) {
        ProcessoAposentadoria processo = processoAposentadoriaService
                .obterProcessoAposentadoriaEntidadePorId(dto.processoId());

        if (processo.getStatus() != StatusProcesso.EM_CALCULO
                && processo.getStatus() != StatusProcesso.FINALIZADO) {
            throw new RuntimeException("Processo precisa estar EM_CALCULO ou FINALIZADO para gerar o Ato de Aposentadoria");
        }

        Servidor servidor = processo.getServidor();
        boolean deveFinalizar = processo.getStatus() == StatusProcesso.EM_CALCULO;
        AtoAposentadoria ato = atoAposentadoriaRepository.findByProcessoId(dto.processoId()).orElse(null);

        if (ato == null) {
            MemoriaCalculo memoria = memoriaCalculoRepository.findByProcessoId(dto.processoId())
                    .orElseThrow(() -> new RuntimeException("Memoria de calculo nao encontrada"));
            if (memoria.getTipoCalculo() != dto.tipoCalculo()) {
                throw new RuntimeException("O tipo do ato deve ser o mesmo da memoria de calculo");
            }
            int anoAtual = LocalDate.now().getYear();
            ato = new AtoAposentadoria(
                    null, proximoNumeroPortaria(anoAtual), anoAtual,
                    dto.naturezaAposentadoria(), dto.tipoCalculo(), dto.emendaConstitucional(),
                    dto.referenciaSalarial(), montarProventos(memoria), dto.dataFinalizacao(),
                    LocalDateTime.now(), processo,
                    usuarioRepository.findById(usuarioAutenticado.obterUsuarioId())
                            .orElseThrow(() -> new RuntimeException("Usuario gerador nao encontrado")));
            ato = atoAposentadoriaRepository.save(ato);
        } else if (ato.getProventos() == null || ato.getProventos().isEmpty()) {
            MemoriaCalculo memoria = memoriaCalculoRepository.findByProcessoId(dto.processoId())
                    .orElseThrow(() -> new RuntimeException("Memoria de calculo nao encontrada"));
            ato.getProventos().addAll(montarProventos(memoria));
            ato = atoAposentadoriaRepository.save(ato);
        }

        int ano = ato.getAnoPortaria();
        int numeroPortaria = ato.getNumeroPortaria();
        List<Provento> proventos = ato.getProventos();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            if (Files.exists(Path.of(TIMBRE_PATH))) {
                Image timbre = new Image(ImageDataFactory.create(TIMBRE_PATH));
                timbre.setWidth(UnitValue.createPercentValue(50));
                timbre.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                document.add(timbre);
            }

            document.add(new Paragraph(
                String.format("PORTARIA Nº %03d/%d", numeroPortaria, ano))
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

            document.add(new Paragraph(
                "A DIRETORA PRESIDENTE DO INSTITUTO DE PREVIDÊNCIA, NA FORMA DAS LEIS " +
                "MUNICIPAIS E NO USO DE SUAS ATRIBUIÇÕES LEGAIS,")
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginTop(20));

            String corpo = String.format(
                "APOSENTAR, %s por %s, o servidor municipal, Sr/Sra. %s, " +
                "lotado(a) no %s na função de %s, sob a matrícula nº %s, %s, " +
                "admitido(a) através de concurso público no dia %s, " +
                "com fulcro na %s, com proventos %s, " +
                "conforme o processo administrativo nº %d/%d.",
                ato.getNaturezaAposentadoria().name(),
                ato.getTipoCalculo().name(),
                servidor.getNome(),
                servidor.getOrgao(),
                servidor.getCargo(),
                servidor.getMatricula(),
                ato.getReferenciaSalarial(),
                servidor.getDtAdmissao().format(FORMATTER),
                ato.getEmendaConstitucional().name().replace("_", " "),
                ato.getTipoCalculo().name(),
                processo.getNumeroProcesso(),
                processo.getDtCriacao().getYear()
            );

            document.add(new Paragraph(corpo)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginTop(20));

            document.add(new Paragraph("FIXAÇÃO DE PROVENTOS")
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

            Table tabela = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(100));

            tabela.addHeaderCell(new Cell().add(new Paragraph("DESCRIÇÃO DAS PARCELAS").setBold()));
            tabela.addHeaderCell(new Cell().add(new Paragraph("VALOR").setBold()));

            NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            for (Provento provento : proventos) {
                tabela.addCell(provento.getTipoProvento());
                tabela.addCell(moeda.format(provento.getValor()));
            }

            BigDecimal total = proventos.stream()
                    .map(Provento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            tabela.addCell(new Cell().add(new Paragraph("Totalizando").setBold()));
            tabela.addCell(new Cell().add(new Paragraph(moeda.format(total)).setBold()));

            document.add(tabela);

            document.add(new Paragraph(
                "Esta portaria entrará em vigor na data de sua publicação, " +
                "com efeitos a contar de " + ato.getDataFinalizacao().format(FORMATTER))
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginTop(20));

            document.add(new Paragraph("Registre-se. Publique-se. Cumpra-se.")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

            document.add(new Paragraph(
                MUNICIPIO + ", " + LocalDate.now().format(FORMATTER))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));

            document.close();
            byte[] pdf = baos.toByteArray();
            if (deveFinalizar) {
                processoFluxoService.finalizarPorAto(dto.processoId(), usuarioAutenticado.obterUsuarioId());
            }
            return pdf;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Ato de Aposentadoria: " + e.getMessage());
        }
    }

    List<Provento> montarProventos(MemoriaCalculo memoria) {
        if (memoria.getTipoCalculo() == iff.edu.br.gesprev.entity.enums.TipoCalculo.PROPORCIONAL) {
            return new ArrayList<>(List.of(new Provento(
                    "PROVENTOS PROPORCIONAIS",
                    memoria.getProporcionalidade().doubleValue(),
                    memoria.getValorBeneficio(),
                    true)));
        }

        if (memoria.getHolerite() == null) {
            throw new RuntimeException("Holerite da memoria integral nao encontrado");
        }
        List<Provento> proventos = memoria.getHolerite().getProventos().stream()
                .filter(Provento::isVencimento)
                .map(origem -> new Provento(
                        origem.getTipoProvento(),
                        origem.getReferencia(),
                        origem.getValor(),
                        true))
                .collect(Collectors.toCollection(ArrayList::new));
        if (proventos.isEmpty()) {
            throw new RuntimeException("Holerite nao possui vencimentos para o ato");
        }
        return proventos;
    }

    private int proximoNumeroPortaria(int ano) {
        SequencialPortaria sequencial = sequencialPortariaRepository.findById(ano)
                .orElse(new SequencialPortaria(ano, 0));

        sequencial.setUltimo(sequencial.getUltimo() + 1);
        sequencialPortariaRepository.save(sequencial);
        return sequencial.getUltimo();
    }
}
