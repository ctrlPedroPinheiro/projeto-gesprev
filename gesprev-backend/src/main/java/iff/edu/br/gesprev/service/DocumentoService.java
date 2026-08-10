package iff.edu.br.gesprev.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import iff.edu.br.gesprev.dto.DocumentoDTO;
import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import iff.edu.br.gesprev.repository.DocumentoRepository;
import iff.edu.br.gesprev.security.UsuarioAutenticado;

@Service
public class DocumentoService {

    public final DocumentoRepository documentoRepository;
    public final ProcessoAposentadoriaService processoAposentadoriaService;
    public final ChecklistDocumentoService checklistDocumentoService;
    private final ProcessoFluxoService processoFluxoService;
    private final UsuarioAutenticado usuarioAutenticado;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public DocumentoService(DocumentoRepository documentoRepository, ProcessoAposentadoriaService processoAposentadoriaService, ChecklistDocumentoService checklistDocumentoService, ProcessoFluxoService processoFluxoService, UsuarioAutenticado usuarioAutenticado, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.usuarioAutenticado = usuarioAutenticado;
        this.documentoRepository = documentoRepository;
        this.processoAposentadoriaService = processoAposentadoriaService;
        this.checklistDocumentoService = checklistDocumentoService;
        this.processoFluxoService = processoFluxoService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    public DocumentoDTO obterDocumentoPorId(Long id) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        return converterEntidade(documento);
    }

    public Documento obterDocumentoEntidadePorId(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
    }

    public List<DocumentoDTO> obterDocumentosPorProcessoId(Long processoId) {
        return documentoRepository.findByProcessoId(processoId)
                .stream()
                .map(this::converterEntidade)
                .collect(Collectors.toList());
    }

    public DocumentoDTO salvarDocumento(DocumentoDTO documentoDTO) {
        if (documentoRepository.existsByProcessoIdAndTipoDocumento(documentoDTO.processoId(), documentoDTO.tipoDocumento())) {
            throw new RuntimeException("Documento já existe para este processo");
        }
        Documento documento = converterDTO(documentoDTO);
        Documento documentoSalvo = documentoRepository.save(documento);
        checklistDocumentoService.marcarDocumentoEntregue(documentoDTO.processoId(), documentoDTO.tipoDocumento());
        processoFluxoService.avancarParaPendenteDocumento(documentoDTO.processoId(), usuarioAutenticado.obterUsuarioId());
        return converterEntidade(documentoSalvo);
    }

    public DocumentoDTO atualizarDocumento(Long id, DocumentoDTO documentoDTO) {
        Documento documentoExistente = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        Documento documentoAtualizado = converterDTO(documentoDTO);
        documentoAtualizado.setId(documentoExistente.getId());
        Documento documentoSalvo = documentoRepository.save(documentoAtualizado);
        return converterEntidade(documentoSalvo);
    }

    @Transactional
    public DocumentoDTO atualizarJsonExtraido(Long id, String jsonExtraido) throws Exception {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento nao encontrado"));
        String jsonNormalizado = objectMapper.writeValueAsString(objectMapper.readTree(jsonExtraido));
        documento.setJsonExtraido(jsonNormalizado);
        if (documento.getStatusVLM() == StatusVLM.PENDENTE || documento.getStatusVLM() == StatusVLM.ERRO) {
            documento.setStatusVLM(StatusVLM.PROCESSADO);
        }
        return converterEntidade(documentoRepository.save(documento));
    }

    @Transactional
    public void deletarDocumento(Long id) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        String caminho = documento.getCaminhoArquivo();
        documentoRepository.delete(documento);
        documentoRepository.flush();
        fileStorageService.deletarArquivosAposCommit(List.of(caminho));
    }

    public DocumentoDTO salvarComArquivo(MultipartFile arquivo, Long processoId, String tipoDocumento) throws Exception {
        String caminho = fileStorageService.salvarArquivo(arquivo, processoId);
        String nomeArquivo = Paths.get(caminho).getFileName().toString();

        DocumentoDTO dto = new DocumentoDTO(
                null,
                nomeArquivo,
                caminho,
                TipoDocumento.valueOf(tipoDocumento),
                StatusVLM.PENDENTE,
                java.time.LocalDateTime.now(),
                processoId,
                arquivo.getOriginalFilename(),
                null
        );

        return salvarDocumento(dto);
    }

    public byte[] baixarArquivo(Long id) throws Exception {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        return fileStorageService.lerArquivo(documento.getCaminhoArquivo());
    }

    public DocumentoDTO converterEntidade(Documento documento) {
        return new DocumentoDTO(
            documento.getId(),
            documento.getNomeArquivo(),
            documento.getCaminhoArquivo(),
            documento.getTipoDocumento(),
            documento.getStatusVLM(),
            documento.getDtUpload(),
            documento.getProcesso().getId(),
            documento.getNomeOriginal(),
            documento.getJsonExtraido()
        );
    }

    public Documento converterDTO(DocumentoDTO documentoDTO) {
        return new Documento(
            documentoDTO.id(),
            documentoDTO.nomeArquivo(),
            documentoDTO.caminhoArquivo(),
            documentoDTO.tipoDocumento(),
            documentoDTO.statusVLM(),
            documentoDTO.dtUpload(),
            processoAposentadoriaService.obterProcessoAposentadoriaEntidadePorId(documentoDTO.processoId()),
            documentoDTO.nomeOriginal(),
            documentoDTO.jsonExtraido()
        );
    }
}
