package iff.edu.br.gesprev.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import iff.edu.br.gesprev.dto.CriarProcessoComServidorDTO;
import iff.edu.br.gesprev.dto.ProcessoAposentadoriaDTO;
import iff.edu.br.gesprev.entity.ChecklistDocumento;
import iff.edu.br.gesprev.entity.HistoricoProcesso;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.Servidor;
import iff.edu.br.gesprev.entity.Usuario;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import iff.edu.br.gesprev.repository.ProcessoAposentadoriaRepository;
import iff.edu.br.gesprev.specification.ProcessoAposentadoriaSpecification;
import jakarta.transaction.Transactional;
import iff.edu.br.gesprev.repository.ChecklistDocumentoRepository;
import iff.edu.br.gesprev.repository.HistoricoProcessoRepository;
import iff.edu.br.gesprev.repository.ServidorRepository;
import iff.edu.br.gesprev.repository.UsuarioRepository;
import iff.edu.br.gesprev.repository.DocumentoRepository;

@Service
public class ProcessoAposentadoriaService {

    public final ProcessoAposentadoriaRepository processoAposentadoriaRepository;
    public final ServidorService servidorService;
    public final ServidorRepository servidorRepository;
    public final ChecklistDocumentoRepository checklistDocumentoRepository;
    public final HistoricoProcessoRepository historicoProcessoRepository;
    public final UsuarioRepository usuarioRepository;
    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;

