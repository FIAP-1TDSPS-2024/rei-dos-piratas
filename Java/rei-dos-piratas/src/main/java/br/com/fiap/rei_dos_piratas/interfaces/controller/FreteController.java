package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;

import java.util.List;

public interface FreteController {
    List<FreteServiceDto> calcularFreteProdutos(ConsultaFreteDto consultaFreteDto);
}
