package iff.edu.br.gesprev.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iff.edu.br.gesprev.dto.CtsDTO;
import iff.edu.br.gesprev.dto.FichaFinanceiraDTO;
import iff.edu.br.gesprev.dto.FichaFuncionalDTO;
import iff.edu.br.gesprev.dto.HoleriteDTO;
import iff.edu.br.gesprev.dto.ProventoDTO;
import iff.edu.br.gesprev.entity.Cts;
import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.FichaFinanceira;
import iff.edu.br.gesprev.entity.Folha;
import iff.edu.br.gesprev.entity.Holerite;
import iff.edu.br.gesprev.entity.Provento;
import iff.edu.br.gesprev.entity.Servidor;
import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.repository.CtsRepository;
import iff.edu.br.gesprev.repository.DocumentoRepository;
import iff.edu.br.gesprev.repository.FichaFinanceiraRepository;
import iff.edu.br.gesprev.repository.HoleriteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidarDocumentoService {

    private final DocumentoRepository documentoRepository;
    private final HoleriteRepository holeriteRepository;
    private final FichaFinanceiraRepository fichaFinanceiraRepository;
    private final CtsRepository ctsRepository;
    private final ChecklistDocumentoService checklistDocumentoService;
    private final ObjectMapper objectMapper;

    public ValidarDocumentoService(
            DocumentoRepository documentoRepository,
            HoleriteRepository holeriteRepository,
            FichaFinanceiraRepository fichaFinanceiraRepository,
            CtsRepository ctsRepository,
            ChecklistDocumentoService checklistDocumentoService) {
        this.documentoRepository = documentoRepository;
        this.holeriteRepository = holeriteRepository;
        this.fichaFinanceiraRepository = fichaFinanceiraRepository;
        this.ctsRepository = ctsRepository;
        this.checklistDocumentoService = checklistDocumentoService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public void confirmarDadosExtraidos(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento nao encontrado"));

        if (documento.getStatusVLM() != StatusVLM.PROCESSADO) {
            throw new RuntimeException("Documento precisa estar PROCESSADO antes de confirmar");
        }

        if (documento.getJsonExtraido() == null) {
            throw new RuntimeException("Documento nao possui JSON extraido");
        }

        try {
            Documento documentoValidado = switch (documento.getTipoDocumento()) {
                case FICHA_FUNCIONAL -> validarFichaFuncional(documento);
                case HOLERITE -> persistirHolerite(documento);
                case FICHA_FINANCEIRA -> persistirFichaFinanceira(documento);
                case CTS -> persistirCts(documento);
            };

            checklistDocumentoService.marcarDocumentoValidado(
                    documentoValidado.getProcesso().getId(),
                    documentoValidado.getTipoDocumento()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar dados extraidos: " + e.getMessage(), e);
        }
    }

    private Documento validarFichaFuncional(Documento documento) throws Exception {
        FichaFuncionalDTO dto = objectMapper.readValue(documento.getJsonExtraido(), FichaFuncionalDTO.class);
        Servidor servidor = documento.getProcesso().getServidor();
        if (preenchido(dto.nome())) {
            servidor.setNome(dto.nome());
        }
        if (dto.dtNascimento() != null) {
            servidor.setDtNascimento(dto.dtNascimento());
        }
        if (preenchido(dto.cpf())) {
            servidor.setCpf(dto.cpf());
        }
        if (preenchido(dto.pis())) {
            servidor.setPis(dto.pis());
        }
        if (dto.sexo() != null) {
            servidor.setSexo(dto.sexo());
        }
        if (preenchido(dto.email())) {
            servidor.setEmail(dto.email());
        }
        if (preenchido(dto.matricula())) {
            servidor.setMatricula(dto.matricula());
        }
        if (preenchido(dto.cargo())) {
            servidor.setCargo(dto.cargo());
        }
        if (preenchido(dto.orgao())) {
            servidor.setOrgao(dto.orgao());
        }
        if (dto.dtAdmissao() != null) {
            servidor.setDtAdmissao(dto.dtAdmissao());
        }

        documento.setStatusVLM(StatusVLM.VALIDADO);
        return documentoRepository.save(documento);
    }

    private boolean preenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private Holerite persistirHolerite(Documento documento) throws Exception {
        HoleriteDTO dto = objectMapper.readValue(documento.getJsonExtraido(), HoleriteDTO.class);
        if (dto.mesReferencia() == null || dto.mesReferencia().isBlank()) {
            throw new IllegalArgumentException("Informe a competencia do holerite antes de validar.");
        }
        if (dto.proventos() == null || dto.proventos().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um provento do holerite antes de validar.");
        }

        Holerite holerite = new Holerite(
                null,
                documento.getNomeArquivo(),
                documento.getCaminhoArquivo(),
                documento.getTipoDocumento(),
                StatusVLM.VALIDADO,
                documento.getDtUpload(),
                documento.getProcesso(),
                documento.getNomeOriginal(),
                documento.getJsonExtraido(),
                new ArrayList<>(),
                dto.mesReferencia()
        );

        List<Provento> proventos = new ArrayList<>();
        for (ProventoDTO proventoDTO : dto.proventos()) {
            if (proventoDTO.descricao() == null || proventoDTO.descricao().isBlank()
                    || proventoDTO.valor() == null) {
                throw new IllegalArgumentException("Revise os proventos do holerite: descricao e valor sao obrigatorios.");
            }
            Provento provento = new Provento();
            provento.setTipoProvento(proventoDTO.descricao());
            provento.setReferencia(proventoDTO.referencia() == null || proventoDTO.referencia().isBlank()
                    ? 0
                    : Double.parseDouble(proventoDTO.referencia().replace(",", ".")));
            provento.setValor(proventoDTO.valor());
            provento.setVencimento(proventoDTO.vencimento());
            proventos.add(provento);
        }

        holerite.setProventos(proventos);
        holerite.setValorTotalProventos(holerite.calcularValorTotalProventos());
        substituirDocumentoBase(documento);
        return holeriteRepository.save(holerite);
    }

    private FichaFinanceira persistirFichaFinanceira(Documento documento) throws Exception {
        FichaFinanceiraDTO dto = objectMapper.readValue(documento.getJsonExtraido(), FichaFinanceiraDTO.class);
        if (dto.folhas() == null || dto.folhas().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma folha da ficha financeira antes de validar.");
        }

        FichaFinanceira fichaFinanceira = new FichaFinanceira(
                null,
                documento.getNomeArquivo(),
                documento.getCaminhoArquivo(),
                documento.getTipoDocumento(),
                StatusVLM.VALIDADO,
                documento.getDtUpload(),
                documento.getProcesso(),
                documento.getNomeOriginal(),
                documento.getJsonExtraido(),
                new ArrayList<>()
        );

        List<Folha> folhas = new ArrayList<>();
        for (var folhaDTO : dto.folhas()) {
            Folha folha = new Folha();
            Integer anoReferencia = folhaDTO.anoReferencia() != null
                    ? folhaDTO.anoReferencia()
                    : dto.anoReferencia();
            if (anoReferencia == null) {
                throw new IllegalArgumentException("Ano de referencia nao informado para a folha " + folhaDTO.competencia());
            }
            if (folhaDTO.competencia() == null || folhaDTO.competencia().isBlank()
                    || folhaDTO.liquido() == null) {
                throw new IllegalArgumentException("Revise a folha da ficha financeira: competencia e liquido sao obrigatorios.");
            }
            folha.setAnoReferencia(anoReferencia);
            folha.setCompetencia(folhaDTO.competencia());
            folha.setVencimentos(folhaDTO.vencimentos());
            folha.setDescontos(folhaDTO.descontos());
            folha.setLiquido(folhaDTO.liquido());
            folhas.add(folha);
        }

        fichaFinanceira.setFolhas(folhas);
        substituirDocumentoBase(documento);
        return fichaFinanceiraRepository.save(fichaFinanceira);
    }

    private Cts persistirCts(Documento documento) throws Exception {
        CtsDTO dto = objectMapper.readValue(documento.getJsonExtraido(), CtsDTO.class);
        if (dto.inicioContribuicao() == null || dto.fimContribuicao() == null) {
            throw new IllegalArgumentException("Informe inicioContribuicao e fimContribuicao da CTS antes de validar. Use o formato dd/MM/yyyy.");
        }
        if (dto.totalDias() <= 0) {
            throw new IllegalArgumentException("Informe totalDias da CTS antes de validar.");
        }

        Cts cts = new Cts(
                null,
                documento.getNomeArquivo(),
                documento.getCaminhoArquivo(),
                documento.getTipoDocumento(),
                StatusVLM.VALIDADO,
                documento.getDtUpload(),
                documento.getProcesso(),
                documento.getNomeOriginal(),
                documento.getJsonExtraido(),
                dto.inicioContribuicao(),
                dto.fimContribuicao(),
                dto.tempoAverbacao(),
                dto.totalBruto(),
                dto.faltas(),
                dto.totalDias(),
                dto.tempoLegivel()
        );

        substituirDocumentoBase(documento);
        return ctsRepository.save(cts);
    }

    private void substituirDocumentoBase(Documento documento) {
        documentoRepository.delete(documento);
        documentoRepository.flush();
    }

    public LocalDateTime converterMesReferencia(String mesReferencia) {
        String[] partes = mesReferencia.split("/");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);
        return LocalDateTime.of(ano, mes, 1, 0, 0);
    }

    public String converterMesReferencia(LocalDateTime mesReferencia) {
        int mes = mesReferencia.getMonthValue();
        int ano = mesReferencia.getYear();
        return String.format("%02d/%d", mes, ano);
    }
}
