package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.TokenResponseDto;
import br.com.fiap.rei_dos_piratas.infrastructure.config.feign.FreteTokenFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "frete-token", name = "frete-api", configuration = FreteTokenFeignConfig.class)
public interface FreteTokenClient {

    @PostMapping("/oauth/token")
    TokenResponseDto renovarToken(@RequestBody TokenRequestDto request);
}
