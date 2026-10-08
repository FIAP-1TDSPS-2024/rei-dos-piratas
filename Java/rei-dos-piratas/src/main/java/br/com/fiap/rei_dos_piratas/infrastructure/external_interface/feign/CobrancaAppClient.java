package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign;

import br.com.fiap.rei_dos_piratas.infrastructure.config.feign.CobrancaFeignConfig;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "cobranca-app", name = "cobranca-api", configuration = CobrancaFeignConfig.class)
public interface CobrancaAppClient {

    @PostMapping("/v3/customers")
    ClienteCobrancaResponseDto criarNovoCliente(@RequestBody ClienteCobrancaRequestDto request);

    @PostMapping("/v3/payments/")
    PagamentoCobrancaResponseDto criarNovaCobranca(@RequestBody PagamentoCobrancaRequestDto request);

    @GetMapping("/v3/payments/{id}/pixQrCode")
    PixQrCodeResponseDto obterQrCodePix(@PathVariable("id") String id);

    //@PostMapping("/v3/payments/{id}/refund")
    //void estornarCobranca(@PathVariable("id") String id, @RequestBody EstornoCobrancaRequestDto request);
}