    public ProcessoAposentadoriaService(ProcessoAposentadoriaRepository processoAposentadoriaRepository, ServidorService servidorService, ServidorRepository servidorRepository, ChecklistDocumentoRepository checklistDocumentoRepository, HistoricoProcessoRepository historicoProcessoRepository, UsuarioRepository usuarioRepository, DocumentoRepository documentoRepository, FileStorageService fileStorageService) {
        this.processoAposentadoriaRepository = processoAposentadoriaRepository;
        this.servidorService = servidorService;
        this.servidorRepository = servidorRepository;
        this.checklistDocumentoRepository = checklistDocumentoRepository;
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentoRepository = documentoRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ProcessoAposentadoriaDTO criarProcessoComServidor(CriarProcessoComServidorDTO dto, Long usuarioId) {
        Servidor servidor = servidorRepository.findByCpf(dto.cpf());
        if (servidor == null) {
            servidor = new Servidor(
                null,
                dto.nome(),
                dto.dtNascimento(),
                dto.cpf(),
                dto.pis(),
                dto.sexo(),
                dto.email(),
                dto.matricula(),
                dto.cargo(),
                dto.orgao(),
                dto.dtAdmissao()
            );
            servidor = servidorRepository.save(servidor);
        }

        if (processoAposentadoriaRepository.findByNumeroProcesso(dto.numeroProcesso()) != null) {
            throw new RuntimeException("Número de processo já cadastrado");
        }

        ProcessoAposentadoria processo = new ProcessoAposentadoria(
            null,
            dto.numeroProcesso(),
            LocalDateTime.now(),
            StatusProcesso.CADASTRADO,
            LocalDateTime.now(),
            servidor
        );
        ProcessoAposentadoria processoSalvo = processoAposentadoriaRepository.save(processo);

        for (TipoDocumento tipo : TipoDocumento.values()) {
            ChecklistDocumento item = new ChecklistDocumento(
                null,
                tipo,
                false,
                false,
                processoSalvo,
                "Pendente"
            );
            checklistDocumentoRepository.save(item);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        HistoricoProcesso historico = new HistoricoProcesso(
            null,
            StatusProcesso.CADASTRADO,
            StatusProcesso.CADASTRADO,
            LocalDateTime.now(),
            "Processo criado",
            usuario,
            processoSalvo
        );
        historicoProcessoRepository.save(historico);

        return converterEntidade(processoSalvo);
    }

    public ProcessoAposentadoriaDTO obterProcessoAposentadoriaPorId(Long id) {
        ProcessoAposentadoria processoAposentadoria = processoAposentadoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de aposentadoria não encontrado"));
        return converterEntidade(processoAposentadoria);
    }

    public ProcessoAposentadoria obterProcessoAposentadoriaEntidadePorId(Long id) {
        return processoAposentadoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de aposentadoria não encontrado"));
    }

    public ProcessoAposentadoria obterProcessoAposentadoriaEntidadePorNumero(int numeroProcesso) {
        ProcessoAposentadoria processo = processoAposentadoriaRepository.findByNumeroProcesso(numeroProcesso);
        if (processo == null) {
            throw new RuntimeException("Processo de aposentadoria nao encontrado");
        }
        return processo;
    }

    public ProcessoAposentadoriaDTO atualizarProcessoAposentadoria(Long id, ProcessoAposentadoriaDTO processoAposentadoriaDTO) {
        ProcessoAposentadoria processoExistente = processoAposentadoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de aposentadoria não encontrado"));

        if (processoExistente.getNumeroProcesso() != processoAposentadoriaDTO.numeroProcesso()) {
            if (processoAposentadoriaRepository.findByNumeroProcesso(processoAposentadoriaDTO.numeroProcesso()) != null) {
                throw new RuntimeException("Número de processo já cadastrado");
            }
        }

        processoExistente.setNumeroProcesso(processoAposentadoriaDTO.numeroProcesso());
        processoExistente.setDtCriacao(processoAposentadoriaDTO.dtCriacao());
        processoExistente.setStatus(processoAposentadoriaDTO.status());
        processoExistente.setDtAtualizacao(processoAposentadoriaDTO.dtAtualizacao());
        processoExistente.setServidor(servidorService.obterServidorEntidadePorId(processoAposentadoriaDTO.servidorId()));

        ProcessoAposentadoria processoAtualizado = processoAposentadoriaRepository.save(processoExistente);
        return converterEntidade(processoAtualizado);
    }

    @Transactional
    public void deletarProcessoAposentadoria(Long id) {
        if (!processoAposentadoriaRepository.existsById(id)) {
            throw new RuntimeException("Processo de aposentadoria não encontrado");
        }
        List<String> caminhos = documentoRepository.findCaminhosByProcessoId(id);
        processoAposentadoriaRepository.deleteById(id);
        processoAposentadoriaRepository.flush();
        fileStorageService.deletarArquivosAposCommit(caminhos);
    }

    public List<ProcessoAposentadoriaDTO> buscarProcessos(
        Integer numeroProcesso,
        StatusProcesso status,
        LocalDateTime dtCriacaoInicio,
        LocalDateTime dtCriacaoFim,
        String nomeServidor,
        String cpfServidor) {

        Specification<ProcessoAposentadoria> spec = ProcessoAposentadoriaSpecification.comFiltros(
                numeroProcesso, status, dtCriacaoInicio, dtCriacaoFim, nomeServidor, cpfServidor);

        return processoAposentadoriaRepository.findAll(spec)
                .stream()
                .map(this::converterEntidade)
                .collect(Collectors.toList());
    }

    public long contarPorStatus(StatusProcesso status) {
        return processoAposentadoriaRepository.countByStatus(status);
    }

    public long contarTotal() {
        return processoAposentadoriaRepository.count();
    }

    public ProcessoAposentadoriaDTO converterEntidade(ProcessoAposentadoria processoAposentadoria) {
        return new ProcessoAposentadoriaDTO(
                processoAposentadoria.getId(),
                processoAposentadoria.getNumeroProcesso(),
                processoAposentadoria.getDtCriacao(),
                processoAposentadoria.getStatus(),
                processoAposentadoria.getDtAtualizacao(),
                processoAposentadoria.getServidor().getId()
        );
    }

    public ProcessoAposentadoria converterDTO(ProcessoAposentadoriaDTO processoAposentadoriaDTO) {
        ProcessoAposentadoria processoAposentadoria = new ProcessoAposentadoria();
        processoAposentadoria.setId(processoAposentadoriaDTO.id());
        processoAposentadoria.setNumeroProcesso(processoAposentadoriaDTO.numeroProcesso());
        processoAposentadoria.setDtCriacao(processoAposentadoriaDTO.dtCriacao());
        processoAposentadoria.setStatus(processoAposentadoriaDTO.status());
        processoAposentadoria.setDtAtualizacao(processoAposentadoriaDTO.dtAtualizacao());
        processoAposentadoria.setServidor(servidorService.obterServidorEntidadePorId(processoAposentadoriaDTO.servidorId()));
        return processoAposentadoria;
    }
}
