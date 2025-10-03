package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;

public class ClienteDtoMapper {

    public static Cliente toEntity(ClienteInDto dto){
        return new Cliente(
                dto.userName(),
                dto.nomeCompleto(),
                dto.email(),
                dto.senha(),
                dto.dataNascimento(),
                dto.sexo(),
                dto.endereco(),
                dto.cpf());
    }

    public static ClienteOutDto toDto(Cliente cliente){
        return new ClienteOutDto(
                cliente.getId(),
                cliente.getUserName(),
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.isUsuarioAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataNascimento(),
                cliente.getSexo(),
                cliente.getEndereco());
    }

    private ClienteDtoMapper() {
    }
}
