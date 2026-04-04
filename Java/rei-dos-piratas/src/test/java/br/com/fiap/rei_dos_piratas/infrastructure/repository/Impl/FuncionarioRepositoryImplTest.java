package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FuncionarioRepositoryImplTest {

    private JpaFuncionarioEntityRepository repository;
    private FuncionarioRepositoryImpl funcionarioRepository;

    @BeforeEach
    void setUp() {
        repository = mock(JpaFuncionarioEntityRepository.class);
        funcionarioRepository = new FuncionarioRepositoryImpl(repository);
    }

    @Test
    void listAll_shouldReturnMappedPage() {
        // Arrange
        JpaFuncionarioEntity jpaEntity = new JpaFuncionarioEntity();
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(jpaEntity)));

        // Act
        Page<Funcionario> result = funcionarioRepository.listAll(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.pageItems().size());
        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    void findById_shouldReturnMappedFuncionario() {
        // Arrange
        JpaFuncionarioEntity jpaEntity = new JpaFuncionarioEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(jpaEntity));

        // Act
        Funcionario result = funcionarioRepository.findById(1L);

        // Assert
        assertNotNull(result);
        verify(repository).findById(1L);
    }

    @Test
    void create_shouldSaveWhenNoDuplicates() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER,
                BigDecimal.valueOf(1000.00));

        when(repository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(repository.findFirstByEmail("jonas@gmail.com")).thenReturn(null);
        when(repository.save(any(JpaFuncionarioEntity.class))).thenReturn(new JpaFuncionarioEntity());

        // Act
        Funcionario result = funcionarioRepository.create(funcionario);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(JpaFuncionarioEntity.class));
    }

    @Test
    void create_shouldThrowWhenUserNameExists() {
        Funcionario funcionario = new Funcionario(
                "duplicado",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER,
                BigDecimal.valueOf(1000.00));

        when(repository.findFirstByUserName("duplicado")).thenReturn(new JpaFuncionarioEntity());

        //Assert
        assertThrows(UniqueKeyDuplicatedException.class, () -> funcionarioRepository.create(funcionario));
        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenEmailExists() {
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER,
                BigDecimal.valueOf(1000.00));

        when(repository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(repository.findFirstByEmail("jonas@gmail.com")).thenReturn(new JpaFuncionarioEntity());

        // Act & Assert
        assertThrows(UniqueKeyDuplicatedException.class, () -> funcionarioRepository.create(funcionario));
        verify(repository, never()).save(any());
    }
}
