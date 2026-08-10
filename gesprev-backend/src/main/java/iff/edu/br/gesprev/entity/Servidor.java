package iff.edu.br.gesprev.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import iff.edu.br.gesprev.entity.enums.Sexo;

/**
 * Entidade que representa um servidor público, que pode ser associado a processos de aposentadoria.
 */
@Entity
@Table(name = "servidor")
public class Servidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;
    
    private LocalDate dtNascimento;
    
    @NotBlank
    @Column(nullable = false)
    private String cpf;

    @Column(length = 20)
    private String pis;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo = Sexo.FEMININO;

    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String matricula;

    @NotBlank
    @Column(nullable = false)
    private String cargo;

    @NotBlank
    @Column(nullable = false)
    private String orgao;

    private LocalDate dtAdmissao;

    public Servidor() {
    }

    public Servidor(Long id, String nome, LocalDate dtNascimento, String cpf, String pis, Sexo sexo, String email, String matricula, String cargo, String orgao, LocalDate dtAdmissao) {
        this.id = id;
        this.nome = nome;
        this.dtNascimento = dtNascimento;
        this.cpf = cpf;
        this.pis = pis;
        this.sexo = sexo;
        this.email = email;
        this.matricula = matricula;
        this.cargo = cargo;
        this.orgao = orgao;
        this.dtAdmissao = dtAdmissao;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public LocalDate getDtNascimento() {
        return dtNascimento;
    }
    public void setDtNascimento(LocalDate dtNascimento) {
        this.dtNascimento = dtNascimento;
    }
    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    public String getPis() {
        return pis;
    }
    public void setPis(String pis) {
        this.pis = pis;
    }
    public Sexo getSexo() {
        return sexo;
    }
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getMatricula() {
        return matricula;
    }
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
    public String getOrgao() {
        return orgao;
    }
    public void setOrgao(String orgao) {
        this.orgao = orgao;
    }
    public LocalDate getDtAdmissao() {
        return dtAdmissao;
    }
    public void setDtAdmissao(LocalDate dtAdmissao) {
        this.dtAdmissao = dtAdmissao;
    }
}
