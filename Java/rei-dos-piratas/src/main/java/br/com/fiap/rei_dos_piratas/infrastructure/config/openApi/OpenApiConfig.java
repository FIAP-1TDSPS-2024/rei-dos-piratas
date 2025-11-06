package br.com.fiap.rei_dos_piratas.infrastructure.config.openApi;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("basicScheme",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(new Info()
                        .title("Rei Dos Piratas")
                        .description("API para gerenciamento do e-commerce de mangás Rei dos Piratas, com gerenciamento de produtos, pedidos, clientes e funcionários")
                        .contact(new Contact().name("Jonas Oliveira").email("rm561144@fiap.com.br").url("https://github.com/jonasdasneves"))
                        .version("Versão 1.0"));
    }
}
