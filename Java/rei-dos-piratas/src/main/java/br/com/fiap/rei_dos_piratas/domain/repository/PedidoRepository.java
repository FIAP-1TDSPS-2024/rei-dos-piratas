package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;

public interface PedidoRepository {
    Page<Pedido> listAllByClient(int pageNumber, int pageSize, Long clienteId);
    Pedido findById(Long id);
    Pedido create(Pedido pedido);
    Pedido update(Pedido pedido);
}
