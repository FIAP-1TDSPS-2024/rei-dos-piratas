package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
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

import java.util.List;
import java.util.Map;

public class FreteServiceImpl implements FreteService {

    private final FreteAppClient apiFrete;

    public FreteServiceImpl(FreteAppClient apiFrete) {
        this.apiFrete = apiFrete;
    }

    @Override
    public List<FreteServiceDto> calcularFreteProdutos(String cepOrigem, String cepDestino, List<ItemProdutoPedido> itens){
        //Criar objeto para request
        ConsultaFreteServiceDto dto = new ConsultaFreteServiceDto(
                Map.of("postal_code", cepOrigem),
                Map.of("postal_code", cepDestino),
                itens
                        .stream()
                        .map(produto -> ProdutoFreteDtoMapper.toDto(produto.getProduto(), produto.getQuantidade()))
                        .toList());

        return this.apiFrete.calcularFrete(dto);
    }

    @Override
    public PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto pedidoRequest){
        return this.apiFrete.criarPedidoFrete(pedidoRequest);
    }

    @Override
    public CompraFreteResponseDto organizarPedidoFrete(List<String> pedidos) {
        return this.apiFrete.pagarPedidoFrete(pedidos);
    }

    @Override
    public GeracaoEtiquetasResponseDto gerarEtiquetasPedidoFrete(List<String> pedidos) {
        return this.apiFrete.gerarEtiquetas(Map.of("orders", pedidos));
    }

    @Override
    public ImpressaoEtiquetasResponseDto imprimirEtiquetasPedidoFrete(List<String> pedidos) {
        return this.apiFrete.gerarLinkImpressaoEtiquetas(Map.of("orders", pedidos));
    }
}
