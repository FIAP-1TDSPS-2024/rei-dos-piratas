package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;

public class JpaFuncionarioMapper {

    public static JpaFuncionarioEntity toJpaEntity(Funcionario funcionario) {
        if (funcionario == null) return null;
        return new JpaFuncionarioEntity(
                funcionario.getId(),
                funcionario.getUsername(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.getSenha(),
                funcionario.isUsuarioAtivo(),
                funcionario.getDataCadastro(),
                JpaPerfilMapper.toJpaEntityWithoutRoles(funcionario.getPerfil()),
                funcionario.getDataDemissao(),
                funcionario.getSalario()
        );
    }

    public static Funcionario toEntity(JpaFuncionarioEntity jpaFuncionario) {
        if (jpaFuncionario == null) return null;
        return new Funcionario(
                jpaFuncionario.getUserName(),
                jpaFuncionario.getId(),
                jpaFuncionario.getNomeCompleto(),
                jpaFuncionario.getEmail(),
                jpaFuncionario.getSenha(),
                jpaFuncionario.isUsuarioAtivo(),
                jpaFuncionario.getDataCadastro(),
                JpaPerfilMapper.toEntityWithoutRoles(jpaFuncionario.getPerfil()),
                jpaFuncionario.getDataDemissao(),
                jpaFuncionario.getSalario()
        );
    }

    private JpaFuncionarioMapper() {
    }
}
