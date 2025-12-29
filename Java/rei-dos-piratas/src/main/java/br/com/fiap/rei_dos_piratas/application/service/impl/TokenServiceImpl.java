package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.interfaces.dto.TokenRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.TokenResponseDto;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenServiceImpl implements TokenService {

    private final TokenRepository repository;

    public TokenServiceImpl(TokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Token findLastToken() {
        try{
            Token token = repository.findLastToken();

            if (token.isTokenValid()) {
                return token;
            }
            else{
                return this.gerarNovoToken(token.getRefreshToken());
            }
        }
        catch (Exception e){
            throw new ResourceNotFoundException("Não é possível conectar nos serviços de frete.");
        }
    }

    @Override
    public Token gerarNovoToken(String refreshToken) {

        //URL melhor envio para fretes
        String url = System.getenv("ME_URL");
        url = url + "/oauth/token";

        //Criando um objeto Gson
        Gson gson = new Gson();

        //Definição de informações de cliente melhor envio para renovação de token
        String clientId = System.getenv("ME_CLIENT_ID");
        String clientSecret = System.getenv("ME_SECRET");
        String redirectUri = System.getenv("ME_REDIRECT_URI");

        //Criar objeto para request
        TokenRequestDto dto = new TokenRequestDto("refresh_token", clientId, clientSecret, redirectUri, refreshToken);

        //request
        HttpPost request = new HttpPost(url);
        String jsonBody = gson.toJson(dto);

        //entity
        StringEntity StringEntity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        StringEntity.setContentType("application/json");
        request.setEntity(StringEntity);

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
                result = EntityUtils.toString(entity);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            TokenResponseDto tokenResponse = gson.fromJson(result, TokenResponseDto.class);
            Token token = new Token(tokenResponse.access_token(), tokenResponse.refresh_token(), tokenResponse.expires_in());
            return this.repository.save(token);
        }
        else{
            throw new ResourceNotFoundException("Não foi possível gerar um novo token com o refresh token.");
        }
    }
}
