package iff.edu.br.gesprev.dto;

import java.time.LocalDate;

import iff.edu.br.gesprev.entity.enums.Sexo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

public record CriarProcessoComServidorDTO(
    @NotBlank(message = "Nome do servidor e obrigatorio")
    String nome,

    @NotNull(message = "Data de nascimento e obrigatoria")
    @Past(message = "Data de nascimento deve estar no passado")
    LocalDate dtNascimento,

    @NotBlank(message = "CPF do servidor e obrigatorio")
    String cpf,

    String pis,

    @NotNull(message = "Sexo do servidor e obrigatorio")
    Sexo sexo,

    @Email(message = "Email deve ser valido")
    String email,

    @NotBlank(message = "Matricula e obrigatoria")
    String matricula,

    @NotBlank(message = "Cargo e obrigatorio")
    String cargo,

    @NotBlank(message = "Orgao e obrigatorio")
    String orgao,

    @NotNull(message = "Data de admissao e obrigatoria")
    @PastOrPresent(message = "Data de admissao nao pode estar no futuro")
    LocalDate dtAdmissao,

    @Positive(message = "Numero do processo deve ser maior que zero")
    int numeroProcesso
) {}
