package iff.edu.br.gesprev.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class HoleriteTest {

    @Test
    void deveSomarSomenteLancamentosMarcadosComoVencimento() {
        Holerite holerite = new Holerite();
        holerite.setProventos(List.of(
                new Provento("Vencimento base", 1, new BigDecimal("5000.00"), true),
                new Provento("Gratificacao", 1, new BigDecimal("750.00"), true),
                new Provento("Previdencia", 0.14, new BigDecimal("700.00"), false),
                new Provento("Imposto de renda", 1, new BigDecimal("450.00"), false)));

        assertThat(holerite.calcularValorTotalProventos())
                .isEqualByComparingTo("5750.00");
    }
}
