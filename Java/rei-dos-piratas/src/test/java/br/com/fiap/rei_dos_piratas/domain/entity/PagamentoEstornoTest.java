package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagamentoEstornoTest {

    private Pagamento pago(StatusPagamentoEnum status) {
        Pagamento pagamento = Pagamento.pendente(TipoPagamentoEnum.PIX);
        pagamento.setValor(new BigDecimal("221.19"));
        pagamento.setStatus(status);
        return pagamento;
    }

    @Test
    void solicitarEstornoDeveRegistrarEstornoSolicitadoNoPagamento() {
        // Arrange
        Pagamento pagamento = pago(StatusPagamentoEnum.CONFIRMADO);

        // Act
        Estorno estorno = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 7L, new BigDecimal("100.00"));

        // Assert
        assertThat(pagamento.getEstornos()).containsExactly(estorno);
        assertThat(estorno.getStatus()).isEqualTo(StatusEstornoEnum.SOLICITADO);
        assertThat(estorno.getOrigem()).isEqualTo(OrigemEstornoEnum.DEVOLUCAO);
        assertThat(estorno.getDevolucaoId()).isEqualTo(7L);
        assertThat(estorno.getValor()).isEqualByComparingTo("100.00");
        assertThat(estorno.getDataSolicitacao()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = StatusPagamentoEnum.class, names = {"PENDENTE", "RECUSADO", "CANCELADO", "ESTORNADO"})
    void soPagamentoConfirmadoOuRecebidoPodeSerEstornado(StatusPagamentoEnum status) {
        Pagamento pagamento = pago(status);

        assertThatThrownBy(() -> pagamento.solicitarEstorno(OrigemEstornoEnum.CANCELAMENTO, null, new BigDecimal("10.00")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void estornoNaoPodeTerValorZeroOuNegativo() {
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);

        assertThatThrownBy(() -> pagamento.solicitarEstorno(OrigemEstornoEnum.CANCELAMENTO, null, BigDecimal.ZERO))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void totalEstornadoNaoPodePassarDoValorPago() {
        // Arrange: estorno em aberto também conta, para não estornar duas vezes
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);
        pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 1L, new BigDecimal("200.00"));

        // Act & Assert
        assertThatThrownBy(() -> pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 2L, new BigDecimal("21.20")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void estornoQueFalhouNaoContaNoLimite() {
        // Arrange
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);
        pagamento.solicitarEstorno(OrigemEstornoEnum.CANCELAMENTO, null, new BigDecimal("221.19")).setStatus(StatusEstornoEnum.FALHOU);

        // Act
        Estorno novo = pagamento.solicitarEstorno(OrigemEstornoEnum.CANCELAMENTO, null, new BigDecimal("221.19"));

        // Assert
        assertThat(novo.getStatus()).isEqualTo(StatusEstornoEnum.SOLICITADO);
    }

    @Test
    void concluirEstornoParcialNaoMudaOStatusDoPagamento() {
        // Arrange
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);
        Estorno estorno = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 1L, new BigDecimal("100.00"));

        // Act
        pagamento.concluirEstorno(estorno);

        // Assert
        assertThat(estorno.getStatus()).isEqualTo(StatusEstornoEnum.CONCLUIDO);
        assertThat(estorno.getDataConclusao()).isNotNull();
        assertThat(pagamento.valorEstornado()).isEqualByComparingTo("100.00");
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
    }

    @Test
    void concluirOEstornoDoValorTodoDeveMarcarOPagamentoComoEstornado() {
        // Arrange
        Pagamento pagamento = pago(StatusPagamentoEnum.CONFIRMADO);
        Estorno primeiro = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 1L, new BigDecimal("121.19"));
        Estorno segundo = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 2L, new BigDecimal("100.00"));
        pagamento.concluirEstorno(primeiro);

        // Act
        pagamento.concluirEstorno(segundo);

        // Assert
        assertThat(pagamento.valorEstornado()).isEqualByComparingTo("221.19");
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.ESTORNADO);
    }

    @Test
    void concluirDeNovoNaoDeveSomarDuasVezes() {
        // Arrange: evento do fornecedor repetido
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);
        Estorno estorno = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 1L, new BigDecimal("100.00"));
        pagamento.concluirEstorno(estorno);

        // Act
        pagamento.concluirEstorno(estorno);

        // Assert
        assertThat(pagamento.valorEstornado()).isEqualByComparingTo("100.00");
    }

    @Test
    void estornoEmAbertoDeveSerOMaisAntigoAindaNaoFinalizado() {
        // Arrange
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);
        Estorno concluido = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 1L, new BigDecimal("50.00"));
        pagamento.concluirEstorno(concluido);
        Estorno aberto = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 2L, new BigDecimal("50.00"));
        pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 3L, new BigDecimal("50.00"));

        // Act & Assert
        assertThat(pagamento.estornoEmAberto()).containsSame(aberto);
    }

    @Test
    void semEstornoEmAbertoDeveRetornarVazio() {
        assertThat(pago(StatusPagamentoEnum.RECEBIDO).estornoEmAberto()).isEmpty();
    }

    @Test
    void estornoComValorAcimaDoLimiteDeCasasDeveGerarViolacao() {
        // Arrange
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Pagamento pagamento = pago(StatusPagamentoEnum.RECEBIDO);
        Estorno estorno = pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 1L, new BigDecimal("10.00"));
        estorno.setValor(new BigDecimal("10.001"));

        // Act & Assert: a validação em cascata do pagamento alcança o estorno
        assertThat(validator.validate(pagamento)).extracting(v -> v.getPropertyPath().toString())
                .contains("estornos[0].valor");
    }
}
