package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteAppClient;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete.ProdutoFreteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.ImpressaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class FreteServiceImpl implements FreteService {

    private final FreteAppClient apiFrete;
    private final EnderecoService enderecoService;

    public FreteServiceImpl(FreteAppClient apiFrete, EnderecoService enderecoService) {
        this.apiFrete = apiFrete;
        this.enderecoService = enderecoService;
    }

    @Override
    public List<FreteServiceDto> calcularFreteProdutos(String cepDestino, List<ItemProdutoPedido> itens) {
        log.info("[FRETE] Calculando frete para CEP destino: {}", cepDestino);
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();

        ConsultaFreteServiceDto dto = new ConsultaFreteServiceDto(
                Map.of("postal_code", enderecoEmpresa.getCep()),
                Map.of("postal_code", cepDestino),
                itens.stream()
                        .map(produto -> ProdutoFreteDtoMapper.toDto(produto.getProduto(), produto.getQuantidade()))
                        .toList());

        try {
            List<FreteServiceDto> resultado = this.apiFrete.calcularFrete(dto);
            log.info("[FRETE] Cálculo concluído — {} opções retornadas.", resultado.size());
            return resultado;
        } catch (ApiExternaException e) {
            log.error("[FRETE] Falha ao calcular frete para CEP {}: {}", cepDestino, e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[FRETE] Erro de comunicação ao calcular frete para CEP {}: {}", cepDestino, e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de frete ao calcular opções de entrega. Tente novamente.");
        }
    }

    @Override
    public PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto pedidoRequest) {
        log.info("[FRETE] Criando pedido de frete no Melhor Envio.");
        try {
            PedidoFreteResponseDto response = this.apiFrete.criarPedidoFrete(pedidoRequest);
            log.info("[FRETE] Pedido de frete criado com sucesso. ID: {}", response);
            return response;
        } catch (ApiExternaException e) {
            log.error("[FRETE] Falha ao criar pedido de frete: {}", e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[FRETE] Erro de comunicação ao criar pedido de frete: {}", e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de frete ao criar o pedido. Tente novamente.");
        }
    }

    @Override
    public CompraFreteResponseDto organizarPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Organizando/pagando {} pedido(s) de frete.", pedidos.size());
        try {
            CompraFreteResponseDto response = this.apiFrete.pagarPedidoFrete(pedidos);
            log.info("[FRETE] Organização concluída.");
            return response;
        } catch (ApiExternaException e) {
            log.error("[FRETE] Falha ao organizar pedidos de frete {}: {}", pedidos, e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[FRETE] Erro de comunicação ao organizar pedidos de frete {}: {}", pedidos, e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de frete ao organizar pedidos. Tente novamente.");
        }
    }

    @Override
    public GeracaoEtiquetasResponseDto gerarEtiquetasPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Gerando etiquetas para {} pedido(s).", pedidos.size());
        try {
            return this.apiFrete.gerarEtiquetas(Map.of("orders", pedidos));
        } catch (ApiExternaException e) {
            log.error("[FRETE] Falha ao gerar etiquetas para pedidos {}: {}", pedidos, e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[FRETE] Erro de comunicação ao gerar etiquetas para pedidos {}: {}", pedidos, e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de frete ao gerar etiquetas. Tente novamente.");
        }
    }

    @Override
    public ImpressaoEtiquetasResponseDto imprimirEtiquetasPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Gerando link de impressão para {} pedido(s).", pedidos.size());
        try {
            return this.apiFrete.gerarLinkImpressaoEtiquetas(Map.of("orders", pedidos));
        } catch (ApiExternaException e) {
            log.error("[FRETE] Falha ao gerar link de impressão para pedidos {}: {}", pedidos, e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[FRETE] Erro de comunicação ao gerar link de impressão para pedidos {}: {}", pedidos, e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de frete ao gerar link de impressão. Tente novamente.");
        }
    }
}
