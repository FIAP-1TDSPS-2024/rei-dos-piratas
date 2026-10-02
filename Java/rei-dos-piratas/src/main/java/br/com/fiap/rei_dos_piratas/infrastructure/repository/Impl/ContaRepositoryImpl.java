package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class ContaRepositoryImpl implements ContaRepository {
    private final ClienteRepository clientes;
    private final FuncionarioRepository funcionarios;
    private final EntityManager entityManager;

    public ContaRepositoryImpl(ClienteRepository clientes, FuncionarioRepository funcionarios, EntityManager entityManager) {
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conta> findByEmail(String email) {
        Cliente cliente = clientes.findByEmail(email);
        Funcionario funcionario = funcionarios.findByEmail(email);
        if (cliente != null && funcionario != null) {
            throw new IllegalStateException("E-mail associado a mais de uma conta");
        }
        if (cliente != null) return Optional.of(conta(TipoConta.CLIENTE, cliente));
        return funcionario == null ? Optional.empty() : Optional.of(conta(TipoConta.FUNCIONARIO, funcionario));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conta> findByIdentidade(IdentidadeConta identidade) {
        try {
            Usuario usuario = identidade.tipo() == TipoConta.CLIENTE
                    ? clientes.findById(identidade.id()) : funcionarios.findById(identidade.id());
            return usuario == null ? Optional.empty() : Optional.of(conta(identidade.tipo(), usuario));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailOcupado(String email, IdentidadeConta propriaConta) {
        email = Usuario.normalizarEmail(email);
        return existe("CLIENTES", TipoConta.CLIENTE, email, propriaConta)
                || existe("FUNCIONARIOS", TipoConta.FUNCIONARIO, email, propriaConta);
    }

    private boolean existe(String tabela, TipoConta tipo, String email, IdentidadeConta propriaConta) {
        String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE LOWER(TRIM(email)) = :email";
        boolean excluir = propriaConta != null && propriaConta.tipo() == tipo;
        if (excluir) sql += " AND id <> :id";
        var query = entityManager.createNativeQuery(sql).setParameter("email", email);
        if (excluir) query.setParameter("id", propriaConta.id());
        return ((Number) query.getSingleResult()).longValue() > 0;
    }

    private Conta conta(TipoConta tipo, Usuario usuario) {
        return new Conta(new IdentidadeConta(tipo, usuario.getId()), usuario);
    }
}
