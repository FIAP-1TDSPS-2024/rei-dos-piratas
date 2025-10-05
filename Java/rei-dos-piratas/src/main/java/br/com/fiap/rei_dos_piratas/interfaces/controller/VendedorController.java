package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorOutDto;

public interface VendedorController {

    Page<VendedorOutDto> listAll(int pageNumber, int pageSize);

    VendedorOutDto findById(Long id);

    VendedorOutDto create(VendedorInDto dto);

    VendedorOutDto update(Vendedor vendedor);

    void delete(Long id);
}
