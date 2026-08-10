package iff.edu.br.gesprev.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
    @NotBlank(message = "CPF e obrigatorio")
    String cpf,

    @NotBlank(message = "Senha e obrigatoria")
    String senha
) {
}
