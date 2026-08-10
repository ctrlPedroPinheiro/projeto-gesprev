package iff.edu.br.gesprev.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Classe que representa um Holerite, que é um tipo específico de Documento.
 * Contém uma lista de Proventos e o valor total dos proventos, além do mês de referência.
 */
@Entity
@Table(name = "holerite")
public class Holerite extends Documento {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "holerite_id")
    public List<Provento> proventos;
    public BigDecimal valorTotalProventos;
    public String mesReferencia;

    public Holerite() {
        super();
        this.valorTotalProventos = BigDecimal.ZERO;
    }

    public Holerite(Long id, String nomeArquivo, String caminhoArquivo, TipoDocumento tipoDocumento, StatusVLM statusVLM, LocalDateTime dtUpload, ProcessoAposentadoria processo, String nomeOriginal, String jsonExtraido, List<Provento> proventos, String mesReferencia) {
        super(id, nomeArquivo, caminhoArquivo, tipoDocumento, statusVLM, dtUpload, processo, nomeOriginal, jsonExtraido);
        this.mesReferencia = mesReferencia;
        this.proventos = proventos;
        this.valorTotalProventos = BigDecimal.ZERO;
    }

    public BigDecimal calcularValorTotalProventos() {
        if (proventos == null || proventos.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return proventos.stream()
                .filter(Objects::nonNull)
                .filter(Provento::isVencimento)
                .map(Provento::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Provento> getProventos() {
        return proventos;
    }
    public void setProventos(List<Provento> proventos) {
        this.proventos = proventos;
    }
    public BigDecimal getValorTotalProventos() {
        return valorTotalProventos;
    }
    public void setValorTotalProventos(BigDecimal valorTotalProventos) {
        this.valorTotalProventos = valorTotalProventos;
    }
    public String getMesReferencia() {
        return mesReferencia;
    }
    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }
}
