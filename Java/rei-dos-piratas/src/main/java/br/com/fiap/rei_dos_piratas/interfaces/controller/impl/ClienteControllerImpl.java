package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import jakarta.validation.Valid;

import java.util.List;

public class ClienteControllerImpl implements ClienteController {

    private final ClienteService service;

    public ClienteControllerImpl(ClienteService service) {
        this.service = service;
    }

    @Override
    public Page<ClienteOutDto> listAll(int pageNumber, int pageSize) {

        Page<Cliente> clientesPage = this.service.listAll(pageNumber, pageSize);

        List<ClienteOutDto> clientes = this.service
                .listAll(pageNumber, pageSize)
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
    public ClienteOutDto create(ClienteInDto dto) {
        Cliente cliente = ClienteDtoMapper.toEntity(dto);
        Cliente novoCliente = this.service.create(cliente);
        return ClienteDtoMapper.toDto(novoCliente);
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
