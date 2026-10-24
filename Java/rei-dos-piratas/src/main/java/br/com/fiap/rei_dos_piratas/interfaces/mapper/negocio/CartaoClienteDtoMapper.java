package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoClienteOutDto;

public class CartaoClienteDtoMapper {

    /** O token fica de fora de propósito: ele só é usado pelo back-end na cobrança. */
    public static CartaoClienteOutDto toDto(CartaoCliente cartao) {
        return new CartaoClienteOutDto(
                cartao.getId(),
                cartao.getBandeira(),
                cartao.getUltimosDigitos(),
                cartao.getDataCadastro());
    }

    private CartaoClienteDtoMapper() {}
}
