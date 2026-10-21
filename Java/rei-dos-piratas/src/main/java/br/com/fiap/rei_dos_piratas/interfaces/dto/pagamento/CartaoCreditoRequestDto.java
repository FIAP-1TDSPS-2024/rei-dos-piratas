package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

public record CartaoCreditoRequestDto(
        String holderName,
        String number,
        String expiryMonth,
        String expiryYear,
        String ccv
) {}
