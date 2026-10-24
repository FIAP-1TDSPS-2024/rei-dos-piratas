package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PagamentoPersistenciaIntegrationTest {

    @Autowired EntityManager em;
    @Autowired PedidoRepository pedidos;
    @Autowired CartaoClienteRepository cartoes;
    @Autowired ClienteRepository clientes;
    @Autowired EnderecoRepository enderecos;
    @Autowired ProdutoRepository produtos;
    @Autowired RegraParcelamento regraParcelamento;

    @org.junit.jupiter.api.Test
    void deveRetornarOptionalVazioQuandoCartaoNaoExiste() {
        org.junit.jupiter.api.Assertions.assertTrue(cartoes.findByIdOptional(-1L).isEmpty());
    }

    void sql(String sql) { em.createNativeQuery(sql).executeUpdate(); }
    Object valor(String sql) { return em.createNativeQuery(sql).getSingleResult(); }
    void sincronizar() { em.flush(); em.clear(); }

    @BeforeEach
    void preparar() {
        sql("INSERT INTO CARRINHOS (id) VALUES (9701)");
        sql("""
            INSERT INTO CLIENTES (id,user_name,nome_completo,email,senha,usuario_ativo,data_cadastro,
                perfil_id,data_nascimento,sexo,cpf,celular,carrinho_id)
            VALUES (9701,'cliente_pagamento','Cliente Pagamento','pagamento@example.com','test',true,CURRENT_DATE,
                (SELECT id FROM PERFIS WHERE nome='CLIENT'),'1990-01-01','M','52998224725','11987654321',9701)
            """);
        sql("""
            INSERT INTO ENDERECO (id,numero,cep,logradouro,bairro,endereco_ativo,cidade_id,cliente_id)
            VALUES (9701,100,'01001000','Rua Pagamento','Centro',true,(SELECT MIN(id) FROM CIDADES),9701)
            """);
        sql("""
            INSERT INTO PRODUTOS (id,nome,descricao,autor,categoria,endereco_imagem,preco,preco_original,
                estoque,altura,largura,profundidade,peso,condicao,funcionario_id)
            VALUES (9701,'Produto Pagamento','Produto para testar pagamentos','Autor Teste',
                'AVENTURA','https://example.com/manga.jpg',100.00,100.00,10,20,14,2,0.30,'NOVO',1)
            """);
        sincronizar();
    }

    /** Pedido gravado como o serviço faz: com tipo de pagamento, o pagamento PENDENTE nasce junto. */
    private Pedido novoPedidoGravado(TipoPagamentoEnum tipo) {
        Pedido pedido = new Pedido(clientes.findById(9701L), enderecos.findById(9701L),
                List.of(new ItemProdutoPedido(produtos.findById(9701L), 2)), 1L, tipo);
        pedido.setValorFrete(new BigDecimal("21.19"));
        pedido.setValorTotal(new BigDecimal("221.19"));
        if (pedido.getPagamento() != null) {
            pedido.getPagamento().setValor(pedido.getValorTotal());
        }
        Pedido criado = pedidos.create(pedido);
        sincronizar();
        return criado;
    }

    private CartaoCliente cartaoGravado() {
        CartaoCliente cartao = cartoes.create(new CartaoCliente(null, clientes.findById(9701L), "tok_persistencia",
                "1234", "VISA", true, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        sincronizar();
        return cartao;
    }

    // ---- Pedido 1-1 Pagamento ----

    @Test
    void pedidoCriadoComTipoDePagamentoGravaPagamentoPendente() {
        Pedido pedido = novoPedidoGravado(TipoPagamentoEnum.PIX);

        Pagamento pagamento = pedidos.findById(pedido.getId()).getPagamento();

        assertThat(pagamento).isNotNull();
        assertThat(pagamento.getId()).isNotNull();
        assertThat(pagamento.getTipo()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(pagamento.getValor()).isEqualByComparingTo("221.19");
        assertThat(pagamento.getParcelas()).isEqualTo(1);
    }

    @Test
    void deveSalvarERelerPagamentoDeCartaoConfirmado() {
        // Arrange
        CartaoCliente cartao = cartaoGravado();
        Pedido pedido = pedidos.findById(novoPedidoGravado(TipoPagamentoEnum.CREDIT_CARD).getId());
        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Pagamento pagamento = pedido.getPagamento();
        pagamento.setStatus(StatusPagamentoEnum.CONFIRMADO);
        pagamento.setIdCobranca("pay_persistencia");
        pagamento.setValorLiquido(new BigDecimal("210.50"));
        pagamento.setParcelas(3);
        pagamento.setCartao(cartao);
        pagamento.setDataConfirmacao(agora);

        // Act
        pedidos.update(pedido);
        sincronizar();
        Pagamento relido = pedidos.findById(pedido.getId()).getPagamento();

        // Assert
        assertThat(relido.getId()).isEqualTo(pagamento.getId());
        assertThat(relido.getTipo()).isEqualTo(TipoPagamentoEnum.CREDIT_CARD);
        assertThat(relido.getStatus()).isEqualTo(StatusPagamentoEnum.CONFIRMADO);
        assertThat(relido.getIdCobranca()).isEqualTo("pay_persistencia");
        assertThat(relido.getValor()).isEqualByComparingTo("221.19");
        assertThat(relido.getValorLiquido()).isEqualByComparingTo("210.50");
        assertThat(relido.getParcelas()).isEqualTo(3);
        assertThat(relido.getCartao().getId()).isEqualTo(cartao.getId());
        assertThat(relido.getCartao().getToken()).isEqualTo("tok_persistencia");
        assertThat(relido.getDataCriacao()).isNotNull();
        assertThat(relido.getDataConfirmacao()).isEqualTo(agora);
    }

    @Test
    void deveGravarPagamentoRecusadoComMotivoESemCartaoNemCobranca() {
        // Arrange
        Pedido pedido = pedidos.findById(novoPedidoGravado(TipoPagamentoEnum.CREDIT_CARD).getId());
        pedido.setStatus(StatusEnum.CANCELADO);
        pedido.getPagamento().setStatus(StatusPagamentoEnum.RECUSADO);
        pedido.getPagamento().setMotivoRecusa("Transação não autorizada");

        // Act
        pedidos.update(pedido);
        sincronizar();
        Pedido relido = pedidos.findById(pedido.getId());

        // Assert
        assertThat(relido.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(relido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECUSADO);
        assertThat(relido.getPagamento().getMotivoRecusa()).isEqualTo("Transação não autorizada");
        assertThat(relido.getPagamento().getCartao()).isNull();
        assertThat(relido.getPagamento().getIdCobranca()).isNull();
    }

    @Test
    void deveAtualizarOPagamentoExistenteSemCriarOutroRegistro() {
        // Arrange
        Pedido pedido = pedidos.findById(novoPedidoGravado(TipoPagamentoEnum.PIX).getId());

        // Act
        pedido.getPagamento().setStatus(StatusPagamentoEnum.RECEBIDO);
        pedido.getPagamento().setDataConfirmacao(LocalDateTime.now());
        pedidos.update(pedido);
        sincronizar();

        // Assert
        assertThat(((Number) valor("SELECT COUNT(*) FROM PAGAMENTOS WHERE pedido_id=" + pedido.getId())).intValue()).isEqualTo(1);
        assertThat(pedidos.findById(pedido.getId()).getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
    }

    @Test
    void pedidoSemPagamentoContinuaSendoLido() {
        Pedido pedido = novoPedidoGravado(null);

        Pedido relido = pedidos.findById(pedido.getId());

        assertThat(relido.getPagamento()).isNull();
        assertThat(relido.getTipoPagamento()).isNull();
        assertThat(relido.getProdutosAdicionados()).hasSize(1);
    }

    // ---- Cartões do cliente ----

    @Test
    void deveSalvarERelerCartaoDoCliente() {
        // Act
        CartaoCliente salvo = cartaoGravado();
        CartaoCliente relido = cartoes.findById(salvo.getId());

        // Assert
        assertThat(relido.getCliente().getId()).isEqualTo(9701L);
        assertThat(relido.getToken()).isEqualTo("tok_persistencia");
        assertThat(relido.getUltimosDigitos()).isEqualTo("1234");
        assertThat(relido.getBandeira()).isEqualTo("VISA");
        assertThat(relido.getAtivo()).isTrue();
        assertThat(relido.getDataCadastro()).isNotNull();
    }

    @Test
    void buscarCartaoInexistenteDeveLancarNoSuchElement() {
        assertThatThrownBy(() -> cartoes.findById(999999L)).isInstanceOf(NoSuchElementException.class);
    }

    // ---- Configuração ----

    @Test
    void regraDeParcelamentoUsaOsValoresPadrao() {
        assertThat(regraParcelamento.valorMinimo()).isEqualByComparingTo("100.00");
        assertThat(regraParcelamento.maxParcelas()).isEqualTo(3);
    }
}
