package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;

public interface ClienteController {

    Page<ClienteOutDto> listAll(int pageNumber, int pageSize);

    ClienteOutDto findById(Long id);

    ClienteOutDto create(ClienteInDto dto);

    ClienteOutDto update(Cliente cliente);

    void delete(Long id);
}
