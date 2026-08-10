package iff.edu.br.gesprev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entidade que representa a Certidão de Tempo de Serviço de um servidor.
 */
@Entity
@Table(name = "cts")
public class Cts extends Documento {
    
    @NotNull
    @Column(nullable = false)
    private LocalDate inicioContribuicao;

    @NotNull
    @Column(nullable = false)
    private LocalDate fimContribuicao;

    @NotNull
    @Column(nullable = false)
    private int tempoAverbacao;
    
    @Column(nullable = false)
    private int totalBruto;

    @NotNull
    @Column(nullable = false)
    private int faltas;
    
    @NotNull
    @Column(nullable = false)
    private int totalDias;

    private String tempoLegivel;

    public Cts() {
        super();
    }

    public Cts(Long id, String nomeArquivo, String caminhoArquivo, TipoDocumento tipoDocumento, StatusVLM statusVLM, LocalDateTime dtUpload, ProcessoAposentadoria processo, String nomeOriginal, String jsonExtraido, LocalDate inicioContribuicao, LocalDate fimContribuicao, int tempoAverbacao, int totalBruto, int faltas, int totalDias, String tempoLegivel) {
        super(id, nomeArquivo, caminhoArquivo, tipoDocumento, statusVLM, dtUpload, processo, nomeOriginal, jsonExtraido);
        this.inicioContribuicao = inicioContribuicao;
        this.fimContribuicao = fimContribuicao;
        this.tempoAverbacao = tempoAverbacao;
        this.totalBruto = totalBruto;
        this.faltas = faltas;
        this.totalDias = totalDias;
        this.tempoLegivel = tempoLegivel;
    }

    public LocalDate getInicioContribuicao() {
        return inicioContribuicao;
    }
    public void setInicioContribuicao(LocalDate inicioContribuicao) {
        this.inicioContribuicao = inicioContribuicao;
    }
    public LocalDate getFimContribuicao() {
        return fimContribuicao;
    }
    public void setFimContribuicao(LocalDate fimContribuicao) {
        this.fimContribuicao = fimContribuicao;
    }
    public int getTempoAverbacao() {
        return tempoAverbacao;
    }
    public void setTempoAverbacao(int tempoAverbacao) {
        this.tempoAverbacao = tempoAverbacao;
    }
    public int getTotalBruto() {
        return totalBruto;
    }
    public void setTotalBruto(int totalBruto) {
        this.totalBruto = totalBruto;
    }
    public int getFaltas() {
        return faltas;
    }
    public void setFaltas(int faltas) {
        this.faltas = faltas;
    }
    public int getTotalDias() {
        return totalDias;
    }
    public void setTotalDias(int totalDias) {
        this.totalDias = totalDias;
    }
    public String getTempoLegivel() {
        return tempoLegivel;
    }
    public void setTempoLegivel(String tempoLegivel) {
        this.tempoLegivel = tempoLegivel;
    }
}
