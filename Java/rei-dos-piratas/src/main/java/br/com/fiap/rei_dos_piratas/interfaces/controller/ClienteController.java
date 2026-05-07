package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;

public interface ClienteController {

    Page<ClienteOutDto> listAll(int pageNumber, int pageSize);

    ClienteOutDto findById(Long id);

    ClienteOutDto update(ClienteInDto cliente);

    void delete();
}
