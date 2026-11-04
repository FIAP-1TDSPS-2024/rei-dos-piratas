package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.gateway.EnderecoCep;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayCep;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Contrato do adaptador com o ViaCEP: Feign real (com o Jackson do projeto) contra um WireMock. */
@SpringBootTest
class GatewayCepViaCepTest {

    @RegisterExtension
    static WireMockExtension viaCep = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void apontarParaOWireMock(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.viacep.url", viaCep::baseUrl);
        registry.add("spring.cloud.openfeign.client.config.viacep.read-timeout", () -> "1000");
    }

    @Autowired GatewayCep gateway;

    @Test
    void deveTraduzirOEnderecoDoCep() {
        viaCep.stubFor(get("/ws/01001000/json/").willReturn(okJson("""
                {"cep": "01001-000", "logradouro": "Praça da Sé", "complemento": "lado ímpar",
                 "bairro": "Sé", "localidade": "São Paulo", "uf": "SP", "estado": "São Paulo",
                 "ibge": "3550308", "ddd": "11"}
                """)));

        Optional<EnderecoCep> endereco = gateway.consultar("01001000");

        assertThat(endereco).contains(new EnderecoCep("01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP"));
    }

    @Test
    void deveRetornarVazioQuandoOCepNaoExiste() {
        viaCep.stubFor(get("/ws/99999999/json/").willReturn(okJson("{\"erro\": \"true\"}")));

        assertThat(gateway.consultar("99999999")).isEmpty();
    }

    @Test
    void cepGeralVemSemLogradouroNemBairro() {
        viaCep.stubFor(get("/ws/13600000/json/").willReturn(okJson("""
                {"cep": "13600-000", "logradouro": "", "bairro": "", "localidade": "Araras",
                 "uf": "SP", "estado": "São Paulo"}
                """)));

        EnderecoCep endereco = gateway.consultar("13600000").orElseThrow();

        assertThat(endereco.geral()).isTrue();
        assertThat(endereco.cidade()).isEqualTo("Araras");
    }

    @Test
    void deveTraduzirErroDoFornecedorEmApiExterna() {
        viaCep.stubFor(get("/ws/01001000/json/").willReturn(serverError()));

        assertThatThrownBy(() -> gateway.consultar("01001000")).isInstanceOf(ApiExternaException.class);
    }

    @Test
    void deveTraduzirTimeoutEmApiExterna() {
        viaCep.stubFor(get("/ws/01001000/json/").willReturn(okJson("{}").withFixedDelay(3000)));

        assertThatThrownBy(() -> gateway.consultar("01001000")).isInstanceOf(ApiExternaException.class);
    }

    @Test
    void cepForaDoFormatoNaoChamaOFornecedor() {
        assertThat(gateway.consultar("123")).isEmpty();
        assertThat(viaCep.getAllServeEvents()).isEmpty();
    }
}
