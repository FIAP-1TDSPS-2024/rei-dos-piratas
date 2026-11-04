package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep;

import br.com.fiap.rei_dos_piratas.infrastructure.config.feign.ViaCepFeignConfig;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep.dto.ViaCepRespostaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(contextId = "viacep", name = "viacep-api", configuration = ViaCepFeignConfig.class)
public interface ViaCepClient {

    @GetMapping("/ws/{cep}/json/")
    ViaCepRespostaDto consultar(@PathVariable("cep") String cep);
}
