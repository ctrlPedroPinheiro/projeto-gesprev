package iff.edu.br.gesprev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import iff.edu.br.gesprev.entity.enums.EmendaConstitucional;
import iff.edu.br.gesprev.entity.enums.NaturezaAposentadoria;
import iff.edu.br.gesprev.entity.enums.TipoCalculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

/**
 * Entidade que representa um ato de aposentadoria.
 */
@Entity
@Table(name = "ato_aposentadoria")
public class AtoAposentadoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numeroPortaria;

    @Column(nullable = false)
    private int anoPortaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NaturezaAposentadoria naturezaAposentadoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCalculo tipoCalculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmendaConstitucional emendaConstitucional;

    private String referenciaSalarial;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ato_aposentadoria_id")
    private List<Provento> proventos = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate dataFinalizacao;

    @Column(nullable = false)
    private LocalDateTime dtGeracao;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoAposentadoria processo;

    @ManyToOne
    @JoinColumn(name = "usuario_gerador_id", nullable = false)
    private Usuario usuarioGerador;

    public AtoAposentadoria() {
    }

    public AtoAposentadoria(Long id, int numeroPortaria, int anoPortaria, NaturezaAposentadoria naturezaAposentadoria, TipoCalculo tipoCalculo, EmendaConstitucional emendaConstitucional, String referenciaSalarial, List<Provento> proventos, LocalDate dataFinalizacao, LocalDateTime dtGeracao, ProcessoAposentadoria processo, Usuario usuarioGerador) {
        this.id = id;
        this.numeroPortaria = numeroPortaria;
        this.anoPortaria = anoPortaria;
        this.naturezaAposentadoria = naturezaAposentadoria;
        this.tipoCalculo = tipoCalculo;
        this.emendaConstitucional = emendaConstitucional;
        this.referenciaSalarial = referenciaSalarial;
        this.proventos = proventos;
        this.dataFinalizacao = dataFinalizacao;
        this.dtGeracao = dtGeracao;
        this.processo = processo;
        this.usuarioGerador = usuarioGerador;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getNumeroPortaria() {
        return numeroPortaria;
    }
    public void setNumeroPortaria(int numeroPortaria) {
        this.numeroPortaria = numeroPortaria;
    }
    public int getAnoPortaria() {
        return anoPortaria;
    }
    public void setAnoPortaria(int anoPortaria) {
        this.anoPortaria = anoPortaria;
    }
    public NaturezaAposentadoria getNaturezaAposentadoria() {
        return naturezaAposentadoria;
    }
    public void setNaturezaAposentadoria(NaturezaAposentadoria naturezaAposentadoria) {
        this.naturezaAposentadoria = naturezaAposentadoria;
    }
    public TipoCalculo getTipoCalculo() {
        return tipoCalculo;
    }
    public void setTipoCalculo(TipoCalculo tipoCalculo) {
        this.tipoCalculo = tipoCalculo;
    }
    public EmendaConstitucional getEmendaConstitucional() {
        return emendaConstitucional;
    }
    public void setEmendaConstitucional(EmendaConstitucional emendaConstitucional) {
        this.emendaConstitucional = emendaConstitucional;
    }
    public String getReferenciaSalarial() {
        return referenciaSalarial;
    }
    public void setReferenciaSalarial(String referenciaSalarial) {
        this.referenciaSalarial = referenciaSalarial;
    }
    public List<Provento> getProventos() {
        return proventos;
    }
    public void setProventos(List<Provento> proventos) {
        this.proventos = proventos;
    }
    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }
    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }
    public LocalDateTime getDtGeracao() {
        return dtGeracao;
    }
    public void setDtGeracao(LocalDateTime dtGeracao) {
        this.dtGeracao = dtGeracao;
    }
    public ProcessoAposentadoria getProcesso() {
        return processo;
    }
    public void setProcesso(ProcessoAposentadoria processo) {
        this.processo = processo;
    }
    public Usuario getUsuarioGerador() {
        return usuarioGerador;
    }
    public void setUsuarioGerador(Usuario usuarioGerador) {
        this.usuarioGerador = usuarioGerador;
    }
}
