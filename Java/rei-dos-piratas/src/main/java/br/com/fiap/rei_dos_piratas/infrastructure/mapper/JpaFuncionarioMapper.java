package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaFuncionarioEntity;

public class JpaFuncionarioMapper {

    public static JpaFuncionarioEntity toJpaEntity(Funcionario funcionario) {
        if (funcionario == null) return null;
        return new JpaFuncionarioEntity(
                funcionario.getId(),
                funcionario.getUserName(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.getSenha(),
                funcionario.isUsuarioAtivo(),
                funcionario.getDataCadastro(),
                funcionario.getRole(),
                funcionario.getDataDemissao(),
                funcionario.getSalario()
        );
    }

    public static Funcionario toEntity(JpaFuncionarioEntity jpaVendedor) {
        if (jpaVendedor == null) return null;
        return new Funcionario(
                jpaVendedor.getUserName(),
                jpaVendedor.getId(),
                jpaVendedor.getNomeCompleto(),
                jpaVendedor.getEmail(),
                jpaVendedor.getSenha(),
                jpaVendedor.isUsuarioAtivo(),
                jpaVendedor.getDataCadastro(),
                jpaVendedor.getRole(),
                jpaVendedor.getDataDemissao(),
                jpaVendedor.getSalario()
        );
    }

    private JpaFuncionarioMapper() {
    }
}
