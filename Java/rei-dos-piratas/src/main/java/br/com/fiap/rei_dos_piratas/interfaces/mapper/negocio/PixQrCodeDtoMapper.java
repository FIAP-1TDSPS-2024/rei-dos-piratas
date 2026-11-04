package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.gateway.QrCodePix;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PixQrCodeOutDto;

public class PixQrCodeDtoMapper {

    public static PixQrCodeOutDto toDto(QrCodePix qrCode) {
        return new PixQrCodeOutDto(
                qrCode.sucesso(),
                qrCode.imagemBase64(),
                qrCode.copiaECola(),
                qrCode.expiraEm(),
                qrCode.descricao());
    }

    private PixQrCodeDtoMapper() {}
}
