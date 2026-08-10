package iff.edu.br.gesprev.dto;

import jakarta.validation.constraints.NotBlank;

public record PerguntaChatDTO(
        @NotBlank(message = "Pergunta e obrigatoria")
        String pergunta,
        Integer numeroProcesso) {
}
