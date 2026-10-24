package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RegraParcelamentoValidacaoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @Test
    void regraValidaNaoTemViolacoes() {
        assertThat(validator.validate(new RegraParcelamento(new BigDecimal("100.00"), 3))).isEmpty();
    }

    @Test
    void valorMinimoEhObrigatorio() {
        assertThat(validator.validateProperty(new RegraParcelamento(null, 3), "valorMinimo")).isNotEmpty();
    }

    @Test
    void valorMinimoNaoPodeSerNegativo() {
        assertThat(validator.validateProperty(new RegraParcelamento(new BigDecimal("-0.01"), 3), "valorMinimo")).isNotEmpty();
    }

    @Test
    void maximoDeParcelasDeveSerPeloMenosUm() {
        assertThat(validator.validateProperty(new RegraParcelamento(new BigDecimal("100.00"), 0), "maxParcelas")).isNotEmpty();
    }
}
