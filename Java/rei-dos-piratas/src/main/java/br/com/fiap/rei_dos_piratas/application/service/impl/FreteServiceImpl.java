package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete.ProdutoFreteDtoMapper;
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

    private final TokenService tokenService;
    private final ProdutoService produtoService;
    private final ObjectMapper mapper;
    private final Gson gson;
    private final CloseableHttpClient httpClient;

    public FreteServiceImpl(TokenService tokenService, ProdutoService produtoService, ObjectMapper mapper, Gson gson, CloseableHttpClient httpClient) {
        this.tokenService = tokenService;
        this.produtoService = produtoService;
        this.mapper = mapper;
        this.gson = gson;
        this.httpClient = httpClient;
    }

    @Override
    public List<FreteServiceDto> calcularFreteProdutos(String cepOrigem, String cepDestino, List<ItemProdutoPedido> itens){

        //URL melhor envio para calculo de fretes
        String url = System.getenv("ME_URL");
        url = url + "/api/v2/me/shipment/calculate";

        //Criar objeto para request
        ConsultaFreteServiceDto dto = new ConsultaFreteServiceDto(
                Map.of("postal_code", cepOrigem),
                Map.of("postal_code", cepDestino),
                itens
                        .stream()
                        .map(produto -> ProdutoFreteDtoMapper.toDto(produto.getProduto(), produto.getQuantidade()))
                        .toList());

        //request
        HttpPost request = new HttpPost(url);
        String jsonBody = gson.toJson(dto);

        //entity
        StringEntity stringEntity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json");

        // Authorization
        Token token = tokenService.findLastToken();
        request.setHeader("Authorization", "Bearer " + token.getToken());

        request.setHeader("Accept", "application/json");
        request.setHeader("User-Agent", "jonascamp2004@gmail.com");

        request.setEntity(stringEntity);

        //response
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new ApiExternaException("Erro temporário no cálculo de frete. Tente novamente mais tarde.");
        }

        HttpEntity entity = response.getEntity();

        if (entity != null){
            try {
                String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                return mapper.readValue(
                        json,
                        new TypeReference<List<FreteServiceDto>>() {}
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            throw new ResourceNotFoundException("Não existem serviços disponíveis para essa entrega");
        }
    }

    @Override
    public PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto pedidoRequest){
        //URL melhor envio para calculo de fretes
        String url = System.getenv("ME_URL");
        url = url + "/api/v2/me/cart";

        HttpPost request = new HttpPost(url);

        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(pedidoRequest);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter pedido para JSON", e);
        }

        StringEntity stringEntity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json");

        Token token = tokenService.findLastToken();
        request.setHeader("Authorization", "Bearer " + token.getToken());
        request.setHeader("Accept", "application/json");
        request.setHeader("User-Agent", "jonascamp2004@gmail.com");
        request.setEntity(stringEntity);

        CloseableHttpResponse response;

        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new ApiExternaException(
                    "Erro temporário na criação de frete do pedido. Tente novamente mais tarde."
            );
        }

        HttpEntity entity = response.getEntity();

        if (entity == null) {
            throw new ResourceNotFoundException("Não foi possível criar o pedido de frete");
        }

        try {
            String result = EntityUtils.toString(entity);
            System.out.println("Resultado: " + result);

            return mapper.readValue(result, PedidoFreteResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler resposta da API de frete", e);
        }
    }
}
