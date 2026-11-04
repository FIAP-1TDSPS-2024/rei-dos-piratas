package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitacaoEnvioTest {

    private final DadosEmpresa loja = new DadosEmpresa(1L, "Rei dos Piratas LTDA", "Rei dos Piratas", "12345678901234",
            "contato@reidospiratas.com.br", "1133334444", "reidospiratas.com.br", "SP", "00000000000191", "4721100");
    private final Endereco enderecoLoja = new Endereco(1L, 123, "12345678", "Avenida Brasil", "Centro", true,
            new Cidade(1L, "São Paulo", new Estado(1L, "São Paulo", "SP")), "Brasil", "BR", null);

    private Cliente cliente() {
        return new Cliente(1L, "joaosilva", "João Silva", "joao@gmail.com", "SenhaSegura123", true, LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de cliente", null), LocalDate.of(1990, 5, 15), SexoEnum.M,
                "52998224725", "11992131234", new Carrinho(1L, new ArrayList<>()));
    }

    private Pedido pedido() {
        Cliente cliente = cliente();
        Produto produto = new Produto();
        produto.setNome("One Piece Vol. 1");
        produto.setPreco(new BigDecimal("45.90"));
        produto.setProfundidade(new BigDecimal("2.0"));
        produto.setPeso(new BigDecimal("0.2"));
        Endereco entrega = new Endereco(2L, 45, "01001000", "Praça da Sé", "Sé", true,
                new Cidade(2L, "São Paulo", new Estado(1L, "São Paulo", "SP")), "Brasil", "BR", cliente);
        Pedido pedido = new Pedido(cliente, entrega, new ArrayList<>(List.of(new ItemProdutoPedido(produto, 2))), 3L, TipoPagamentoEnum.PIX);
        pedido.setId(10L);
        pedido.setValorTotal(new BigDecimal("121.80"));
        return pedido;
    }

    @Test
    void deveTerALojaComoRemetente() {
        ParteEnvio remetente = SolicitacaoEnvio.para(pedido(), enderecoLoja, loja).remetente();

        assertThat(remetente.nome()).isEqualTo("Rei dos Piratas");
        assertThat(remetente.email()).isEqualTo("contato@reidospiratas.com.br");
        assertThat(remetente.telefone()).isEqualTo("1133334444");
        assertThat(remetente.cpf()).isNull();
        assertThat(remetente.cnpj()).isEqualTo("12345678901234");
        assertThat(remetente.inscricaoEstadual()).isEqualTo("00000000000191");
        assertThat(remetente.cnae()).isEqualTo("4721100");
        assertThat(remetente.logradouro()).isEqualTo("Avenida Brasil");
        assertThat(remetente.numero()).isEqualTo("123");
        assertThat(remetente.bairro()).isEqualTo("Centro");
        assertThat(remetente.cidade()).isEqualTo("São Paulo");
        assertThat(remetente.cep()).isEqualTo("12345678");
        assertThat(remetente.uf()).isEqualTo("SP");
    }

    @Test
    void deveTerOClienteComoDestinatarioNoEnderecoDeEntrega() {
        ParteEnvio destinatario = SolicitacaoEnvio.para(pedido(), enderecoLoja, loja).destinatario();

        assertThat(destinatario.nome()).isEqualTo("João Silva");
        assertThat(destinatario.email()).isEqualTo("joao@gmail.com");
        assertThat(destinatario.telefone()).isEqualTo("11992131234");
        assertThat(destinatario.cpf()).isEqualTo("52998224725");
        assertThat(destinatario.cnpj()).isNull();
        assertThat(destinatario.logradouro()).isEqualTo("Praça da Sé");
        assertThat(destinatario.numero()).isEqualTo("45");
        assertThat(destinatario.cep()).isEqualTo("01001000");
    }

    @Test
    void deveLevarServicoProdutosVolumeESeguroPeloValorDoPedido() {
        SolicitacaoEnvio envio = SolicitacaoEnvio.para(pedido(), enderecoLoja, loja);

        assertThat(envio.servico()).isEqualTo(3L);
        assertThat(envio.produtos()).containsExactly(new ProdutoEnvio("One Piece Vol. 1", 2, new BigDecimal("45.90")));
        assertThat(envio.volume()).isEqualTo(Volume.para(pedido().getProdutosAdicionados()));
        assertThat(envio.valorSeguro()).isEqualByComparingTo("121.80");
        assertThat(envio.plataforma()).isEqualTo("Rei dos Piratas");
        assertThat(envio.lembrete()).isEqualTo("Entrega do pedido 10");
    }

    @Test
    void naoDeveExporDadosPessoaisNoToString() {
        ParteEnvio destinatario = SolicitacaoEnvio.para(pedido(), enderecoLoja, loja).destinatario();

        assertThat(destinatario.toString()).doesNotContain("52998224725", "joao@gmail.com", "11992131234", "Praça da Sé");
    }

    @Test
    void envioReversoDeveUsarOEnvioOriginalEOsContatosDoCliente() {
        // Arrange
        Pedido pedido = pedido();
        UUID envioOriginal = UUID.randomUUID();
        pedido.setPedidoFrete(envioOriginal);
        Devolucao devolucao = new Devolucao();
        devolucao.setPedido(pedido);
        devolucao.setValorTotal(new BigDecimal("91.80"));
        ItemDevolucao item = new ItemDevolucao();
        item.setItemPedido(pedido.getProdutosAdicionados().getFirst());
        item.setQuantidade(1);
        devolucao.setItens(List.of(item));

        // Act
        SolicitacaoEnvioReverso reverso = SolicitacaoEnvioReverso.para(devolucao, 7L);

        // Assert
        assertThat(reverso.servico()).isEqualTo(7L);
        assertThat(reverso.emailRemetente()).isEqualTo("joao@gmail.com");
        assertThat(reverso.telefoneRemetente()).isEqualTo("11992131234");
        assertThat(reverso.valorSeguro()).isEqualByComparingTo("91.80");
        assertThat(reverso.envioOriginal()).isEqualTo(envioOriginal);
        // Volume pelos itens devolvidos (1 unidade), não pelo pedido inteiro
        assertThat(reverso.volume()).isEqualTo(Volume.para(List.of(
                new ItemProdutoPedido(pedido.getProdutosAdicionados().getFirst().getProduto(), 1))));
    }
}
