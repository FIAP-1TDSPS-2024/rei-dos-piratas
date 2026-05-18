package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign;

import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.fallback.FreteTokenClientFallbackFactory;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "frete-token", name = "frete-api",
        fallbackFactory = FreteTokenClientFallbackFactory.class)
public interface FreteTokenClient {

    @PostMapping("/oauth/token")
    TokenResponseDto renovarToken(@RequestBody TokenRequestDto request);
}
