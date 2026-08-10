package iff.edu.br.gesprev.dto;

import iff.edu.br.gesprev.entity.enums.Perfil;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioDTO(
        Long id,

        @NotBlank(message = "Nome do usuario e obrigatorio")
        String nome,

        @NotBlank(message = "CPF do usuario e obrigatorio")
        String cpf,

        @NotBlank(message = "Senha e obrigatoria")
        String senha,

        @NotNull(message = "Perfil e obrigatorio")
        Perfil perfil,

        boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime ultimoLogin
) {
}
