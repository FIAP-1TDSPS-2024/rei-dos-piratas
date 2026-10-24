package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanWrapperImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PagamentoValidacaoTest {

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

    private Pagamento pagamentoValido() {
        return new Pagamento(1L, TipoPagamentoEnum.CREDIT_CARD, StatusPagamentoEnum.CONFIRMADO, "pay_000001",
                new BigDecimal("221.19"), new BigDecimal("210.50"), 3, null, null,
                LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void pagamentoValidoNaoTemViolacoes() {
        assertThat(validator.validate(pagamentoValido())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"tipo", "status", "valor", "parcelas", "dataCriacao"})
    void camposObrigatoriosNaoPodemSerNulos(String campo) {
        Pagamento pagamento = pagamentoValido();
        new BeanWrapperImpl(pagamento).setPropertyValue(campo, null);

        assertThat(validator.validateProperty(pagamento, campo)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00"})
    void valorDeveSerMaiorQueZero(String valor) {
        Pagamento pagamento = pagamentoValido();
        pagamento.setValor(new BigDecimal(valor));

        assertThat(validator.validateProperty(pagamento, "valor")).isNotEmpty();
    }

    @Test
    void valorDeveTerNoMaximoDuasCasasDecimais() {
        Pagamento pagamento = pagamentoValido();
        pagamento.setValor(new BigDecimal("10.001"));

        assertThat(validator.validateProperty(pagamento, "valor")).isNotEmpty();
    }

    @Test
    void valorLiquidoDeveTerNoMaximoDuasCasasDecimais() {
        Pagamento pagamento = pagamentoValido();
        pagamento.setValorLiquido(new BigDecimal("10.001"));

        assertThat(validator.validateProperty(pagamento, "valorLiquido")).isNotEmpty();
    }

    @Test
    void parcelasDevemSerPeloMenosUma() {
        Pagamento pagamento = pagamentoValido();
        pagamento.setParcelas(0);

        assertThat(validator.validateProperty(pagamento, "parcelas")).isNotEmpty();
    }

    @Test
    void idCobrancaDeveTerNoMaximo50Caracteres() {
        Pagamento pagamento = pagamentoValido();
        pagamento.setIdCobranca("p".repeat(51));

        assertThat(validator.validateProperty(pagamento, "idCobranca")).isNotEmpty();
    }

    @Test
    void motivoRecusaDeveTerNoMaximo255Caracteres() {
        Pagamento pagamento = pagamentoValido();
        pagamento.setMotivoRecusa("m".repeat(256));

        assertThat(validator.validateProperty(pagamento, "motivoRecusa")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dataCriacao", "dataConfirmacao"})
    void datasNaoPodemEstarNoFuturo(String campo) {
        Pagamento pagamento = pagamentoValido();
        new BeanWrapperImpl(pagamento).setPropertyValue(campo, LocalDateTime.now().plusDays(1));

        assertThat(validator.validateProperty(pagamento, campo)).isNotEmpty();
    }

    @Test
    void pedidoValidaOPagamentoJunto() {
        Pedido pedido = new Pedido(null, null, new java.util.ArrayList<>(), 1L, TipoPagamentoEnum.PIX);
        pedido.getPagamento().setValor(new BigDecimal("-1.00"));

        assertThat(validator.validate(pedido))
                .anyMatch(violacao -> violacao.getPropertyPath().toString().equals("pagamento.valor"));
    }
}
