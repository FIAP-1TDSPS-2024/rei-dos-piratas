package br.com.fiap.rei_dos_piratas.infrastructure.config.httpClient;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create()
                .disableRedirectHandling()
                .build();
    }
}
