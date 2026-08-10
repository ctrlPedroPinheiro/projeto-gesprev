package iff.edu.br.gesprev.dto;

import java.math.BigDecimal;

public record ProventoMemoriaDTO(
        Long id,
        String descricao,
        double referencia,
        BigDecimal valor
) {
}
