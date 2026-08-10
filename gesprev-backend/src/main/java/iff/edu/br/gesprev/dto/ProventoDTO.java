package iff.edu.br.gesprev.dto;

import java.math.BigDecimal;

public record ProventoDTO(
    Long id,
    String descricao,
    String referencia,
    BigDecimal valor,
    boolean vencimento
) {
}
