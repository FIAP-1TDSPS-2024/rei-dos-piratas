package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;

public interface FuncionarioController {

    Page<FuncionarioOutDto> listAll(int pageNumber, int pageSize);

    FuncionarioOutDto findById(Long id);

    FuncionarioOutDto create(FuncionarioInDto dto);

    FuncionarioOutDto update(Funcionario funcionario);

    FuncionarioOutDto ativarDesativar(Long id);
}
