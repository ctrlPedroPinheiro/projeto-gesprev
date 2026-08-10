package iff.edu.br.gesprev.entity;

import java.math.BigDecimal;

import iff.edu.br.gesprev.entity.enums.TipoCalculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/**
 * Entidade que representa a memória de cálculo para um determinado processo de aposentadoria.
 */
@Entity
@Table(name = "memoria_calculo")
public class MemoriaCalculo {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private BigDecimal mediaAritmetica;

    @NotNull
    @Column(nullable = false)
    private BigDecimal valorBeneficio;

    @NotNull
    @Column(nullable = false)
    private BigDecimal proporcionalidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCalculo tipoCalculo;

    @OneToOne
    @JoinColumn(name = "processo_id", nullable = false, unique = true)
    private ProcessoAposentadoria processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holerite_id")
    private Holerite holerite;

    public MemoriaCalculo() {
    }

    public MemoriaCalculo(Long id, BigDecimal mediaAritmetica, BigDecimal valorBeneficio, BigDecimal proporcionalidade, TipoCalculo tipoCalculo, ProcessoAposentadoria processo) {
        this.id = id;
        this.mediaAritmetica = mediaAritmetica;
        this.valorBeneficio = valorBeneficio;
        this.proporcionalidade = proporcionalidade;
        this.tipoCalculo = tipoCalculo;
        this.processo = processo;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public BigDecimal getMediaAritmetica() {
        return mediaAritmetica;
    }
    public void setMediaAritmetica(BigDecimal mediaAritmetica) {
        this.mediaAritmetica = mediaAritmetica;
    }
    public BigDecimal getValorBeneficio() {
        return valorBeneficio;
    }
    public void setValorBeneficio(BigDecimal valorBeneficio) {
        this.valorBeneficio = valorBeneficio;
    }
    public BigDecimal getProporcionalidade() {
        return proporcionalidade;
    }
    public void setProporcionalidade(BigDecimal proporcionalidade) {
        this.proporcionalidade = proporcionalidade;
    }
    public TipoCalculo getTipoCalculo() {
        return tipoCalculo;
    }
    public void setTipoCalculo(TipoCalculo tipoCalculo) {
        this.tipoCalculo = tipoCalculo;
    }
    public ProcessoAposentadoria getProcesso() {
        return processo;
    }
    public void setProcesso(ProcessoAposentadoria processo) {
        this.processo = processo;
    }
    public Holerite getHolerite() {
        return holerite;
    }
    public void setHolerite(Holerite holerite) {
        this.holerite = holerite;
    }
}
