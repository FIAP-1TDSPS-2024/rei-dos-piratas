package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaTokenEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaTokenEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenRepositoryImplTest {

    private JpaTokenEntityRepository repository;
    private TokenRepositoryImpl tokenRepository;

    @BeforeEach
    void setUp() {
        repository = mock(JpaTokenEntityRepository.class);
        tokenRepository = new TokenRepositoryImpl(repository);
    }

    @Test
    void save() {
        // Arrange
        Token token = new Token(
                "Token",
                "Refresh Token",
                2592000
        );

        when(repository.save(any(JpaTokenEntity.class))).thenReturn(new JpaTokenEntity());

        // Act
        Token result = tokenRepository.save(token);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(JpaTokenEntity.class));
    }

    @Test
    void findLastToken_shouldReturnMappedToken() {
        // Arrange
        JpaTokenEntity jpaEntity = new JpaTokenEntity();
        when(repository.findLastToken()).thenReturn(jpaEntity);

        // Act
        Token result = tokenRepository.findLastToken();

        // Assert
        assertNotNull(result);
        verify(repository).findLastToken();
    }
}