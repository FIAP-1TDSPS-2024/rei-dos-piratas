package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository repository;

    public FuncionarioServiceImpl(FuncionarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Funcionario> listAll(int pageNumber, int pageSize) {
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Funcionario findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um vendedor com o id " + id);
        }
    }

    @Override
    @Transactional
    public Funcionario create(Funcionario funcionario) {
        return this.repository.create(funcionario);
    }

    @Override
    @Transactional
    public Funcionario update(Funcionario funcionario) {
        Funcionario funcionarioAtualizado = this.repository.update(funcionario);

        if (funcionarioAtualizado == null){
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + funcionario.getId() + ". Crie um novo cliente.");
        }

        return funcionarioAtualizado;
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }
}
