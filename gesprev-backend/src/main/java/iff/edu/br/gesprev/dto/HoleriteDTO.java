package iff.edu.br.gesprev.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.List;

public record HoleriteDTO(
    List<ProventoDTO> proventos,
    @JsonAlias("totalVencimentos")
    BigDecimal valorTotalProventos,
    String mesReferencia
) {   
}
