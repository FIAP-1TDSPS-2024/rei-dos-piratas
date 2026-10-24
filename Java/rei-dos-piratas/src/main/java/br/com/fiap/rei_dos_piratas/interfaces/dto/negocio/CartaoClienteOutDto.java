package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import java.time.LocalDateTime;

/**
 * Cartão salvo exibido ao cliente para escolha no pagamento.
 * O token do Asaas não sai da API: o app envia só o {@code id} (cartao_id) ao fazer o pedido.
 */
public record CartaoClienteOutDto(
        Long id,
        String bandeira,
        String ultimosDigitos,
        LocalDateTime dataCadastro
) {}
