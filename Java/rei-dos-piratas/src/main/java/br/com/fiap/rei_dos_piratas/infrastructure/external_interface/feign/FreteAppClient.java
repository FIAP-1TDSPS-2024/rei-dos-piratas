package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign;

import br.com.fiap.rei_dos_piratas.infrastructure.config.feign.FreteFeignConfig;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(contextId = "frete-app", name = "frete-api", configuration = FreteFeignConfig.class)
public interface FreteAppClient {

    @PostMapping("/api/v2/me/shipment/calculate")
    List<FreteServiceDto> calcularFrete(@RequestBody ConsultaFreteServiceDto request);

    @PostMapping("/api/v2/me/cart")
    PedidoFreteResponseDto criarPedidoFrete(@RequestBody PedidoFreteRequestDto request);
}
