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

        List<FreteServiceDto> resultado = this.apiFrete.calcularFrete(dto);
        log.info("[FRETE] Cálculo concluído — {} opções retornadas.", resultado.size());
        return resultado;
    }

    @Override
    public PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto pedidoRequest) {
        log.info("[FRETE] Criando pedido de frete no Melhor Envio.");
        PedidoFreteResponseDto response = this.apiFrete.criarPedidoFrete(pedidoRequest);
        log.info("[FRETE] Pedido de frete criado com sucesso. ID: {}", response);
        return response;
    }

    @Override
    public CompraFreteResponseDto organizarPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Organizando/pagando {} pedido(s) de frete.", pedidos.size());
        CompraFreteResponseDto response = this.apiFrete.pagarPedidoFrete(pedidos);
        log.info("[FRETE] Organização concluída.");
        return response;
    }

    @Override
    public GeracaoEtiquetasResponseDto gerarEtiquetasPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Gerando etiquetas para {} pedido(s).", pedidos.size());
        return this.apiFrete.gerarEtiquetas(Map.of("orders", pedidos));
    }

    @Override
    public ImpressaoEtiquetasResponseDto imprimirEtiquetasPedidoFrete(List<String> pedidos) {
        log.info("[FRETE] Gerando link de impressão para {} pedido(s).", pedidos.size());
        return this.apiFrete.gerarLinkImpressaoEtiquetas(Map.of("orders", pedidos));
    }
}
