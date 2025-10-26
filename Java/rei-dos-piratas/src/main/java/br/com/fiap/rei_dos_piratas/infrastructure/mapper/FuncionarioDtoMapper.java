package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.interfaces.dto.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.FuncionarioOutDto;

public class FuncionarioDtoMapper {

    public static Funcionario toEntity(FuncionarioInDto dto){
        if(dto == null) return null;
        return new Funcionario(
                dto.userName(),
                dto.nomeCompleto(),
                dto.email(),
                dto.senha(),
                dto.role());
    }

    public static FuncionarioOutDto toDto(Funcionario funcionario){
        if(funcionario == null) return null;
        return new FuncionarioOutDto(
                funcionario.getId(),
                funcionario.getUserName(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.isUsuarioAtivo(),
                funcionario.getDataCadastro(),
                funcionario.getRole());
    }

    private FuncionarioDtoMapper() {
    }
}
