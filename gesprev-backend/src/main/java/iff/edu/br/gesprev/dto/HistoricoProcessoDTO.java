package iff.edu.br.gesprev.dto;

import java.time.LocalDateTime;

import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import jakarta.validation.constraints.NotNull;

public record HistoricoProcessoDTO(
    Long id,
    StatusProcesso statusAnterior,

    @NotNull(message = "Status atual e obrigatorio")
    StatusProcesso statusAtual,

    LocalDateTime dtAlteracao,
    String observacao,

    @NotNull(message = "Usuario e obrigatorio")
    Long usuarioId,

    @NotNull(message = "Processo e obrigatorio")
    Long processoId
) {
}
