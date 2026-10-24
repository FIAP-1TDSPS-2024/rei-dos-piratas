package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CartaoClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio.CartaoClienteDtoMapper;

import java.util.List;

public class CartaoClienteControllerImpl implements CartaoClienteController {

    private final CartaoService service;

    public CartaoClienteControllerImpl(CartaoService service) {
        this.service = service;
    }

    @Override
    public List<CartaoClienteOutDto> listarCartoesDoClienteAtual() {
        return this.service.listarCartoesDoClienteAtual()
                .stream()
                .map(CartaoClienteDtoMapper::toDto)
                .toList();
    }
}
