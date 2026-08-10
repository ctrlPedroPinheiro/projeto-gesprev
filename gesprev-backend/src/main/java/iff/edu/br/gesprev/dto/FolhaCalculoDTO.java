package iff.edu.br.gesprev.dto;

import java.math.BigDecimal;

public record FolhaCalculoDTO(
        int anoReferencia,
        String competencia,
        BigDecimal valorOriginal,
        BigDecimal fatorAtualizacao,
        BigDecimal valorCorrigido,
        boolean utilizadaNaMedia
) {
}
