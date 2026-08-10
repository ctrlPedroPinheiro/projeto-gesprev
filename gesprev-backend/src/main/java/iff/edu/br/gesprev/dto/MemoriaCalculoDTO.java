package iff.edu.br.gesprev.dto;

import java.math.BigDecimal;
import java.util.List;

public record MemoriaCalculoDTO(
        Long id,
        BigDecimal mediaAritmetica,
        BigDecimal valorBeneficio,
        BigDecimal proporcionalidade,
        String tipoCalculo,
        Long processoId,
        Long holeriteId,
        String holeriteMesReferencia,
        BigDecimal holeriteValorTotalProventos,
        List<ProventoMemoriaDTO> vencimentosHolerite,
        List<FolhaCalculoDTO> folhasCalculo
) {
}
