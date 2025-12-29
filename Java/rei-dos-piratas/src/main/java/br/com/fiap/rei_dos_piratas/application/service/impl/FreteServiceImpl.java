package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.ProdutoFreteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.TokenResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.ConsultaFreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.FreteServiceDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FreteServiceImpl implements FreteService {

    private final TokenService tokenService;
    private final ProdutoService produtoService;

    public FreteServiceImpl(TokenService tokenService, ProdutoService produtoService) {
        this.tokenService = tokenService;
        this.produtoService = produtoService;
    }

    @Override
    public List<FreteServiceDto> calcularFreteProdutos(String cepOrigem, String cepDestino, List<ItemProdutoInDto> itens){

        //URL melhor envio para calculo de fretes
        String url = System.getenv("ME_URL");
        url = url + "/api/v2/me/shipment/calculate";

        //Criando um objeto Gson
        Gson gson = new Gson();

        //Criar objeto para request
        ConsultaFreteServiceDto dto = new ConsultaFreteServiceDto(
                Map.of("postal_code", cepOrigem),
                Map.of("postal_code", cepDestino),
                itens
                        .stream()
                        .map(produto -> ProdutoFreteDtoMapper.toDto(produtoService.findById(produto.produtoId()), produto.quantidade()))
                        .toList());

        //request
        HttpPost request = new HttpPost(url);
        String jsonBody = gson.toJson(dto);

        //entity
        StringEntity stringEntity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json");

        // Authorization
        Token token = tokenService.findLastToken();
        request.setHeader("Authorization", "Bearer" + token.getToken());

        request.setHeader("Accept", "application/json");

        request.setEntity(stringEntity);

        //client
        CloseableHttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().build();

        //response
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpEntity entity = response.getEntity();

        if (entity != null){
            String result = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<FreteServiceDto> services = mapper.readValue((JsonParser) entity, new TypeReference<List<FreteServiceDto>>() {});
                return services;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            throw new ResourceNotFoundException("Não foi possível gerar um novo token com o refresh token.");
        }
    }
}
