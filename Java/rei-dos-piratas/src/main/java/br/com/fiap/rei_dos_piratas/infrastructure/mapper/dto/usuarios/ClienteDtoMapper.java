package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;

public class ClienteDtoMapper {

    public static Cliente toEntity(ClienteInDto dto){
        if (dto == null) return null;
        return new Cliente(
                dto.userName(),
                dto.nomeCompleto(),
                dto.email(),
                dto.senha(),
                dto.dataNascimento(),
                dto.sexo(),
                dto.cpf(),
                dto.celular());
    }

    public static ClienteOutDto toDto(Cliente cliente){
        if (cliente == null) return null;
        return new ClienteOutDto(
                cliente.getId(),
                cliente.getUsername(),
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getCelular(),
                cliente.isUsuarioAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataNascimento(),
                cliente.getSexo(),
                cliente.getCarrinho());
    }

    private ClienteDtoMapper() {
    }
}
