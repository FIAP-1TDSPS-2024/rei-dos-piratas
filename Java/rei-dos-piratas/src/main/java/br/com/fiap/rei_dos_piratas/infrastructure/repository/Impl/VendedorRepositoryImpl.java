package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.VendedorRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaVendedorMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaVendedorEntityRepository;
import org.springframework.data.domain.Pageable;

public class VendedorRepositoryImpl implements VendedorRepository {

    private final JpaVendedorEntityRepository repository;

    public VendedorRepositoryImpl(JpaVendedorEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Vendedor> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                Pageable
                        .ofSize(pageSize)
                        .withPage(pageNumber)
        ).map(JpaVendedorMapper::toEntity));
    }

    @Override
    public Vendedor findById(Long id) {
        return JpaVendedorMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }

    @Override
    public Vendedor create(Vendedor vendedor) {

        //Verificação para evitar e-mail ou userNames duplicados em banco
        if (this.repository.findFirstByUserName(vendedor.getUserName()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um vendedor com esse nome de usuário. Insira um novo nome de usuário válido");
        }
        if(this.repository.findFirstByEmail(vendedor.getEmail()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um vendedor registrado com esse email. Insira um novo e-mail válido");
        }

        return JpaVendedorMapper.toEntity(
                this.repository.save(
                        JpaVendedorMapper.toJpaEntity(vendedor)));
    }

    @Override
    public Vendedor update(Vendedor vendedor) {
        return JpaVendedorMapper.toEntity(
                this.repository.save(
                        JpaVendedorMapper.toJpaEntity(vendedor)));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
