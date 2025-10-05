package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaEnderecoEntity;

public class JpaClienteMapper {

    public static JpaClienteEntity toJpaEntity(Cliente cliente, JpaEnderecoEntity jpaEndereco) {
        if (cliente == null) return null;
        JpaClienteEntity clienteJpa = new JpaClienteEntity(
                cliente.getId(),
                cliente.getUserName(),
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getSenha(),
                cliente.isUsuarioAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataNascimento(),
                cliente.getSexo(),
                jpaEndereco,
                cliente.getCpf()
        );

        clienteJpa.getEndereco().setCliente(clienteJpa);

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
                jpaCliente.getCpf()
        );
    }

    private JpaClienteMapper() {
    }
}
