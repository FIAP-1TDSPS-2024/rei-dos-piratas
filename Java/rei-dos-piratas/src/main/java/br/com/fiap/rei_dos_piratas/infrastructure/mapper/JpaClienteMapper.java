package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;

public class JpaClienteMapper {

    public static JpaClienteEntity toJpaEntity(Cliente cliente) {
        if (cliente == null) return null;
        return new JpaClienteEntity(
                cliente.getId(),
                cliente.getUserName(),
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getSenha(),
                cliente.isUsuarioAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataNascimento(),
                cliente.getSexo(),
                JpaEnderecoMapper.toJpaEntity(cliente.getEndereco(), cliente),
                cliente.getCpf()
        );
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
