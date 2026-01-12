package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.endereco.EnderecoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.EnderecoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoOutDto;

import java.util.List;

public class EnderecoControllerImpl implements EnderecoController {

    private final EnderecoService service;

    public EnderecoControllerImpl(EnderecoService service) {
        this.service = service;
    }

    @Override
    public Page<EnderecoOutDto> findAll(int pageNumber, int pageSize) {
        Page<Endereco> enderecosPage = this.service.findAll(pageNumber, pageSize);

        List<EnderecoOutDto> produtos = enderecosPage
                .pageItems()
                .stream()
                .map(EnderecoDtoMapper::toDto)
                .toList();

        return new Page<EnderecoOutDto>(
                enderecosPage.numberOfPages(),
                enderecosPage.pageNumber(),
                produtos);
    }

    @Override
    public EnderecoOutDto findById(Long id) {
        Endereco endereco = this.service.findById(id);
        return EnderecoDtoMapper.toDto(endereco);
    }

    @Override
    public EnderecoOutDto save(EnderecoInDto endereco) {
        Endereco novoEndereco = EnderecoDtoMapper.toEntity(endereco);
        return EnderecoDtoMapper.toDto(this.service.save(novoEndereco));
    }

    @Override
    public void deactivate(Long id) {
        this.service.deactivate(id);
    }
}
