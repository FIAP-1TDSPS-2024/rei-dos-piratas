package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.gateway.EnderecoCep;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoCepTest {

    private Endereco digitadoPeloCliente() {
        Endereco endereco = new Endereco();
        endereco.setCep("01001000");
        endereco.setNumero(10);
        endereco.setLogradouro("Rua Inventada");
        endereco.setBairro("Bairro Inventado");
        endereco.setCidade(new Cidade(null, "Cidade Inventada", new Estado(null, "Estado Inventado", "XX")));
        return endereco;
    }

    @Test
    void dadosDoCepSubstituemOsDigitadosPeloCliente() {
        Endereco endereco = digitadoPeloCliente();

        endereco.aplicarCep(new EnderecoCep("01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP"));

        assertThat(endereco.getLogradouro()).isEqualTo("Praça da Sé");
        assertThat(endereco.getBairro()).isEqualTo("Sé");
        assertThat(endereco.getCidade().getNome()).isEqualTo("São Paulo");
        assertThat(endereco.getCidade().getEstado().getNome()).isEqualTo("São Paulo");
        assertThat(endereco.getCidade().getEstado().getSigla()).isEqualTo("SP");
        assertThat(endereco.getNumero()).isEqualTo(10);
    }

    @Test
    void cepGeralMantemRuaEBairroDoClienteMasUsaCidadeEEstadoOficiais() {
        Endereco endereco = digitadoPeloCliente();

        endereco.aplicarCep(new EnderecoCep("13600000", "", "", "Araras", "São Paulo", "SP"));

        assertThat(endereco.getLogradouro()).isEqualTo("Rua Inventada");
        assertThat(endereco.getBairro()).isEqualTo("Bairro Inventado");
        assertThat(endereco.getCidade().getNome()).isEqualTo("Araras");
        assertThat(endereco.getCidade().getEstado().getSigla()).isEqualTo("SP");
    }

    @Test
    void enderecoSemCidadeRecebeACidadeDoCep() {
        Endereco endereco = new Endereco();
        endereco.setCep("01001000");

        endereco.aplicarCep(new EnderecoCep("01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP"));

        assertThat(endereco.getCidade().getNome()).isEqualTo("São Paulo");
        assertThat(endereco.getCidade().getEstado().getSigla()).isEqualTo("SP");
    }
}
