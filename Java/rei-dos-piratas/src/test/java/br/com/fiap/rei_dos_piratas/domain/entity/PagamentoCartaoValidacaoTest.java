package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PagamentoCartaoValidacaoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private final DadosCartaoCredito cartaoNovo = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987");

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
    void pagamentoComCartaoNovoValidoNaoTemViolacoes() {
        assertThat(validator.validate(new PagamentoCartao(null, cartaoNovo, 2))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void parcelasDevemSerPeloMenosUma(int parcelas) {
        assertThat(validator.validateProperty(new PagamentoCartao(null, cartaoNovo, parcelas), "parcelas")).isNotEmpty();
    }

    @Test
    void validaTambemOsDadosDoCartaoNovo() {
        DadosCartaoCredito cartaoInvalido = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "1");

        assertThat(validator.validate(new PagamentoCartao(null, cartaoInvalido, 1)))
                .anyMatch(violacao -> violacao.getPropertyPath().toString().equals("novoCartao.cvv"));
    }
}
