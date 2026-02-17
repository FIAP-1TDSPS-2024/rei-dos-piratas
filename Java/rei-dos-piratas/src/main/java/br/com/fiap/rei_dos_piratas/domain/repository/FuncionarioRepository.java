package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

import java.util.Optional;

public interface FuncionarioRepository {
    Page<Funcionario> listAll(int pageNumber, int pageSize);
    Funcionario findById(Long id);
    Funcionario create(Funcionario funcionario);
    Funcionario update(Funcionario funcionario);
    Page<Funcionario> findAllByUsuarioAtivoTrue(int pageNumber, int pageSize);
    Funcionario findByUsername(String username);
    Funcionario findByEmail(String email);
}