package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteAppClient;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete.ProdutoFreteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.token.TokenResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        return this.apiFrete.gerarEtiquetas(pedidos);
    }

    @Override
    public String imprimirEtiquetasPedidoFrete(List<String> pedidos) {
        return this.apiFrete.gerarLinkImpressaoEtiquetas(pedidos);
    }
}
