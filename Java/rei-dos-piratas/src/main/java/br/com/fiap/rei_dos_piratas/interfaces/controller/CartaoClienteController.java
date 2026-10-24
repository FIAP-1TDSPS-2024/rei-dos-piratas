package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoClienteOutDto;

import java.util.List;

public interface CartaoClienteController {
    /** Cartões ativos do cliente logado. */
    List<CartaoClienteOutDto> listarCartoesDoClienteAtual();
}
