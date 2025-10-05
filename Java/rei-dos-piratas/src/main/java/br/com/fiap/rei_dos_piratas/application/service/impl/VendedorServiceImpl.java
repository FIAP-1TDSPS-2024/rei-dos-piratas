package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.VendedorService;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.VendedorRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

public class VendedorServiceImpl implements VendedorService {

    private final VendedorRepository repository;

    public VendedorServiceImpl(VendedorRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Vendedor> listAll(int pageNumber, int pageSize) {
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Vendedor findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um vendedor com o id " + id);
        }
    }

    @Override
    @Transactional
    public Vendedor create(Vendedor vendedor) {
        return this.repository.create(vendedor);
    }

    @Override
    @Transactional
    public Vendedor update(Vendedor vendedor) {
        return this.repository.update(vendedor);
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }
}
