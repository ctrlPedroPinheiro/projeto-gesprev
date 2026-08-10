package iff.edu.br.gesprev.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record CtsDTO(
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate inicioContribuicao,
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate fimContribuicao,
    int tempoAverbacao,
    int totalBruto,
    int faltas,
    int totalDias,
    String tempoLegivel
) {
}
