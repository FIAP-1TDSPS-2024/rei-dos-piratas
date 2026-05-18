package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteAppClient;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete.ProdutoFreteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.ImpressaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Implementação do serviço de frete com resiliência via Resilience4j.
 *
 * Circuit Breaker "melhorEnvioApp":
 *   - OPEN após 50% de falhas nas últimas 10 chamadas
 *   - Permanece OPEN por 130s (> 120s do auto-scaling) antes de testar HALF-OPEN
 *   - Fallback: FreteAppClientFallbackFactory trata degradação por operação
 *
 * Bulkhead "melhorEnvioApp":
 *   - Isola até 10 threads concorrentes para a API de frete
 *   - Evita que lentidão externa bloqueie o pool principal da aplicação
 *
 * Retry "melhorEnvioApp":
 *   - Tenta 2x antes de registrar como falha no Circuit Breaker
 *   - Cobre falhas transitórias de rede durante o scaling
 */
@Slf4j
public class FreteServiceImpl implements FreteService {

    private final FreteAppClient apiFrete;
    private final EnderecoService enderecoService;

    public FreteServiceImpl(FreteAppClient apiFrete, EnderecoService enderecoService) {
        this.apiFrete = apiFrete;
        this.enderecoService = enderecoService;
    }

    @Override
    @CircuitBreaker(name = "melhorEnvioApp")
    @Bulkhead(name = "melhorEnvioApp")
    @Retry(name = "melhorEnvioApp")
    public List<FreteServiceDto> calcularFreteProdutos(String cepDestino, List<ItemProdutoPedido> itens) {
        log.info("[FRETE] Calculando frete para CEP destino: {}", cepDestino);
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();

        ConsultaFreteServiceDto dto = new ConsultaFreteServiceDto(
                Map.of("postal_code", enderecoEmpresa.getCep()),
                Map.of("postal_code", cepDestino),
                itens.stream()
                        .map(produto -> ProdutoFreteDtoMapper.toDto(produto.getProduto(), produto.getQuantidade()))
                        .toList());

        List<FreteServiceDto> resultado = this.apiFrete.calcularFrete(dto);
        log.info("[FRETE] Cálculo concluído — {} opções retornadas.", resultado.size());
        return resultado;
    }

    @Override
    @CircuitBreaker(name = "melhorEnvioApp")
    @Bulkhead(name = "melhorEnvioApp")
    @Retry(name = "melhorEnvioApp")
    public PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto pedidoRequest) {
        log.info("[FRETE] Criando pedido de frete no Melhor Envio.");
        PedidoFreteResponseDto response = this.apiFrete.criarPedidoFrete(pedidoRequest);
        log.info("[FRETE] Pedido de frete criado com sucesso. ID: {}", response);
        return response;
    }

    @Override
    @CircuitBreaker(name = "melhorEnvioApp")
    @Bulkhead(name = "melhorEnvioApp")
    @Retry(name = "melhorEnvioApp")
    public CompraFreteResponseDto organizarPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Organizando/pagando {} pedido(s) de frete.", pedidos.size());
        CompraFreteResponseDto response = this.apiFrete.pagarPedidoFrete(pedidos);
        log.info("[FRETE] Organização concluída.");
        return response;
    }

    @Override
    @CircuitBreaker(name = "melhorEnvioApp")
    @Bulkhead(name = "melhorEnvioApp")
    @Retry(name = "melhorEnvioApp")
    public GeracaoEtiquetasResponseDto gerarEtiquetasPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Gerando etiquetas para {} pedido(s).", pedidos.size());
        return this.apiFrete.gerarEtiquetas(Map.of("orders", pedidos));
    }

    @Override
    @CircuitBreaker(name = "melhorEnvioApp")
    @Bulkhead(name = "melhorEnvioApp")
    @Retry(name = "melhorEnvioApp")
    public ImpressaoEtiquetasResponseDto imprimirEtiquetasPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Gerando link de impressão para {} pedido(s).", pedidos.size());
        return this.apiFrete.gerarLinkImpressaoEtiquetas(Map.of("orders", pedidos));
    }
}
