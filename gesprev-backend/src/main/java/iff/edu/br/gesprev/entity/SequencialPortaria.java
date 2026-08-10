package iff.edu.br.gesprev.entity;

import jakarta.persistence.*;

/**
 * Entidade que representa o sequencial de portaria para um determinado ano.
 * Utilizada para gerar o número da portaria de aposentadoria de forma sequencial.
 */
@Entity
@Table(name = "sequencial_portaria")
public class SequencialPortaria {

    @Id
    private int ano;

    @Column(nullable = false)
    private int ultimo;

    public SequencialPortaria() {}

    public SequencialPortaria(int ano, int ultimo) {
        this.ano = ano;
        this.ultimo = ultimo;
    }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public int getUltimo() { return ultimo; }
    public void setUltimo(int ultimo) { this.ultimo = ultimo; }
}