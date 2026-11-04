package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.TokenResponseDto;
import lombok.extern.slf4j.Slf4j;

/**
 * Token OAuth do Melhor Envio usado pelo interceptor do Feign: devolve o último token gravado
 * e o renova pelo refresh token quando venceu. Detalhe do adaptador; o domínio não o conhece.
 */
@Slf4j
public class ProvedorTokenMelhorEnvio {

    private final TokenRepository repository;
    private final FreteTokenClient api;
    private final CredenciaisMelhorEnvio credenciais;

    public ProvedorTokenMelhorEnvio(TokenRepository repository, FreteTokenClient api, CredenciaisMelhorEnvio credenciais) {
        this.repository = repository;
        this.api = api;
        this.credenciais = credenciais;
    }

    public Token tokenValido() {
        try {
            Token token = this.repository.findLastToken();
            return token.isTokenValid() ? token : this.renovar(token.getRefreshToken());
        } catch (Exception e) {
            log.error("[MELHOR-ENVIO-TOKEN] Não foi possível obter token de frete: {}", e.getMessage());
            throw new ResourceNotFoundException("Não é possível conectar nos serviços de frete. " + e.getMessage());
        }
    }

    private Token renovar(String refreshToken) {
        log.info("[MELHOR-ENVIO-TOKEN] Renovando token OAuth");
        TokenResponseDto resposta = this.api.renovarToken(new TokenRequestDto(
                "refresh_token",
                this.credenciais.clientId(),
                this.credenciais.clientSecret(),
                this.credenciais.redirectUri(),
                refreshToken));

        Token salvo = this.repository.save(new Token(resposta.access_token(), resposta.refresh_token(), resposta.expires_in()));
        log.info("[MELHOR-ENVIO-TOKEN] Token renovado com sucesso");
        return salvo;
    }
}
