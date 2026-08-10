package iff.edu.br.gesprev.service;

import iff.edu.br.gesprev.entity.HistoricoProcesso;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.Usuario;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import iff.edu.br.gesprev.repository.ChecklistDocumentoRepository;
import iff.edu.br.gesprev.repository.HistoricoProcessoRepository;
import iff.edu.br.gesprev.repository.ProcessoAposentadoriaRepository;
import iff.edu.br.gesprev.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProcessoFluxoService {

    private final ProcessoAposentadoriaRepository processoRepository;
    private final ChecklistDocumentoRepository checklistRepository;
    private final HistoricoProcessoRepository historicoRepository;
    private final UsuarioService usuarioService;
    private final UsuarioAutenticado usuarioAutenticado;

    public ProcessoFluxoService(ProcessoAposentadoriaRepository processoRepository, ChecklistDocumentoRepository checklistRepository, HistoricoProcessoRepository historicoRepository, UsuarioService usuarioService, UsuarioAutenticado usuarioAutenticado) {
        this.processoRepository = processoRepository;
        this.checklistRepository = checklistRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioService = usuarioService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional
    public void avancarParaPendenteDocumento(Long processoId, Long usuarioId) {
        ProcessoAposentadoria processo = obterProcesso(processoId);

        if (processo.getStatus() != StatusProcesso.CADASTRADO) return;

        atualizarStatus(processo, StatusProcesso.PENDENTE_DOCUMENTO, usuarioId, "Documentos sendo anexados ao processo");
    }

    @Transactional
    public void verificarChecklistEAvancar(Long processoId, Long usuarioId) {
        ProcessoAposentadoria processo = obterProcesso(processoId);

        if (processo.getStatus() != StatusProcesso.PENDENTE_DOCUMENTO) return;

        boolean checklistCompleto = checklistRepository.findByProcessoId(processoId)
                .stream()
                .allMatch(c -> c.isEntregue() && c.isValido());

        if (checklistCompleto) {
            atualizarStatus(processo, StatusProcesso.EM_ANALISE, usuarioId, "Checklist completo — todos os documentos entregues e válidos");
        }
    }

    @Transactional
    public void avancarParaEmCalculo(Long processoId, Long usuarioId) {
        ProcessoAposentadoria processo = obterProcesso(processoId);

        if (processo.getStatus() != StatusProcesso.EM_ANALISE) {
            throw new RuntimeException("Processo precisa estar EM_ANALISE para avançar para EM_CALCULO");
        }

        atualizarStatus(processo, StatusProcesso.EM_CALCULO, usuarioId, "Memória de cálculo gerada");
    }

    @Transactional
    public void finalizarPorAto(Long processoId, Long usuarioId) {
        ProcessoAposentadoria processo = obterProcesso(processoId);

        if (processo.getStatus() != StatusProcesso.EM_CALCULO) {
            throw new RuntimeException("Processo precisa estar EM_CALCULO para gerar o Ato de Aposentadoria");
        }

        atualizarStatus(processo, StatusProcesso.FINALIZADO,
                usuarioId, "Ato de Aposentadoria gerado");
    }

    @Transactional
    public void rejeitar(Long processoId, String observacao) {
        ProcessoAposentadoria processo = obterProcesso(processoId);

        if (processo.getStatus() != StatusProcesso.EM_CALCULO) {
            throw new RuntimeException("Processo precisa estar EM_CALCULO para ser rejeitado");
        }

        atualizarStatus(processo, StatusProcesso.REJEITADO,
                usuarioAutenticado.obterUsuarioId(), observacao);
    }

    @Transactional
    public void reabrir(Long processoId, String observacao) {
        ProcessoAposentadoria processo = obterProcesso(processoId);

        if (processo.getStatus() != StatusProcesso.REJEITADO) {
            throw new RuntimeException("Apenas processos REJEITADOS podem ser reabertos");
        }

        atualizarStatus(processo, StatusProcesso.PENDENTE_DOCUMENTO,
                usuarioAutenticado.obterUsuarioId(), "Processo reaberto: " + observacao);
    }

    private void atualizarStatus(ProcessoAposentadoria processo,
            StatusProcesso novoStatus, Long usuarioId, String observacao) {

        StatusProcesso statusAnterior = processo.getStatus();
        processo.setStatus(novoStatus);
        processo.setDtAtualizacao(LocalDateTime.now());
        processoRepository.save(processo);

        Usuario usuario = usuarioService.obterUsuarioEntidadePorId(usuarioId);
        HistoricoProcesso historico = new HistoricoProcesso(
                null,
                statusAnterior,
                novoStatus,
                LocalDateTime.now(),
                observacao,
                usuario,
                processo
        );
        historicoRepository.save(historico);
    }

    private ProcessoAposentadoria obterProcesso(Long processoId) {
        return processoRepository.findById(processoId).orElseThrow(() -> new RuntimeException("Processo não encontrado"));
    }
}
