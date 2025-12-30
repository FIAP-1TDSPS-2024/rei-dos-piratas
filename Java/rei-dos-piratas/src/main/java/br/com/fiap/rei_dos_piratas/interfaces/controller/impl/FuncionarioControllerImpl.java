package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.FuncionarioDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;

import java.util.List;

public class FuncionarioControllerImpl implements FuncionarioController {

    private final FuncionarioService service;

    public FuncionarioControllerImpl(FuncionarioService service) {
        this.service = service;
    }

    @Override
    public Page<FuncionarioOutDto> listAll(int pageNumber, int pageSize) {

        Page<Funcionario> vendedoresPage = this.service.listAll(pageNumber, pageSize);

        List<FuncionarioOutDto> vendedores = vendedoresPage
                .pageItems()
                .stream()
                .map(FuncionarioDtoMapper::toDto)
                .toList();

        return new Page<FuncionarioOutDto>(
                vendedoresPage.numberOfPages(),
                vendedoresPage.pageNumber(),
                vendedores);

    }

    @Override
    public FuncionarioOutDto findById(Long id) {
        Funcionario funcionario = this.service.findById(id);
        return FuncionarioDtoMapper.toDto(funcionario);
    }

    @Override
    public FuncionarioOutDto create(FuncionarioInDto dto) {
        Funcionario funcionario = FuncionarioDtoMapper.toEntity(dto);
        Funcionario novoFuncionario = this.service.create(funcionario);
        return FuncionarioDtoMapper.toDto(novoFuncionario);
    }

    @Override
    public FuncionarioOutDto update(Funcionario funcionario) {
        Funcionario updFuncionario = this.service.update(funcionario);
        return FuncionarioDtoMapper.toDto(updFuncionario);
    }

    @Override
    public FuncionarioOutDto ativarDesativar(Long id) {
        Funcionario updFuncionario = this.service.ativarDesativar(id);
        return FuncionarioDtoMapper.toDto(updFuncionario);
    }
}
