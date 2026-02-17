package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete.ProdutoFreteDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.ItemProdutoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaItemProdutoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FreteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;

import java.util.List;

public class FreteControllerImpl implements FreteController {

    private final FreteService service;

    private final ProdutoService produtoService;

    public FreteControllerImpl(FreteService service, ProdutoService produtoService) {
        this.service = service;
        this.produtoService = produtoService;
    }

    @Override
    public List<FreteServiceDto> calcularFreteProdutos(ConsultaFreteDto dto) {
        return this.service.calcularFreteProdutos(
                dto.cepOrigem(),
                dto.cepDestino(),
                dto.itens()
                        .stream()
                        .map(produto -> new ItemProdutoPedido(produtoService.findById(produto.produtoId()), produto.quantidade()))
                        .toList());
    }
}
