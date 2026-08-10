package iff.edu.br.gesprev.entity;

import java.time.LocalDateTime;

import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidade base que representa um documento relacionado a um processo de aposentadoria.
 */
@Entity
@Table(name = "documento")
@Inheritance(strategy = InheritanceType.JOINED)
public class Documento {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nomeArquivo;

    @NotBlank
    @Column(nullable = false)
    private String caminhoArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_vlm", nullable = false)
    private StatusVLM statusVLM;

    @Column(nullable = false)
    private LocalDateTime dtUpload;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoAposentadoria processo;

    @NotBlank
    @Column(nullable = false)
    private String nomeOriginal;

    @Column(columnDefinition = "TEXT")
    private String jsonExtraido;

    public Documento() {
    }

    public Documento(Long id, String nomeArquivo, String caminhoArquivo, TipoDocumento tipoDocumento, StatusVLM statusVLM, LocalDateTime dtUpload, ProcessoAposentadoria processo, String nomeOriginal, String jsonExtraido) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.tipoDocumento = tipoDocumento;
        this.statusVLM = statusVLM;
        this.dtUpload = dtUpload;
        this.processo = processo;
        this.nomeOriginal = nomeOriginal;
        this.jsonExtraido = jsonExtraido;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNomeArquivo() {
        return nomeArquivo;
    }
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }
    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public StatusVLM getStatusVLM() {
        return statusVLM;
    }
    public void setStatusVLM(StatusVLM statusVLM) {
        this.statusVLM = statusVLM;
    }
    public LocalDateTime getDtUpload() {
        return dtUpload;
    }
    public void setDtUpload(LocalDateTime dtUpload) {
        this.dtUpload = dtUpload;
    }
    public ProcessoAposentadoria getProcesso() {
        return processo;
    }
    public void setProcesso(ProcessoAposentadoria processo) {
        this.processo = processo;
    }
    public String getNomeOriginal() {
        return nomeOriginal;
    }
    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }
    public String getJsonExtraido() {
        return jsonExtraido;
    }
    public void setJsonExtraido(String jsonExtraido) {
        this.jsonExtraido = jsonExtraido;
    }
}
