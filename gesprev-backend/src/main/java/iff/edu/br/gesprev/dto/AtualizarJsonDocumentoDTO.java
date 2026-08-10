package iff.edu.br.gesprev.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarJsonDocumentoDTO(
        @NotBlank(message = "JSON extraido e obrigatorio")
        String jsonExtraido
) {
}
