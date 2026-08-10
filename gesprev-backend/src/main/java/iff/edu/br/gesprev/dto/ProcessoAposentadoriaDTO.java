package iff.edu.br.gesprev.dto;

import java.time.LocalDateTime;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProcessoAposentadoriaDTO(
        Long id,

        @Positive(message = "Numero do processo deve ser maior que zero")
        int numeroProcesso,

        LocalDateTime dtCriacao,

        @NotNull(message = "Status do processo e obrigatorio")
        StatusProcesso status,

        LocalDateTime dtAtualizacao,

        @NotNull(message = "Servidor e obrigatorio")
        Long servidorId
) {
}
