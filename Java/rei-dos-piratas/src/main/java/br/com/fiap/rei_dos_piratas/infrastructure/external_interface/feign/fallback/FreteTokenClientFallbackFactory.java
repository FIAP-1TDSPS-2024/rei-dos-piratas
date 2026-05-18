package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.fallback;

import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteTokenClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory para o FreteTokenClient (renovação OAuth do Melhor Envio).
 * Quando o circuito está OPEN, lança exceção imediatamente — evita requisições
 * de frete que falhariam de qualquer forma sem um token válido.
 */
@Slf4j
@Component
public class FreteTokenClientFallbackFactory implements FallbackFactory<FreteTokenClient> {

    @Override
    public FreteTokenClient create(Throwable cause) {
        log.error("[CIRCUIT BREAKER] FreteTokenClient degradado. Renovação de token bloqueada. Causa: {}",
                cause.getMessage());

        return new FreteTokenClient() {
            @Override
            public TokenResponseDto renovarToken(TokenRequestDto request) {
                log.error("[FALLBACK] renovarToken indisponível — serviço de autenticação de frete inacessível.");
                throw new ServiceUnavailableException(
                        "Autenticação do serviço de frete temporariamente indisponível. Tente novamente em instantes.");
            }
        };
    }
}

