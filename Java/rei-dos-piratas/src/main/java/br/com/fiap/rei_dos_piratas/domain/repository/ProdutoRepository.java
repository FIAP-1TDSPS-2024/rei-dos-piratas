package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;

public interface ProdutoRepository {
    /** Só produtos ativos (os desativados continuam acessíveis pelo id, para pedidos e devoluções). */
    Page<Produto> listAtivos(int pageNumber, int pageSize);
    /** Só os desativados, para o funcionário poder reativar. */
    Page<Produto> listDesativados(int pageNumber, int pageSize);
    Produto findById(Long id);
    Produto create(Produto produto);
    Produto update(Produto produto);
}
