package iff.edu.br.gesprev.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import iff.edu.br.gesprev.dto.ChecklistDocumentoDTO;
import iff.edu.br.gesprev.dto.DocumentoDTO;
import iff.edu.br.gesprev.entity.ChecklistDocumento;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import iff.edu.br.gesprev.repository.ChecklistDocumentoRepository;
import iff.edu.br.gesprev.security.UsuarioAutenticado;

import java.util.List;

@Service
public class ChecklistDocumentoService {
    private final ChecklistDocumentoRepository checklistDocumentoRepository;
    private final ProcessoAposentadoriaService processoAposentadoriaService;
    private final ProcessoFluxoService processoFluxoService;
    private final UsuarioAutenticado usuarioAutenticado;


    public ChecklistDocumentoService(ChecklistDocumentoRepository checklistDocumentoRepository, ProcessoAposentadoriaService processoAposentadoriaService, ProcessoFluxoService processoFluxoService, UsuarioAutenticado usuarioAutenticado) {
        this.checklistDocumentoRepository = checklistDocumentoRepository;
        this.processoAposentadoriaService = processoAposentadoriaService;
        this.processoFluxoService = processoFluxoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public ChecklistDocumentoDTO obterChecklistDocumentoPorId(Long id) {
        ChecklistDocumento checklistDocumento = checklistDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChecklistDocumento não encontrado"));
        return converterEntidade(checklistDocumento);
    }

    public List<ChecklistDocumentoDTO> obterChecklistDocumentosPorProcessoId(Long processoId) {
        return checklistDocumentoRepository.findByProcessoId(processoId)
                .stream()
                .map(this::converterEntidade)
                .collect(Collectors.toList());
    }

    public ChecklistDocumentoDTO salvarChecklistDocumento(ChecklistDocumentoDTO checklistDocumentoDTO) {
        ChecklistDocumento checklistDocumento = converterDTO(checklistDocumentoDTO);
        ChecklistDocumento checklistDocumentoSalvo = checklistDocumentoRepository.save(checklistDocumento);
        return converterEntidade(checklistDocumentoSalvo);
    }

    public ChecklistDocumentoDTO atualizarChecklistDocumento(Long id, ChecklistDocumentoDTO checklistDocumentoDTO) {
        ChecklistDocumento checklistDocumentoExistente = checklistDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChecklistDocumento não encontrado"));
        ChecklistDocumento checklistDocumentoAtualizado = converterDTO(checklistDocumentoDTO);
        checklistDocumentoAtualizado.setId(checklistDocumentoExistente.getId());
        ChecklistDocumento checklistDocumentoSalvo = checklistDocumentoRepository.save(checklistDocumentoAtualizado);
        
        processoFluxoService.verificarChecklistEAvancar(checklistDocumentoDTO.processoId(), usuarioAutenticado.obterUsuarioId());
        
        return converterEntidade(checklistDocumentoSalvo);
    }

    public void marcarDocumentoEntregue(Long processoId, TipoDocumento tipoDocumento) {
        ChecklistDocumento checklistDocumento = obterItemPorProcessoETipo(processoId, tipoDocumento);
        checklistDocumento.setEntregue(true);
        checklistDocumento.setObservacao("Documento anexado");
        checklistDocumentoRepository.save(checklistDocumento);
    }

    public void marcarDocumentoValidado(Long processoId, TipoDocumento tipoDocumento) {
        ChecklistDocumento checklistDocumento = obterItemPorProcessoETipo(processoId, tipoDocumento);
        checklistDocumento.setEntregue(true);
        checklistDocumento.setValido(true);
        checklistDocumento.setObservacao("Documento validado pelo analista");
        checklistDocumentoRepository.save(checklistDocumento);

        processoFluxoService.verificarChecklistEAvancar(processoId, usuarioAutenticado.obterUsuarioId());
    }

    public void deletarChecklistDocumento(Long id) {
        ChecklistDocumento checklistDocumento = checklistDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChecklistDocumento não encontrado"));
        checklistDocumentoRepository.delete(checklistDocumento);
    }

    public boolean documentoExiste(DocumentoDTO documentoDTO) {
        List<ChecklistDocumentoDTO> checklistDocumentos = obterChecklistDocumentosPorProcessoId(documentoDTO.processoId());
        for (ChecklistDocumentoDTO checklistDocumento : checklistDocumentos) {
            if (checklistDocumento.tipoDocumento().equals(documentoDTO.tipoDocumento())) {
                return true;
            }
        }
        return false;        
    }

    private ChecklistDocumento obterItemPorProcessoETipo(Long processoId, TipoDocumento tipoDocumento) {
        return checklistDocumentoRepository.findByProcessoIdAndTipoDocumento(processoId, tipoDocumento)
                .orElseThrow(() -> new RuntimeException("Item do checklist não encontrado para este documento"));
    }

    private ChecklistDocumentoDTO converterEntidade(ChecklistDocumento checklistDocumento) {
        return new ChecklistDocumentoDTO(
                checklistDocumento.getId(),
                checklistDocumento.getTipoDocumento(),
                checklistDocumento.isEntregue(),
                checklistDocumento.isValido(),
                checklistDocumento.getProcesso().getId(),
                checklistDocumento.getObservacao()
        );
    }

    private ChecklistDocumento converterDTO(ChecklistDocumentoDTO checklistDocumentoDTO) {
        ChecklistDocumento checklistDocumento = new ChecklistDocumento();
        checklistDocumento.setTipoDocumento(checklistDocumentoDTO.tipoDocumento());
        checklistDocumento.setEntregue(checklistDocumentoDTO.entregue());
        checklistDocumento.setValido(checklistDocumentoDTO.valido());
        checklistDocumento.setProcesso(processoAposentadoriaService.obterProcessoAposentadoriaEntidadePorId(checklistDocumentoDTO.processoId()));
        checklistDocumento.setObservacao(checklistDocumentoDTO.observacao());
        return checklistDocumento;
    }
}
