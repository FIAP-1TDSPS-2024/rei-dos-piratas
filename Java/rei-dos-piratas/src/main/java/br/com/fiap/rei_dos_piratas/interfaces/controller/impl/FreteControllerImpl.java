package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FreteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;

import java.util.List;

public class FreteControllerImpl implements FreteController {

    private final FreteService service;

    public FreteControllerImpl(FreteService service) {
        this.service = service;
    }

    @Override
    public List<FreteServiceDto> calcularFreteProdutos(ConsultaFreteDto dto) {
        return this.service.calcularFreteProdutos(dto.cepOrigem(), dto.cepDestino(), dto.itens());
    }
}
