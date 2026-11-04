package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosBoleto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.BoletoOutDto;

public class BoletoDtoMapper {

    public static BoletoOutDto toDto(DadosBoleto boleto) {
        return new BoletoOutDto(
                boleto.linhaDigitavel(),
                boleto.codigoBarras(),
                boleto.urlBoleto(),
                boleto.dataVencimento());
    }

    private BoletoDtoMapper() {}
}
