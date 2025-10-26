package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.interfaces.dto.EnderecoInDto;

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
                null);
    }

    private EnderecoDtoMapper() {}
}
