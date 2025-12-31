package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.FreteServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreteServiceConfig {
    @Bean
    public FreteService freteService(TokenService tokenService, ProdutoService produtoService, ObjectMapper mapper, Gson gson, CloseableHttpClient httpClient) {
        return new FreteServiceImpl(tokenService, produtoService, mapper, gson, httpClient);
    }
}
