package br.com.fiap.rei_dos_piratas.interfaces.mapper.frete;

import br.com.fiap.rei_dos_piratas.domain.gateway.OpcaoFrete;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.OpcaoFreteOutDto;

public class OpcaoFreteDtoMapper {

    public static OpcaoFreteOutDto toDto(OpcaoFrete opcao) {
        OpcaoFreteOutDto.Company company = opcao.transportadora() == null ? null
                : new OpcaoFreteOutDto.Company(opcao.transportadora().id(), opcao.transportadora().nome(), opcao.transportadora().logo());
        return new OpcaoFreteOutDto(opcao.id(), opcao.nome(), opcao.preco(), opcao.precoPersonalizado(),
                opcao.desconto(), opcao.moeda(), opcao.prazoDias(), company);
    }

    private OpcaoFreteDtoMapper() {}
}
