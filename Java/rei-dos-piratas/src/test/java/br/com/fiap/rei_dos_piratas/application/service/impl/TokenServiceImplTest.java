package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceImplTest {

    private TokenServiceImpl service;
    private TokenRepository repository;
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;
    private Gson gson;

    @BeforeEach
    void setUp() {
        this.repository = mock(TokenRepository.class);
        this.httpClient = mock(CloseableHttpClient.class);
        this.gson = new Gson();
        this.service = new TokenServiceImpl(repository, gson, httpClient);
        this.response = mock(CloseableHttpResponse.class);
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
    void deveGerarNovoTokenComSucesso() throws Exception {

        String json = """
        {
          "access_token": "abc",
          "refresh_token": "def",
          "expires_in": 2592000
        }
        """;

        HttpEntity entity = new StringEntity(json, StandardCharsets.UTF_8);

        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(response);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Token token = service.gerarNovoToken("refresh");

        assertNotNull(token);
        verify(repository).save(any());
    }


}