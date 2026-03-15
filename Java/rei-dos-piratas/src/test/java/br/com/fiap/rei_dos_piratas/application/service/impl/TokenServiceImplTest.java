package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteTokenClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceImplTest {

    private TokenServiceImpl service;
    private TokenRepository repository;
    private FreteTokenClient apiFrete;

    @BeforeEach
    void setUp() {
        this.repository = mock(TokenRepository.class);
        this.apiFrete = mock(FreteTokenClient.class);
        this.service = new TokenServiceImpl(repository, apiFrete);
    }

    @Test
    void deveRetornarTokenQuandoTokenForValido() {
        Token token = mock(Token.class);

        when(token.isTokenValid()).thenReturn(true);
        when(repository.findLastToken()).thenReturn(token);

        Token result = service.findLastToken();

        assertEquals(token, result);
        verify(repository).findLastToken();
        verify(repository, never()).save(any());
    }

    @Test
    void deveGerarNovoTokenQuandoTokenForInvalido() {
        Token tokenInvalido = mock(Token.class);
        Token novoToken = mock(Token.class);

        when(tokenInvalido.isTokenValid()).thenReturn(false);
        when(tokenInvalido.getRefreshToken()).thenReturn("refresh");
        when(repository.findLastToken()).thenReturn(tokenInvalido);

        TokenServiceImpl spyService = spy(service);
        doReturn(novoToken).when(spyService).gerarNovoToken("refresh");

        Token result = spyService.findLastToken();

        assertEquals(novoToken, result);
    }

    @Test
    void deveLancarExceptionQuandoRepositorioFalhar() {
        when(repository.findLastToken())
                .thenThrow(new RuntimeException("Erro"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.findLastToken());
    }

    @Test
    void deveGerarNovoTokenComSucesso() {
        // Arrange
        String refreshToken = "refresh_token_test";
        TokenResponseDto tokenResponse = new TokenResponseDto(
                "Bearer",
                2592000,
                "new_access_token",
                "new_refresh_token"
        );

        Token expectedToken = new Token(
                tokenResponse.access_token(),
                tokenResponse.refresh_token(),
                tokenResponse.expires_in()
        );

        when(apiFrete.renovarToken(any(TokenRequestDto.class))).thenReturn(tokenResponse);
        when(repository.save(any(Token.class))).thenReturn(expectedToken);

        // Act
        Token result = service.gerarNovoToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result);
        verify(apiFrete).renovarToken(any(TokenRequestDto.class));
        verify(repository).save(any(Token.class));
    }


}