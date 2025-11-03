package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios;

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
                dto.role(),
                dto.salario());
    }

    public static FuncionarioOutDto toDto(Funcionario funcionario){
        if(funcionario == null) return null;
        return new FuncionarioOutDto(
                funcionario.getId(),
                funcionario.getUsername(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.isUsuarioAtivo(),
                funcionario.getDataCadastro(),
                funcionario.getRole(),
                funcionario.getDataDemissao(),
                funcionario.getSalario());
    }

    private FuncionarioDtoMapper() {
    }
}
