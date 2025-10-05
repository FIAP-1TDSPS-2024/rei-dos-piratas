package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Cliente> listAll(int pageNumber, int pageSize) {
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Cliente findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + id);
        }
    }

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {
        return this.repository.create(cliente);
    }

    @Override
    @Transactional
    public Cliente update(Cliente cliente) {
        return this.repository.update(cliente);
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }
}
