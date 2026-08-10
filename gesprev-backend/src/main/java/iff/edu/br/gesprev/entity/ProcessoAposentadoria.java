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
import jakarta.validation.constraints.NotNull;

/**
 * Entidade que representa um processo de aposentadoria.
 */
@Entity
@Table(name = "processo_aposentadoria")
public class ProcessoAposentadoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private int numeroProcesso;

    private LocalDateTime dtCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcesso status;

    private LocalDateTime dtAtualizacao;

    @ManyToOne
    @JoinColumn(name = "servidor_id", nullable = false)
    private Servidor servidor;

    public ProcessoAposentadoria() {
    }

    public ProcessoAposentadoria(Long id, int numeroProcesso, LocalDateTime dtCriacao, StatusProcesso status, LocalDateTime dtAtualizacao, Servidor servidor) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.dtCriacao = dtCriacao;
        this.status = status;
        this.dtAtualizacao = dtAtualizacao;
        this.servidor = servidor;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getNumeroProcesso() {
        return numeroProcesso;
    }
    public void setNumeroProcesso(int numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }
    public LocalDateTime getDtCriacao() {
        return dtCriacao;
    }
    public void setDtCriacao(LocalDateTime dtCriacao) {
        this.dtCriacao = dtCriacao;
    }
    public StatusProcesso getStatus() {
        return status;
    }
    public void setStatus(StatusProcesso status) {
        this.status = status;
    }
    public LocalDateTime getDtAtualizacao() {
        return dtAtualizacao;
    }
    public void setDtAtualizacao(LocalDateTime dtAtualizacao) {
        this.dtAtualizacao = dtAtualizacao;
    }
    public Servidor getServidor() {
        return servidor;
    }
    public void setServidor(Servidor servidor) {
        this.servidor = servidor;
    }
}
