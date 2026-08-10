package iff.edu.br.gesprev.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade que representa um provento associado a um processo de aposentadoria.
 */
@Entity
@Table(name = "provento")
public class Provento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipoProvento;

    @Column(nullable = false)
    private double referencia;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private boolean vencimento;

    public Provento() {
    }
    
    public Provento(String tipoProvento, double referencia, BigDecimal valor, boolean vencimento) {
        this.tipoProvento = tipoProvento;
        this.referencia = referencia;
        this.valor = valor;
        this.vencimento = vencimento;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTipoProvento() {
        return tipoProvento;
    }
    public void setTipoProvento(String tipoProvento) {
        this.tipoProvento = tipoProvento;
    }
    public double getReferencia() {
        return referencia;
    }
    public void setReferencia(double referencia) {
        this.referencia = referencia;
    }
    public BigDecimal getValor() {
        return valor;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    public boolean isVencimento() {
        return vencimento;
    }
    public void setVencimento(boolean vencimento) {
        this.vencimento = vencimento;
    }
}
