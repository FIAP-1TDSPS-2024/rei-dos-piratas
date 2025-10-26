package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

public interface FuncionarioRepository {
    Page<Funcionario> listAll(int pageNumber, int pageSize);
    Funcionario findById(Long id);
    Funcionario create(Funcionario funcionario);
    Funcionario update(Funcionario funcionario);
    void delete(Long id);
}