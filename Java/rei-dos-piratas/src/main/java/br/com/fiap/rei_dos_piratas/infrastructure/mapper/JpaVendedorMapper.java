package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaVendedorEntity;

public class JpaVendedorMapper {

    public static JpaVendedorEntity toJpaEntity(Vendedor vendedor) {
        if (vendedor == null) return null;
        return new JpaVendedorEntity(
                vendedor.getId(),
                vendedor.getUserName(),
                vendedor.getNomeCompleto(),
                vendedor.getEmail(),
                vendedor.getSenha(),
                vendedor.isUsuarioAtivo(),
                vendedor.getDataCadastro(),
                vendedor.getRole()
        );
    }

    public static Vendedor toEntity(JpaVendedorEntity jpaVendedor) {
        if (jpaVendedor == null) return null;
        return new Vendedor(
                jpaVendedor.getUserName(),
                jpaVendedor.getId(),
                jpaVendedor.getNomeCompleto(),
                jpaVendedor.getEmail(),
                jpaVendedor.getSenha(),
                jpaVendedor.isUsuarioAtivo(),
                jpaVendedor.getDataCadastro(),
                jpaVendedor.getRole()
        );
    }

    private JpaVendedorMapper() {
    }
}
