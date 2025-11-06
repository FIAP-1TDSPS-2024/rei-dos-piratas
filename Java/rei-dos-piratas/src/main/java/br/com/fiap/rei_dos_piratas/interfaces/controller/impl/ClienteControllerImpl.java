package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;

import java.util.List;

public class ClienteControllerImpl implements ClienteController {

    private final ClienteService service;

    public ClienteControllerImpl(ClienteService service) {
        this.service = service;
    }

    @Override
    public Page<ClienteOutDto> listAll(int pageNumber, int pageSize) {

        Page<Cliente> clientesPage = this.service.listAll(pageNumber, pageSize);

        List<ClienteOutDto> clientes = clientesPage
                .pageItems()
                .stream()
                .map(ClienteDtoMapper::toDto)
                .toList();

        return new Page<ClienteOutDto>(
                clientesPage.numberOfPages(),
                clientesPage.pageNumber(),
                clientes);
    }

    @Override
    public ClienteOutDto findById(Long id) {
        Cliente cliente = this.service.findById(id);
        return ClienteDtoMapper.toDto(cliente);
    }

    @Override
    public ClienteOutDto update(Cliente cliente) {
        Cliente updCliente = this.service.update(cliente);
        return ClienteDtoMapper.toDto(updCliente);
    }

    @Override
    public void delete(Long id) {
        this.service.delete(id);
    }
}
