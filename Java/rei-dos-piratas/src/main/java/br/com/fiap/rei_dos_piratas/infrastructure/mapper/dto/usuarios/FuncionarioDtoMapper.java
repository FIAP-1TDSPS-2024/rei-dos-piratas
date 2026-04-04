package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;

public class FuncionarioDtoMapper {

    public static Funcionario toEntity(FuncionarioInDto dto){
        if(dto == null) return null;
        return new Funcionario(
                dto.userName(),
                dto.nomeCompleto(),
                dto.email(),
                dto.senha(),
                new Perfil(dto.perfil().toString()),
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
                funcionario.getPerfil(),
                funcionario.getDataDemissao(),
                funcionario.getSalario());
    }

    private FuncionarioDtoMapper() {
    }
}
