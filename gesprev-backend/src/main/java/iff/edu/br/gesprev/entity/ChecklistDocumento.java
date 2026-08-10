package iff.edu.br.gesprev.entity;

import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;

/**
 * Entidade que representa um item do checklist de documentos para um processo de aposentadoria.
 */
@Entity
@Table(name = "checklist_documento")
public class ChecklistDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false)
    private boolean entregue;

    @Column(nullable = false)
    private boolean valido;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoAposentadoria processo;

    @NotBlank
    @Column(nullable = false)
    private String observacao;

    public ChecklistDocumento() {
    }

    public ChecklistDocumento(Long id, TipoDocumento tipoDocumento, boolean entregue, boolean valido, ProcessoAposentadoria processo, String observacao) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.entregue = entregue;
        this.valido = valido;
        this.processo = processo;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public boolean isEntregue() {
        return entregue;
    }
    public void setEntregue(boolean entregue) {
        this.entregue = entregue;
    }
    public boolean isValido() {
        return valido;
    }
    public void setValido(boolean valido) {
        this.valido = valido;
    }
    public ProcessoAposentadoria getProcesso() {
        return processo;
    }
    public void setProcesso(ProcessoAposentadoria processo) {
        this.processo = processo;
    }
    public String getObservacao() {
        return observacao;
    }
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
