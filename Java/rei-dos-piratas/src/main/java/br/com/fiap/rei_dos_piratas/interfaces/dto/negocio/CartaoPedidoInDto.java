package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

/**
 * Cartão informado no pedido: {@code cartaoId} de um cartão salvo ou os dados de um cartão novo.
 */
public record CartaoPedidoInDto(
        Long cartaoId,
        String titular,
        String numero,
        String mesValidade,
        String anoValidade,
        String cvv,
        Integer parcelas
) {}
