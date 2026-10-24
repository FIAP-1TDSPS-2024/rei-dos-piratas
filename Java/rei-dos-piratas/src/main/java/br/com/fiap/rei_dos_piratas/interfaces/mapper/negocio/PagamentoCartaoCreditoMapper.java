package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosCartaoCredito;
import br.com.fiap.rei_dos_piratas.domain.entity.PagamentoCartao;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoPedidoInDto;

public class PagamentoCartaoCreditoMapper {

    public static PagamentoCartao toEntity(CartaoPedidoInDto cartaoDto){
        DadosCartaoCredito dadosCartao = new DadosCartaoCredito(
                cartaoDto.titular(),
                cartaoDto.numero(),
                cartaoDto.mesValidade(),
                cartaoDto.anoValidade(),
                cartaoDto.cvv());

        return new PagamentoCartao(
                cartaoDto.cartaoId(),
                dadosCartao,
                cartaoDto.parcelas());
    }
}
