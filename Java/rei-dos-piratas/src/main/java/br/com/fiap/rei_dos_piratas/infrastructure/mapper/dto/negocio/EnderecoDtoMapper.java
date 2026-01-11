package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoOutDto;

import javax.lang.model.util.ElementScanner6;

public class EnderecoDtoMapper {

    public static Endereco toEntity(EnderecoInDto enderecoDto) {

        Estado estado = new Estado(
                null,
                enderecoDto.estadoNome(),
                enderecoDto.estadoSigla());

        Cidade cidade = new Cidade(
                null,
                enderecoDto.cidade(),
                estado);

        return new Endereco(
                null,
                enderecoDto.numero(),
                enderecoDto.cep(),
                enderecoDto.logradouro(),
                enderecoDto.bairro(),
                cidade,
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
                endereco.getCidade().getId(),
                endereco.getCidade().getNome(),
                endereco.getCidade().getEstado().getId(),
                endereco.getCidade().getEstado().getNome(),
                endereco.getCidade().getEstado().getSigla());
    }

    private EnderecoDtoMapper() {}
}
