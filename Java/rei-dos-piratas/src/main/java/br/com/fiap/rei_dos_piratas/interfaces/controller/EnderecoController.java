package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoOutDto;

public interface EnderecoController {
    Page<EnderecoOutDto> findAll(int pageNumber, int pageSize);
    EnderecoOutDto findById(Long id);
    EnderecoOutDto save(EnderecoInDto endereco);
    void deactivate(Long id);
}
