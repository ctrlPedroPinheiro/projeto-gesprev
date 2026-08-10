package iff.edu.br.gesprev.entity;

import java.time.LocalDateTime;

import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidade que representa o histórico de alterações de status de um processo de aposentadoria.
 */
@Entity
@Table(name = "historico_processo")
public class HistoricoProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcesso statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcesso statusAtual;

    @Column(nullable = false)
    private LocalDateTime dtAlteracao;

    @NotBlank
    @Column(nullable = false)
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoAposentadoria processo;

    public HistoricoProcesso() {
    }

    public HistoricoProcesso(Long id, StatusProcesso statusAnterior, StatusProcesso statusAtual, LocalDateTime dtAlteracao, String observacao, Usuario usuario, ProcessoAposentadoria processo) {
        this.id = id;
        this.statusAnterior = statusAnterior;
        this.statusAtual = statusAtual;
        this.dtAlteracao = dtAlteracao;
        this.observacao = observacao;
        this.usuario = usuario;
        this.processo = processo;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public StatusProcesso getStatusAnterior() {
        return statusAnterior;
    }
    public void setStatusAnterior(StatusProcesso statusAnterior) {
        this.statusAnterior = statusAnterior;
    }
    public StatusProcesso getStatusAtual() {
        return statusAtual;
    }
    public void setStatusAtual(StatusProcesso statusAtual) {
        this.statusAtual = statusAtual;
    }
    public LocalDateTime getDtAlteracao() {
        return dtAlteracao;
    }
    public void setDtAlteracao(LocalDateTime dtAlteracao) {
        this.dtAlteracao = dtAlteracao;
    }
    public String getObservacao() {
        return observacao;
    }
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public ProcessoAposentadoria getProcesso() {
        return processo;
    }
    public void setProcesso(ProcessoAposentadoria processo) {
        this.processo = processo;
    }
}
