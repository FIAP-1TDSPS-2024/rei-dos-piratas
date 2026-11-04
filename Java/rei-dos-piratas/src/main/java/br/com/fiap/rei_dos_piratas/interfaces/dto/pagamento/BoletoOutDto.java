package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import java.time.LocalDate;

/** Dados para o cliente pagar o boleto do pedido. */
public record BoletoOutDto(
        String linhaDigitavel,
        String codigoBarras,
        String urlBoleto,
        LocalDate dataVencimento
) {}
