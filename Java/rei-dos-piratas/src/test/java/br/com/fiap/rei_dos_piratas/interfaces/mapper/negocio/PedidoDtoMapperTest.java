package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoDtoMapperTest {

    @Test
    void camposDePagamentoDaRespostaVemDoPagamentoDoPedido() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L, TipoPagamentoEnum.PIX);
        pedido.getPagamento().setIdCobranca("pay_dto");
        pedido.getPagamento().setValorLiquido(new BigDecimal("219.20"));

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.tipoPagamento()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(dto.idCobranca()).isEqualTo("pay_dto");
        assertThat(dto.valorLiquido()).isEqualByComparingTo("219.20");
    }

    @Test
    void pedidoSemPagamentoRetornaCamposDePagamentoNulos() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L);

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.tipoPagamento()).isNull();
        assertThat(dto.idCobranca()).isNull();
        assertThat(dto.valorLiquido()).isNull();
    }
}
