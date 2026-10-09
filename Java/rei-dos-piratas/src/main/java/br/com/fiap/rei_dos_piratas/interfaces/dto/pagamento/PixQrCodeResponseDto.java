package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record PixQrCodeResponseDto(
    Boolean success,
    //QR Code Pix
    @JsonAlias("encodedImage") String encodedImage,
    //Pix copia e cola
    String payload,
    @JsonAlias("expirationDate") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expirationDate,
    String description
) {
}
