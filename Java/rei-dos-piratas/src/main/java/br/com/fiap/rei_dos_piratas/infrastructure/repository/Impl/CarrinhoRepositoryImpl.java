package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.repository.CarrinhoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaCarrinhoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaCarrinhoEntityRepository;

public class CarrinhoRepositoryImpl implements CarrinhoRepository {

    private final JpaCarrinhoEntityRepository repository;

    public CarrinhoRepositoryImpl(JpaCarrinhoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Carrinho update(Carrinho carrinho) {

        JpaCarrinhoEntity JpaCarrinhoEntity = JpaCarrinhoMapper.toJpaEntity(carrinho);
        JpaCarrinhoEntity novoCarrinhoJpa = this.repository.save(JpaCarrinhoEntity);
        Carrinho novoCarrinho = JpaCarrinhoMapper.toEntity(novoCarrinhoJpa);
        return novoCarrinho;
    }
}
