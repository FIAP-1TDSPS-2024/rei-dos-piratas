package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanWrapperImpl;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CartaoClienteValidacaoTest {

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

    private CartaoCliente cartaoValido() {
        return new CartaoCliente(1L, new Cliente(), "tok_abc123", "1234", "VISA", true, LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void cartaoValidoNaoTemViolacoes() {
        assertThat(validator.validate(cartaoValido())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cliente", "token", "ultimosDigitos", "bandeira", "ativo", "dataCadastro"})
    void camposObrigatoriosNaoPodemSerNulos(String campo) {
        CartaoCliente cartao = cartaoValido();
        new BeanWrapperImpl(cartao).setPropertyValue(campo, null);

        assertThat(validator.validateProperty(cartao, campo)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"token", "bandeira"})
    void textosObrigatoriosNaoPodemSerEmBranco(String campo) {
        CartaoCliente cartao = cartaoValido();
        new BeanWrapperImpl(cartao).setPropertyValue(campo, "   ");

        assertThat(validator.validateProperty(cartao, campo)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345", "12a4"})
    void ultimosDigitosDevemSerExatamenteQuatroDigitos(String ultimosDigitos) {
        CartaoCliente cartao = cartaoValido();
        cartao.setUltimosDigitos(ultimosDigitos);

        assertThat(validator.validateProperty(cartao, "ultimosDigitos")).isNotEmpty();
    }

    @Test
    void tokenDeveTerNoMaximo100Caracteres() {
        CartaoCliente cartao = cartaoValido();
        cartao.setToken("t".repeat(101));

        assertThat(validator.validateProperty(cartao, "token")).isNotEmpty();
    }

    @Test
    void bandeiraDeveTerNoMaximo30Caracteres() {
        CartaoCliente cartao = cartaoValido();
        cartao.setBandeira("b".repeat(31));

        assertThat(validator.validateProperty(cartao, "bandeira")).isNotEmpty();
    }

    @Test
    void dataCadastroNaoPodeEstarNoFuturo() {
        CartaoCliente cartao = cartaoValido();
        cartao.setDataCadastro(LocalDateTime.now().plusDays(1));

        assertThat(validator.validateProperty(cartao, "dataCadastro")).isNotEmpty();
    }
}
