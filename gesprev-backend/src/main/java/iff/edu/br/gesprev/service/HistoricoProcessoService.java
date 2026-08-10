package iff.edu.br.gesprev.service;

import org.springframework.stereotype.Service;

import iff.edu.br.gesprev.dto.HistoricoProcessoDTO;
import iff.edu.br.gesprev.entity.HistoricoProcesso;
import iff.edu.br.gesprev.repository.HistoricoProcessoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoProcessoService {

    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final UsuarioService usuarioService;
    private final ProcessoAposentadoriaService processoAposentadoriaService;

    public HistoricoProcessoService(HistoricoProcessoRepository historicoProcessoRepository,
            UsuarioService usuarioService,
            ProcessoAposentadoriaService processoAposentadoriaService) {
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.usuarioService = usuarioService;
        this.processoAposentadoriaService = processoAposentadoriaService;
    }

    public HistoricoProcessoDTO obterHistoricoPorId(Long id) {
        HistoricoProcesso historico = historicoProcessoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));
        return converterEntidade(historico);
    }

    public List<HistoricoProcessoDTO> obterHistoricosPorProcessoId(Long processoId) {
        return historicoProcessoRepository.findByProcessoId(processoId)
                .stream()
                .map(this::converterEntidade)
                .collect(Collectors.toList());
    }

    public HistoricoProcessoDTO criarHistorico(HistoricoProcessoDTO historicoDTO) {
        HistoricoProcesso historico = converterDTO(historicoDTO);
        HistoricoProcesso historicoSalvo = historicoProcessoRepository.save(historico);
        return converterEntidade(historicoSalvo);
    }

    public HistoricoProcessoDTO atualizarHistorico(Long id, HistoricoProcessoDTO historicoDTO) {
        HistoricoProcesso historicoExistente = historicoProcessoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        historicoExistente.setStatusAnterior(historicoDTO.statusAnterior());
        historicoExistente.setStatusAtual(historicoDTO.statusAtual());
        historicoExistente.setDtAlteracao(historicoDTO.dtAlteracao());
        historicoExistente.setObservacao(historicoDTO.observacao());
        historicoExistente.setUsuario(usuarioService.obterUsuarioEntidadePorId(historicoDTO.usuarioId()));
        historicoExistente.setProcesso(processoAposentadoriaService.obterProcessoAposentadoriaEntidadePorId(historicoDTO.processoId()));

        HistoricoProcesso historicoAtualizado = historicoProcessoRepository.save(historicoExistente);
        return converterEntidade(historicoAtualizado);
    }

    public void deletarHistorico(Long id) {
        if (!historicoProcessoRepository.existsById(id)) {
            throw new RuntimeException("Histórico não encontrado");
        }
        historicoProcessoRepository.deleteById(id);
    }

    public HistoricoProcessoDTO converterEntidade(HistoricoProcesso historico) {
        return new HistoricoProcessoDTO(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusAtual(),
                historico.getDtAlteracao(),
                historico.getObservacao(),
                historico.getUsuario().getId(),
                historico.getProcesso().getId()
        );
    }

    public HistoricoProcesso converterDTO(HistoricoProcessoDTO historicoDTO) {
        return new HistoricoProcesso(
                historicoDTO.id(),
                historicoDTO.statusAnterior(),
                historicoDTO.statusAtual(),
                historicoDTO.dtAlteracao(),
                historicoDTO.observacao(),
                usuarioService.obterUsuarioEntidadePorId(historicoDTO.usuarioId()),
                processoAposentadoriaService.obterProcessoAposentadoriaEntidadePorId(historicoDTO.processoId())
        );
    }
}