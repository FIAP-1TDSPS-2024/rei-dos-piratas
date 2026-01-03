package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaCidadeEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEstadoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
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
        // Create properly initialized entities with all required nested objects
        JpaEstadoEntity estado = new JpaEstadoEntity(1L, "São Paulo", "SP");
        JpaCidadeEntity cidade = new JpaCidadeEntity(1L, "São Paulo", estado);
        JpaEnderecoEntity endereco = new JpaEnderecoEntity();
        endereco.setId(1L);
        endereco.setCidade(cidade);
        endereco.setNumero(123);
        endereco.setCep("12345678");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setBairro("Bela Vista");

        JpaCarrinhoEntity carrinho = new JpaCarrinhoEntity();
        carrinho.setId(1L);
        carrinho.setProdutosAdicionados(new ArrayList<>());

        JpaClienteEntity entity = new JpaClienteEntity(
                1L,
                "testuser",
                "Test User",
                "test@email.com",
                "password123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 1, 1),
                SexoEnum.M,
                "12345678900",
                carrinho
        );

        when(jpaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        Page<Cliente> result = repository.listAll(0, 10);

        assertNotNull(result);
        verify(jpaRepository).findAll(any(Pageable.class));
    }

    @Test
    void findById_shouldReturnClienteWhenFound() {
        // Create properly initialized entities with all required nested objects
        JpaEstadoEntity estado = new JpaEstadoEntity(1L, "São Paulo", "SP");
        JpaCidadeEntity cidade = new JpaCidadeEntity(1L, "São Paulo", estado);
        JpaEnderecoEntity endereco = new JpaEnderecoEntity();
        endereco.setId(1L);
        endereco.setCidade(cidade);
        endereco.setNumero(123);
        endereco.setCep("12345678");
        endereco.setLogradouro("Avenida Paulista");
        endereco.setBairro("Bela Vista");

        JpaCarrinhoEntity carrinho = new JpaCarrinhoEntity();
        carrinho.setId(1L);
        carrinho.setProdutosAdicionados(new ArrayList<>());

        JpaClienteEntity entity = new JpaClienteEntity(
                1L,
                "testuser",
                "Test User",
                "test@email.com",
                "password123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 1, 1),
                SexoEnum.M,
                "12345678900",
                carrinho
        );

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
                "BR"
        );

        Cliente cliente = new Cliente(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                "12345678978"
        );

        // Create a properly initialized JpaCarrinhoEntity
        JpaCarrinhoEntity mockCarrinho = new JpaCarrinhoEntity();
        mockCarrinho.setId(1L);
        mockCarrinho.setProdutosAdicionados(new ArrayList<>());

        // Create a properly initialized JpaClienteEntity with carrinho using constructor
        JpaClienteEntity savedEntity = new JpaClienteEntity(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                "12345678978",
                mockCarrinho
        );

        when(jpaRepository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(jpaRepository.findFirstByCpf("12345678978")).thenReturn(null);
        when(jpaRepository.findFirstByEmail("jonas@gmail.com")).thenReturn(null);
        when(jpaRepository.save(any(JpaClienteEntity.class))).thenReturn(savedEntity);

        Cliente createdCliente = repository.create(cliente);

        assertNotNull(createdCliente);
        verify(jpaRepository).save(any(JpaClienteEntity.class));
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        repository.delete(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
