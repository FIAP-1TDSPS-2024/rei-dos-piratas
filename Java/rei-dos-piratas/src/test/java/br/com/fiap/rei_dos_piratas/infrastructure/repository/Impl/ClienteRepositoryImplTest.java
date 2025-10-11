package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaEnderecoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteRepositoryImplTest {

    private JpaClienteEntityRepository jpaRepository;
    private ClienteRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaClienteEntityRepository.class);
        repository = new ClienteRepositoryImpl(jpaRepository);
    }

    @Test
    void listAll_shouldReturnPageOfClientes() {
        JpaClienteEntity entity = new JpaClienteEntity();
        when(jpaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        Page<Cliente> result = repository.listAll(0, 10);

        assertNotNull(result);
        verify(jpaRepository).findAll(any(Pageable.class));
    }

    @Test
    void findById_shouldReturnClienteWhenFound() {
        JpaClienteEntity entity = new JpaClienteEntity();
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Cliente result = repository.findById(1L);

        assertNotNull(result);
        verify(jpaRepository).findById(1L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> repository.findById(1L));
    }

    @Test
    void create_shouldThrowWhenUserNameExists() {
        //O que
        Endereco enderecoParaCriar = new Endereco(
                null,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoParaCriar,
                "12345678978");

        when(jpaRepository.findFirstByUserName("jonasdasneves")).thenReturn(new JpaClienteEntity());

        assertThrows(UniqueKeyDuplicatedException.class, () -> repository.create(cliente));
    }

    @Test
    void create_shouldSaveClienteWhenNoDuplicates() {
        //O que
        Endereco enderecoParaCriar = new Endereco(
                null,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoParaCriar,
                "12345678978");

        when(jpaRepository.save(any(JpaClienteEntity.class))).thenReturn(new JpaClienteEntity());

        Cliente result = repository.create(cliente);

        assertNotNull(result);
        verify(jpaRepository).save(any(JpaClienteEntity.class));
    }

    @Test
    void update_shouldSaveAndReturnUpdatedCliente() {
        //O que
        Endereco enderecoParaCriar = new Endereco(
                null,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoParaCriar,
                "12345678978");

        when(jpaRepository.save(any(JpaClienteEntity.class))).thenReturn(new JpaClienteEntity());

        Cliente result = repository.update(cliente);

        assertNotNull(result);
        verify(jpaRepository).save(any(JpaClienteEntity.class));
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        repository.delete(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
