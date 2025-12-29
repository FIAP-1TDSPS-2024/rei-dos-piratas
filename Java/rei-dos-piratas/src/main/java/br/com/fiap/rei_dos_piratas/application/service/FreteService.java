package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.FreteServiceDto;

import java.util.List;

public interface FreteService {
    List<FreteServiceDto> calcularFreteProdutos(String cepOrigem, String cepDestino, List<ItemProdutoInDto> itens);
}
