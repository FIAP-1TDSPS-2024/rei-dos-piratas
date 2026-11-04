package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EstornoOutDto;

public class EstornoDtoMapper {

    public static EstornoOutDto toDto(Estorno estorno) {
        return new EstornoOutDto(estorno.getId(), estorno.getOrigem(), estorno.getDevolucaoId(), estorno.getValor(),
                estorno.getStatus(), estorno.getUrlSolicitacaoBoleto(), estorno.getDataSolicitacao(), estorno.getDataConclusao());
    }

    private EstornoDtoMapper() {}
}
