package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * QR Code PIX do pedido. Os nomes dos campos mantêm o contrato JSON já consumido pelo app
 * (success, encoded_image, payload, expiration_date, description).
 */
public record PixQrCodeOutDto(
        Boolean success,
        // Imagem do QR Code em Base64
        String encodedImage,
        // PIX copia e cola
        String payload,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expirationDate,
        String description
) {}
