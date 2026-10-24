package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DadosCartaoCreditoValidacaoTest {

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

    private DadosCartaoCredito cartao(String titular, String numero, String mes, String ano, String cvv) {
        return new DadosCartaoCredito(titular, numero, mes, ano, cvv);
    }

    @Test
    void cartaoValidoNaoTemViolacoes() {
        assertThat(validator.validate(cartao("JOAO SILVA", "4111111111111234", "12", "2030", "987"))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void titularNaoPodeSerVazio(String titular) {
        assertThat(validator.validateProperty(cartao(titular, "4111111111111234", "12", "2030", "987"), "titular")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"411111111111", "41111111111111111111", "4111-1111-1111-1234", "41111111111ABCD"})
    void numeroDeveTerDe13A19Digitos(String numero) {
        assertThat(validator.validateProperty(cartao("JOAO SILVA", numero, "12", "2030", "987"), "numero")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "13", "1"})
    void mesDeValidadeDeveSerDe01A12(String mes) {
        assertThat(validator.validateProperty(cartao("JOAO SILVA", "4111111111111234", mes, "2030", "987"), "mesValidade")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"30", "20301"})
    void anoDeValidadeDeveTerQuatroDigitos(String ano) {
        assertThat(validator.validateProperty(cartao("JOAO SILVA", "4111111111111234", "12", ano, "987"), "anoValidade")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "12345", "12a"})
    void cvvDeveTerTresOuQuatroDigitos(String cvv) {
        assertThat(validator.validateProperty(cartao("JOAO SILVA", "4111111111111234", "12", "2030", cvv), "cvv")).isNotEmpty();
    }

    @Test
    void camposObrigatoriosNaoPodemSerNulos() {
        Set<ConstraintViolation<DadosCartaoCredito>> violacoes = validator.validate(cartao(null, null, null, null, null));

        assertThat(violacoes)
                .extracting(violacao -> violacao.getPropertyPath().toString())
                .contains("titular", "numero", "mesValidade", "anoValidade", "cvv");
    }

    @Test
    @DisplayName("Mensagens de violação não expõem o número nem o CVV informados")
    void mensagensNaoExpoemNumeroNemCvv() {
        Set<ConstraintViolation<DadosCartaoCredito>> violacoes =
                validator.validate(cartao("JOAO SILVA", "41111111111ABCD", "12", "2030", "98X"));

        assertThat(violacoes).isNotEmpty();
        assertThat(violacoes).allSatisfy(violacao -> assertThat(violacao.getMessage())
                .doesNotContain("41111111111ABCD")
                .doesNotContain("98X"));
    }
}
