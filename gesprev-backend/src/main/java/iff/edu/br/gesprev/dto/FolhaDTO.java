package iff.edu.br.gesprev.dto;

import java.math.BigDecimal;

public record FolhaDTO(
    Integer anoReferencia,
    String competencia,
    BigDecimal vencimentos,
    BigDecimal descontos,
    BigDecimal liquido
) {
}
