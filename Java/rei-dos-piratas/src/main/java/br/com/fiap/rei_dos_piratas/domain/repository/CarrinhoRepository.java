package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;

public interface CarrinhoRepository {
    Carrinho findByCliente(Long clienteId);
    Carrinho update(Carrinho carrinho);
}
