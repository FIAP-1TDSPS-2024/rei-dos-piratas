package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FuncionarioRepositoryImplTest {

    @MockBean
    private JpaFuncionarioEntityRepository repository;

    private FuncionarioRepositoryImpl funcionarioRepository;
    private Perfil perfilPadrao;

    @BeforeEach
    void setUp() {
        repository = mock(JpaFuncionarioEntityRepository.class);
        funcionarioRepository = new FuncionarioRepositoryImpl(repository);
        perfilPadrao = new Perfil(1L, "FUNCIONARIO", "Perfil de funcionário", null);
    }

    @Test
    void listAll_shouldReturnFuncionarios() {
        // Arrange
        org.springframework.data.domain.Page<JpaFuncionarioEntity> mockPage =
            new org.springframework.data.domain.PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        // Act
        Page<Funcionario> result = funcionarioRepository.listAll(0, 10);

        // Assert
        assertNotNull(result);
    }

    @Test
    void findAllByUsuarioAtivoTrue_shouldReturnActiveFuncionarios() {
        // Arrange
        org.springframework.data.domain.Page<JpaFuncionarioEntity> mockPage =
            new org.springframework.data.domain.PageImpl<>(Collections.emptyList());
        when(repository.findAllByUsuarioAtivoTrue(any(PageRequest.class))).thenReturn(mockPage);

        // Act
        Page<Funcionario> result = funcionarioRepository.findAllByUsuarioAtivoTrue(0, 10);

        // Assert
        assertNotNull(result);
    }

    @Test
    void create_shouldSaveWhenNoDuplicates() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                perfilPadrao,
                BigDecimal.valueOf(1000.00));

        when(repository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(repository.findFirstByEmail("jonas@gmail.com")).thenReturn(null);
        when(repository.save(any(JpaFuncionarioEntity.class))).thenReturn(new JpaFuncionarioEntity());

        // Act
        Funcionario result = funcionarioRepository.create(funcionario);

        // Assert
        assertNotNull(result);
        // Add more specific assertions based on your mapper implementation
    }

    @Test
    void create_shouldThrowExceptionWhenUsernameExists() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "existinguser",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                perfilPadrao,
                BigDecimal.valueOf(1000.00));

        when(repository.findFirstByUserName("existinguser")).thenReturn(new JpaFuncionarioEntity());

        // Act & Assert
        assertThrows(UniqueKeyDuplicatedException.class, () -> funcionarioRepository.create(funcionario));
    }

    @Test
    void create_shouldThrowExceptionWhenEmailExists() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "existing@gmail.com",
                "SenhaSegura123",
                perfilPadrao,
                BigDecimal.valueOf(1000.00));

        when(repository.findFirstByUserName("jonasdasneves")).thenReturn(null);
        when(repository.findFirstByEmail("existing@gmail.com")).thenReturn(new JpaFuncionarioEntity());

        // Act & Assert
        assertThrows(UniqueKeyDuplicatedException.class, () -> funcionarioRepository.create(funcionario));
    }
}
