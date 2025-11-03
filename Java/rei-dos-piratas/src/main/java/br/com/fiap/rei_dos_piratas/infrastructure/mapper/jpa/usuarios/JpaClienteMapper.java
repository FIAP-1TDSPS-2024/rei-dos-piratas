package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaCarrinhoMapper;

public class JpaClienteMapper {

    public static JpaClienteEntity toJpaEntity(Cliente cliente, JpaEnderecoEntity jpaEndereco, JpaCarrinhoEntity jpaCarrinho) {
        if (cliente == null) return null;
        JpaClienteEntity clienteJpa = new JpaClienteEntity(
                cliente.getId(),
                cliente.getUsername(),
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getSenha(),
                cliente.isUsuarioAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataNascimento(),
                cliente.getSexo(),
                jpaEndereco,
                cliente.getCpf(),
                jpaCarrinho
        );

        clienteJpa.getEndereco().setCliente(clienteJpa);

        clienteJpa.getCarrinho().setCliente(clienteJpa);

        return clienteJpa;
    }

    public static Cliente toEntity(JpaClienteEntity jpaCliente) {
        if (jpaCliente == null) return null;
        return new Cliente(
                jpaCliente.getId(),
                jpaCliente.getUserName(),
                jpaCliente.getNomeCompleto(),
                jpaCliente.getEmail(),
                jpaCliente.getSenha(),
                jpaCliente.isUsuarioAtivo(),
                jpaCliente.getDataCadastro(),
                jpaCliente.getDataNascimento(),
                jpaCliente.getSexo(),
                JpaEnderecoMapper.toEntity(jpaCliente.getEndereco()),
                jpaCliente.getCpf(),
                JpaCarrinhoMapper.toEntity(jpaCliente.getCarrinho())
        );
    }

    private JpaClienteMapper() {
    }
}
