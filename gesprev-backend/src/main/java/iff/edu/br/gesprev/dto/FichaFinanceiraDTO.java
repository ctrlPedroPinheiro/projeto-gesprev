package iff.edu.br.gesprev.dto;

import java.util.List;

public record FichaFinanceiraDTO(
    List<FolhaDTO> folhas,
    Integer anoReferencia
) {
}
