package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoOutDto;

public class EnderecoDtoMapper {

    public static Endereco toEntity(EnderecoInDto enderecoDto) {
        return new Endereco(
                null,
                enderecoDto.numero(),
                enderecoDto.cep(),
                enderecoDto.logradouro(),
                enderecoDto.bairro(),
                null,
                enderecoDto.cidade(),
                null,
                enderecoDto.estadoNome(),
                enderecoDto.estadoSigla(),
                null,
                null,
                null);
    }

    public static EnderecoOutDto toDto(Endereco endereco) {
        return new EnderecoOutDto(
                endereco.getId(),
                endereco.getNumero(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getBairro(),
                endereco.getCidadeId(),
                endereco.getCidadeNome(),
                endereco.getEstadoId(),
                endereco.getEstadoNome(),
                endereco.getEstadoSigla());
    }

    private EnderecoDtoMapper() {}
}
