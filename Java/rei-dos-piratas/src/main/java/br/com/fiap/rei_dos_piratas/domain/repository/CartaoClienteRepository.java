package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;

import java.util.List;
import java.util.Optional;

public interface CartaoClienteRepository {
    CartaoCliente findById(Long id);
    Optional<CartaoCliente> findByIdOptional(Long id);
    Optional<CartaoCliente> findByClienteIdAndToken(Long clienteId, String token);
    CartaoCliente create(CartaoCliente cartao);
    List<CartaoCliente> findAllByClienteId(Long clienteId);
}
