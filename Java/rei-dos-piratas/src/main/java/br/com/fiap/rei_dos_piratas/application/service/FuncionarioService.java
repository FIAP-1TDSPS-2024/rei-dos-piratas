package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

public interface FuncionarioService {
    Page<Funcionario> listAll(int pageNumber, int pageSize);

    Funcionario findById(Long id);

    Funcionario create(Funcionario funcionario);

    Funcionario update(Funcionario funcionario);

    Funcionario ativarDesativar(Long id);
}
