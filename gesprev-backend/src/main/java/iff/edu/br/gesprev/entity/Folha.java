package iff.edu.br.gesprev.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Classe que representa a entidade Folha, mapeada para a tabela "folha" no banco de dados.
 * Contém informações sobre a folha de pagamento, como competência, vencimentos, descontos e valor líquido.
 */
@Entity
@Table(name = "folha")
public class Folha {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int anoReferencia;
    private String competencia;
    private BigDecimal vencimentos;
    private BigDecimal descontos;
    private BigDecimal liquido;

    public Folha() {

    }

    public Folha(Long id, int anoReferencia, String competencia, BigDecimal vencimentos, BigDecimal descontos, BigDecimal liquido) {
        this.id = id;
        this.anoReferencia = anoReferencia;
        this.competencia = competencia;
        this.vencimentos = vencimentos;
        this.descontos = descontos;
        this.liquido = liquido;
    }
    public int getAnoReferencia() {
        return anoReferencia;
    }
    public void setAnoReferencia(int anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCompetencia() {
        return competencia;
    }
    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }
    public BigDecimal getVencimentos() {
        return vencimentos;
    }
    public void setVencimentos(BigDecimal vencimentos) {
        this.vencimentos = vencimentos;
    }
    public BigDecimal getDescontos() {
        return descontos;
    }
    public void setDescontos(BigDecimal descontos) {
        this.descontos = descontos;
    }
    public BigDecimal getLiquido() {
        return liquido;
    }
    public void setLiquido(BigDecimal liquido) {
        this.liquido = liquido;
    }
}
