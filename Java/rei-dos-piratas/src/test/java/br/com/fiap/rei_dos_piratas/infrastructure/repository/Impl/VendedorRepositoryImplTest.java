package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaVendedorEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaVendedorMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaVendedorEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VendedorRepositoryImplTest {

    private JpaVendedorEntityRepository repository;
    private VendedorRepositoryImpl vendedorRepository;

    @BeforeEach
    void setUp() {
        repository = mock(JpaVendedorEntityRepository.class);
        vendedorRepository = new VendedorRepositoryImpl(repository);
    }

    @Test
    void listAll_shouldReturnMappedPage() {
        // Arrange
        JpaVendedorEntity jpaEntity = new JpaVendedorEntity();
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(jpaEntity)));

        // Act
        Page<Vendedor> result = vendedorRepository.listAll(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.pageItems().size());
        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    void findById_shouldReturnMappedVendedor() {
        // Arrange
        JpaVendedorEntity jpaEntity = new JpaVendedorEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(jpaEntity));

        // Act
        Vendedor result = vendedorRepository.findById(1L);

        // Assert
        assertNotNull(result);
        verify(repository).findById(1L);
    }

    @Test
    void create_shouldSaveWhenNoDuplicates() {
        // Arrange
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER);

        when(repository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(repository.findFirstByEmail("jonas@gmail.com")).thenReturn(null);
        when(repository.save(any(JpaVendedorEntity.class))).thenReturn(new JpaVendedorEntity());

        // Act
        Vendedor result = vendedorRepository.create(vendedor);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(JpaVendedorEntity.class));
    }

    @Test
    void create_shouldThrowWhenUserNameExists() {
        Vendedor vendedor = new Vendedor(
                "duplicado",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER);

        when(repository.findFirstByUserName("duplicado")).thenReturn(new JpaVendedorEntity());

        //Assert
        assertThrows(UniqueKeyDuplicatedException.class, () -> vendedorRepository.create(vendedor));
        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenEmailExists() {
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER);

        when(repository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(repository.findFirstByEmail("jonas@gmail.com")).thenReturn(new JpaVendedorEntity());

        // Act & Assert
        assertThrows(UniqueKeyDuplicatedException.class, () -> vendedorRepository.create(vendedor));
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldSaveVendedor() {
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER);
        when(repository.save(any(JpaVendedorEntity.class))).thenReturn(new JpaVendedorEntity());

        // Act
        Vendedor result = vendedorRepository.update(vendedor);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(JpaVendedorEntity.class));
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        // Act
        vendedorRepository.delete(10L);

        // Assert
        verify(repository).deleteById(10L);
    }
}
