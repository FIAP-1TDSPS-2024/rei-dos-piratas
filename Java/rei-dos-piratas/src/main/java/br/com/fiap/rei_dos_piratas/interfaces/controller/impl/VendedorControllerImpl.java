package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.VendedorService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.VendedorDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.VendedorController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorOutDto;

import java.util.List;

public class VendedorControllerImpl implements VendedorController {

    private final VendedorService service;

    public VendedorControllerImpl(VendedorService service) {
        this.service = service;
    }

    @Override
    public Page<VendedorOutDto> listAll(int pageNumber, int pageSize) {

        Page<Vendedor> vendedoresPage = this.service.listAll(pageNumber, pageSize);

        List<VendedorOutDto> vendedores = this.service
                .listAll(pageNumber, pageSize)
                .pageItems()
                .stream()
                .map(VendedorDtoMapper::toDto)
                .toList();

        return new Page<VendedorOutDto>(
                vendedoresPage.numberOfPages(),
                vendedoresPage.pageNumber(),
                vendedores);

    }

    @Override
    public VendedorOutDto findById(Long id) {
        Vendedor vendedor = this.service.findById(id);
        return VendedorDtoMapper.toDto(vendedor);
    }

    @Override
    public VendedorOutDto create(VendedorInDto dto) {
        Vendedor vendedor = VendedorDtoMapper.toEntity(dto);
        Vendedor novoVendedor = this.service.create(vendedor);
        return VendedorDtoMapper.toDto(novoVendedor);
    }

    @Override
    public VendedorOutDto update(Vendedor vendedor) {
        Vendedor updVendedor = this.service.update(vendedor);
        return VendedorDtoMapper.toDto(updVendedor);
    }

    @Override
    public void delete(Long id) {
        this.service.delete(id);
    }
}
