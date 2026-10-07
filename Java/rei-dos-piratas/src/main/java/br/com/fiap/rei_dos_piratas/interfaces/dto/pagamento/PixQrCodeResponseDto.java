package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import java.time.LocalDateTime;

public record PixQrCodeResponseDto(
    Boolean success,
    //QR Code Pix
    String encodedImage,
    //Pix copia e cola
    String payload,
    LocalDateTime expirationDate,
    String description
) {
}
