package iff.edu.br.gesprev.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import iff.edu.br.gesprev.entity.Holerite;
import iff.edu.br.gesprev.entity.MemoriaCalculo;
import iff.edu.br.gesprev.entity.Provento;
import iff.edu.br.gesprev.entity.enums.TipoCalculo;

class AtoAposentadoriaServiceTest {

    private final AtoAposentadoriaService service = new AtoAposentadoriaService(
            null, null, null, null, null, null, null);

    @Test
    void deveCriarUmaUnicaParcelaParaCalculoProporcional() {
        MemoriaCalculo memoria = new MemoriaCalculo();
        memoria.setTipoCalculo(TipoCalculo.PROPORCIONAL);
        memoria.setProporcionalidade(new BigDecimal("0.8000"));
        memoria.setValorBeneficio(new BigDecimal("2500.00"));

        List<Provento> proventos = service.montarProventos(memoria);

        assertThat(proventos).hasSize(1);
        assertThat(proventos.get(0).getTipoProvento()).isEqualTo("PROVENTOS PROPORCIONAIS");
        assertThat(proventos.get(0).getValor()).isEqualByComparingTo("2500.00");
    }

    @Test
    void deveCopiarSomenteVencimentosDoHoleriteIntegral() {
        Holerite holerite = new Holerite();
        holerite.setProventos(List.of(
                new Provento("Vencimento base", 1, new BigDecimal("5000.00"), true),
                new Provento("Quinquenio", 0.1, new BigDecimal("500.00"), true),
                new Provento("Previdencia", 0.14, new BigDecimal("700.00"), false)));
        MemoriaCalculo memoria = new MemoriaCalculo();
        memoria.setTipoCalculo(TipoCalculo.INTEGRAL);
        memoria.setHolerite(holerite);

        List<Provento> proventos = service.montarProventos(memoria);

        assertThat(proventos).extracting(Provento::getTipoProvento)
                .containsExactly("Vencimento base", "Quinquenio");
        assertThat(proventos).allMatch(Provento::isVencimento);
    }
}
