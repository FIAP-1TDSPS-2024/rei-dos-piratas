package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegraBoletoTest {

    @Test
    void vencimentoDeveSerAQuantidadeDeDiasConfiguradaAPartirDeHoje() {
        RegraBoleto regra = new RegraBoleto(3);

        assertThat(regra.vencimentoAPartirDe(LocalDate.of(2026, 10, 30))).isEqualTo(LocalDate.of(2026, 11, 2));
    }

    @Test
    void cancelamentoDoRegistroDeveSerUmDiaAposOVencimento() {
        assertThat(RegraBoleto.DIAS_CANCELAMENTO_REGISTRO).isEqualTo(1);
    }

    @Test
    void diasDeVencimentoDevemSerPeloMenosUm() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validateProperty(new RegraBoleto(0), "diasVencimento")).isNotEmpty();
        }
    }
}
