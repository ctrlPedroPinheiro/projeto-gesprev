package iff.edu.br.gesprev.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade que representa o fator de atualização para um determinado mês.
 */
@Entity
@Table(name = "fator_atualizacao")
public class FatorAtualizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate mesReferencia;

    @Column(nullable = false, precision = 15, scale = 10)
    private BigDecimal fator;

    public FatorAtualizacao() {}

    public FatorAtualizacao(LocalDate mesReferencia, BigDecimal fator) {
        this.mesReferencia = mesReferencia;
        this.fator = fator;
    }

    public Long getId() { return id; }
    public LocalDate getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(LocalDate mesReferencia) { this.mesReferencia = mesReferencia; }
    public BigDecimal getFator() { return fator; }
    public void setFator(BigDecimal fator) { this.fator = fator; }
}