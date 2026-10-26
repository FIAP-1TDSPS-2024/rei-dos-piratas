package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.PagamentoCartao;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoPedidoInDto;

public class PagamentoCartaoCreditoMapper {

    public static PagamentoCartao toEntity(CartaoPedidoInDto cartaoDto){
        return new PagamentoCartao(
                cartaoDto.cartaoId(),
                cartaoDto.getDadosCartaoCredito(),
                cartaoDto.parcelas());
    }
}
