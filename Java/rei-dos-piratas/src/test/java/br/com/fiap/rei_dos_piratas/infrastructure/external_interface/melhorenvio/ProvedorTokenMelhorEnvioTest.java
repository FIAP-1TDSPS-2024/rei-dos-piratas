package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.TokenResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Comportamentos do antigo TokenServiceImplTest, agora no adaptador do Melhor Envio. */
class ProvedorTokenMelhorEnvioTest {

    private TokenRepository repository;
    private FreteTokenClient api;
    private ProvedorTokenMelhorEnvio provedor;

    @BeforeEach
    void setUp() {
        repository = mock(TokenRepository.class);
        api = mock(FreteTokenClient.class);
        provedor = new ProvedorTokenMelhorEnvio(repository, api,
                new CredenciaisMelhorEnvio("client-id", "client-secret", "https://loja/callback"));
    }

    private Token tokenVencido() {
        // Expira em 0 dias - 5 de folga: já nasce vencido
        return new Token("tok_velho", "refresh_velho", 0);
    }

    @Test
    void tokenValidoDeveSerReutilizadoSemRenovar() {
        // Arrange
        Token valido = new Token("tok_atual", "refresh_atual", 2_592_000);
        when(repository.findLastToken()).thenReturn(valido);

        // Act
        Token resultado = provedor.tokenValido();

        // Assert
        assertThat(resultado).isSameAs(valido);
        verifyNoInteractions(api);
        verify(repository, never()).save(any());
    }

    @Test
    void tokenVencidoDeveSerRenovadoPeloRefreshTokenESalvo() {
        // Arrange
        when(repository.findLastToken()).thenReturn(tokenVencido());
        when(api.renovarToken(any())).thenReturn(new TokenResponseDto("Bearer", 2_592_000, "tok_novo", "refresh_novo"));
        when(repository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Token resultado = provedor.tokenValido();

        // Assert
        assertThat(resultado.getToken()).isEqualTo("tok_novo");
        assertThat(resultado.getRefreshToken()).isEqualTo("refresh_novo");
        assertThat(resultado.isTokenValid()).isTrue();
        verify(repository).save(resultado);
    }

    @Test
    void renovacaoDeveUsarAsCredenciaisDaConfiguracao() {
        // Arrange
        when(repository.findLastToken()).thenReturn(tokenVencido());
        when(api.renovarToken(any())).thenReturn(new TokenResponseDto("Bearer", 2_592_000, "tok_novo", "refresh_novo"));
        when(repository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        provedor.tokenValido();

        // Assert
        ArgumentCaptor<TokenRequestDto> captor = ArgumentCaptor.forClass(TokenRequestDto.class);
        verify(api).renovarToken(captor.capture());
        TokenRequestDto requisicao = captor.getValue();
        assertThat(requisicao.grant_type()).isEqualTo("refresh_token");
        assertThat(requisicao.client_id()).isEqualTo("client-id");
        assertThat(requisicao.client_secret()).isEqualTo("client-secret");
        assertThat(requisicao.redirect_uri()).isEqualTo("https://loja/callback");
        assertThat(requisicao.refresh_token()).isEqualTo("refresh_velho");
    }

    @Test
    void falhaAoObterOTokenDeveLancarResourceNotFound() {
        // Arrange
        when(repository.findLastToken()).thenThrow(new RuntimeException("Erro"));

        // Act & Assert
        assertThatThrownBy(() -> provedor.tokenValido()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void credenciaisNaoDevemAparecerNoToString() {
        assertThat(new CredenciaisMelhorEnvio("client-id", "client-secret", "https://loja/callback").toString())
                .doesNotContain("client-id", "client-secret");
    }
}
