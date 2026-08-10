package iff.edu.br.gesprev.dto;

import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.validation.constraints.NotNull;

public record ChecklistDocumentoDTO(
    Long id,

    @NotNull(message = "Tipo do documento e obrigatorio")
    TipoDocumento tipoDocumento,

    boolean entregue,
    boolean valido,

    @NotNull(message = "Processo e obrigatorio")
    Long processoId,

    String observacao
) {
}
