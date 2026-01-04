package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoOutDto;

public interface EnderecoController {
    Page<EnderecoOutDto> findAll(int pageNumber, int pageSize);
    EnderecoOutDto findById(Long id);
    EnderecoOutDto save(EnderecoInDto endereco);
    EnderecoOutDto update(Endereco endereco);
    void delete(Long id);
}
