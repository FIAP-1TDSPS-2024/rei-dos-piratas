package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.repository.*;
import br.com.fiap.rei_dos_piratas.infrastructure.security.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Sem @Transactional no teste: cada etapa precisa confirmar seus dados e poder rele-los em outra transacao.
@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.open-in-view=false"})
class PedidoFluxoIntegrationTest {
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager manager;
    @Autowired PedidoService pedidos;
    @Autowired DevolucaoService devolucoes;
    @Autowired CarrinhoService carrinhos;
    @Autowired RastreioService rastreios;
    @Autowired HmacUtil hmac;
    @Autowired ObjectMapper json;
    @Autowired ClienteRepository clientes;
    @Autowired ProdutoRepository produtos;
    @Autowired EnderecoRepository enderecos;
    @Autowired MotivoDevolucaoRepository motivos;
    @Autowired PedidoRepository pedidoRepo;
    @Autowired DevolucaoRepository devolucaoRepo;
    @MockBean FreteService fretes;
    TransactionTemplate tx;
    final UUID ida = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");
    final UUID volta = UUID.fromString("123e4567-e89b-12d3-a456-426614174011");
    final String trackingUrl = "https://example.com/tracking/" + "a".repeat(440);

    <T> T banco(Supplier<T> action) { return tx.execute(status -> action.get()); }
    void sql(String sql) { em.createNativeQuery(sql).executeUpdate(); }
    Object valor(String sql) { return banco(() -> em.createNativeQuery(sql).getSingleResult()); }

    @BeforeEach
    void preparar() {
        tx = new TransactionTemplate(manager);
        banco(() -> {
            sql("INSERT INTO CARRINHOS (id) VALUES (9601)");
            sql("""
                INSERT INTO CLIENTES (id,user_name,nome_completo,email,senha,usuario_ativo,data_cadastro,
                    perfil_id,data_nascimento,sexo,cpf,celular,carrinho_id)
                VALUES (9601,'cliente_fluxo','Cliente Fluxo','fluxo@example.com','test',true,CURRENT_DATE,
                    (SELECT id FROM PERFIS WHERE nome='CLIENT'),'1990-01-01','M','52998224725','11987654321',9601)
                """);
            sql("""
                INSERT INTO ENDERECO (id,numero,cep,logradouro,bairro,endereco_ativo,cidade_id,cliente_id)
                VALUES (9601,100,'01001000','Rua Fluxo','Centro',true,(SELECT MIN(id) FROM CIDADES),9601)
                """);
            sql("""
                INSERT INTO PRODUTOS (id,nome,descricao,autor,categoria,endereco_imagem,preco,preco_original,
                    estoque,altura,largura,profundidade,peso,condicao,funcionario_id)
                VALUES (9601,'Produto Fluxo Completo','Produto para testar o fluxo completo','Autor Teste',
                    'AVENTURA','https://example.com/manga.jpg',29.90,39.90,100,20,14,2,0.30,'NOVO',1)
                """);
            return null;
        });
        clienteLogado();
        when(fretes.calcularFreteProdutos(anyString(), anyList())).thenReturn(List.of(
                new FreteServiceDto(1L,"Entrega",new BigDecimal("10.25"),null,null,"BRL",3,null)));
        when(fretes.criarPedidoFrete(any())).thenReturn(new PedidoFreteResponseDto(
                ida,"PROTOCOLO-IDA",1,null,null,new BigDecimal("10.25"),null,2,3,"pending",false,
                LocalDateTime.now(),LocalDateTime.now()));
        when(fretes.organizarFretes(anyList())).thenReturn(new CompraFreteResponseDto(null,null,null,null,null,null,null));
        var etiquetas = new GeracaoEtiquetasResponseDto();
        etiquetas.setPedido(ida.toString(),new StatusPedidoEtiqueta("OK",true));
        when(fretes.gerarEtiquetasPedidoFrete(anyList())).thenReturn(etiquetas);
        when(fretes.imprimirEtiquetasPedidoFrete(anyList())).thenReturn(new ImpressaoEtiquetasResponseDto("https://example.com/etiquetas"));
        when(fretes.criarPedidoDevolucaoFrete(any())).thenReturn(new DevolucaoFreteResponseDto(
                volta,"PROTOCOLO-VOLTA",1,null,new BigDecimal("12.35"),null,2,3,"pending",
                LocalDateTime.now(),LocalDateTime.now(),1));
    }

    @AfterEach
    void limpar() {
        banco(() -> {
            sql("DELETE FROM DEVOLUCAO_ITENS WHERE devolucao_id IN (SELECT id FROM DEVOLUCOES WHERE pedido_id IN (SELECT id FROM PEDIDOS WHERE cliente_id=9601))");
            sql("DELETE FROM DEVOLUCOES WHERE pedido_id IN (SELECT id FROM PEDIDOS WHERE cliente_id=9601)");
            sql("DELETE FROM PEDIDO_PRODUTO WHERE pedido_id IN (SELECT id FROM PEDIDOS WHERE cliente_id=9601)");
            sql("DELETE FROM PEDIDOS WHERE cliente_id=9601");
            sql("DELETE FROM CARRINHO_PRODUTO WHERE carrinho_id=9601");
            sql("DELETE FROM ENDERECO WHERE id=9601");
            sql("DELETE FROM CLIENTES WHERE id=9601");
            sql("DELETE FROM CARRINHOS WHERE id=9601");
            sql("DELETE FROM PRODUTOS WHERE id=9601");
            return null;
        });
        SecurityContextHolder.clearContext();
    }

    void clienteLogado() { autenticar(9601L,TipoConta.CLIENTE,List.of("ROLE_PEDIDO_READ")); }
    void funcionarioLogado() { autenticar(1L,TipoConta.FUNCIONARIO,List.of("ROLE_PEDIDO_WRITE")); }
    void autenticar(Long id, TipoConta tipo, List<String> roles) {
        var user = new CustomUserDetails(id,"test","test",roles.stream().map(SimpleGrantedAuthority::new).toList(),tipo,true);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()));
    }
    Pedido novoPedido() {
        return banco(() -> new Pedido(clientes.findById(9601L),enderecos.findById(9601L),
                List.of(new ItemProdutoPedido(produtos.findById(9601L),3)),1L));
    }
    Pedido criar() { return pedidos.fazerPedido(novoPedido()); }
    Pedido entregue() throws Exception {
        Pedido pedido = criar();
        pedidos.pagarPedido(pedido.getId());
        evento(ida,"order.delivered");
        return pedidos.findById(pedido.getId());
    }
    void evento(UUID uuid,String nome) throws Exception {
        var data = new RastreioDataDto(uuid.toString(),"PROTOCOLO",nome,"TRACK123",null,
                OffsetDateTime.now(),OffsetDateTime.now(),OffsetDateTime.now(),OffsetDateTime.now(),
                OffsetDateTime.now(),null,null,trackingUrl);
        String body = json.writeValueAsString(new RastreioWebhookDto(nome,data));
        rastreios.rastreioWebhook(hmac.generateHmac(body),body);
    }
    Devolucao solicitar(Pedido pedido, String codigo) {
        MotivoDevolucao motivo = banco(() -> motivos.findById(((Number)em.createNativeQuery(
                "SELECT id FROM MOTIVOS_DEVOLUCAO WHERE codigo=:codigo").setParameter("codigo",codigo).getSingleResult()).longValue()));
        var devolucao = new Devolucao(pedido,motivo,"Produto para devolucao",
                List.of(new ItemDevolucao(null,pedido.getProdutosAdicionados().getFirst(),1)));
        devolucao.setServicoEntrega(1L);
        return devolucoes.solicitarDevolucao(devolucao);
    }

    @Test
    void fluxoCompletoComItensFreteEtiquetasRastreioEDevolucao() throws Exception {
        Pedido criado = criar();
        assertThat(pedidos.findById(criado.getId()).getValorTotal()).isEqualByComparingTo("99.95");
        assertThat(pedidos.findById(criado.getId()).getProdutosAdicionados()).hasSize(1);
        assertThat(((Number)valor("SELECT estoque FROM PRODUTOS WHERE id=9601")).intValue()).isEqualTo(97);
        Long itemId = pedidos.findById(criado.getId()).getProdutosAdicionados().getFirst().getId();
        pedidos.pagarPedido(criado.getId());
        assertThat(pedidos.findByPedidoFrete(ida)).isPresent();
        funcionarioLogado();
        assertThat(pedidos.findAll(0,10).pageItems()).extracting(Pedido::getId).contains(criado.getId());
        assertThat(pedidos.organizarPedidosParaEnvio(List.of(criado.getId()))).isNull();
        assertThat(pedidos.findById(criado.getId()).getStatus()).isEqualTo(StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        assertThat(pedidos.gerarEtiquetasParaEnvio(List.of(criado.getId()))).containsKey(criado.getId());
        assertThat(pedidos.findById(criado.getId()).getStatus()).isEqualTo(StatusEnum.AGUARDANDO_POSTAGEM);
        assertThat(pedidos.imprimirEtiquetasEnvio(List.of(criado.getId()))).isEqualTo("https://example.com/etiquetas");
        evento(ida,"order.posted");
        assertThat(pedidos.findById(criado.getId()).getStatus()).isEqualTo(StatusEnum.EM_TRANSITO);
        evento(ida,"order.delivered");
        Pedido entregue = pedidos.findById(criado.getId());
        assertThat(entregue.getStatus()).isEqualTo(StatusEnum.ENTREGUE);
        assertThat(entregue.getDataEntrega()).isEqualTo(LocalDate.now());
        assertThat(entregue.getTrackingUrl()).isEqualTo(trackingUrl);
        assertThat(entregue.getProdutosAdicionados().getFirst().getId()).isEqualTo(itemId);
        assertThat(entregue.getProdutosAdicionados().getFirst().getPrecoUnitario()).isEqualByComparingTo("29.90");
        Devolucao devolucao = solicitar(entregue,"PRODUTO_DEFEITUOSO");
        assertThat(devolucoes.findById(devolucao.getId()).getStatus()).isEqualTo(StatusDevolucaoEnum.EM_ANALISE);
        devolucoes.aprovarDevolucao(devolucao.getId());
        assertThat(devolucoes.findByPedidoFrete(volta)).isPresent();
        assertThat(devolucoes.organizarDevolucoesParaEnvio(List.of(devolucao.getId()))).isNull();
        evento(volta,"order.posted");
        assertThat(devolucoes.findById(devolucao.getId()).getStatus()).isEqualTo(StatusDevolucaoEnum.EM_RETORNO);
        evento(volta,"order.delivered");
        var finalizada = devolucoes.findById(devolucao.getId());
        assertThat(finalizada.getStatus()).isEqualTo(StatusDevolucaoEnum.RETORNADO);
        assertThat(finalizada.getDataConclusao()).isEqualTo(LocalDate.now());
        assertThat(finalizada.getTrackingUrl()).isEqualTo(trackingUrl);
        assertThat(finalizada.getItens().getFirst().getPrecoUnitario()).isEqualByComparingTo("29.90");
        assertThat(finalizada.getValorTotal()).isEqualByComparingTo("42.25");
        assertThat(devolucoes.findAllByPedido(0,10,criado.getId()).pageItems()).hasSize(1);
        assertThat(devolucoes.findAllByStatus(0,10,StatusDevolucaoEnum.RETORNADO).pageItems()).hasSize(1);
    }

    @Test
    void migracaoDeDevolucaoLegadaPreservaMotivoECorrigeStatus() {
        var pedido = criar();
        banco(() -> {
            em.createNativeQuery("""
                INSERT INTO DEVOLUCOES (pedido_id,motivo,motivo_devolucao_id,data_solicitacao,status)
                VALUES (:pedido,'PRODUTO_DEFEITUOSO',
                    (SELECT id FROM MOTIVOS_DEVOLUCAO WHERE codigo='PRODUTO_DEFEITUOSO'),
                    CURRENT_DATE,'PREPARANDO_ENVIO')
                """).setParameter("pedido",pedido.getId()).executeUpdate();
            em.unwrap(org.hibernate.Session.class).doWork(connection ->
                org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection,
                    new org.springframework.core.io.ClassPathResource("db.migracao/V34__compatibilidade_fluxo_devolucoes.SQL")));
            return null;
        });
        var legada = devolucoes.findAllByPedidoId(pedido.getId()).getFirst();
        assertThat(legada.getStatus()).isEqualTo(StatusDevolucaoEnum.PREPARANDO_RETORNO);
        assertThat(legada.getMotivo().getCodigo()).isEqualTo("PRODUTO_DEFEITUOSO");
        assertThat(valor("SELECT motivo FROM DEVOLUCOES WHERE pedido_id="+pedido.getId())).isEqualTo("PRODUTO_DEFEITUOSO");
    }

    @Test
    void cancelamentoAntesEDepoisDoPagamentoRestauraEstoque() {
        Pedido primeiro = criar();
        pedidos.cancelarPedido(primeiro.getId());
        assertThat(pedidos.findById(primeiro.getId()).getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(((Number)valor("SELECT estoque FROM PRODUTOS WHERE id=9601")).intValue()).isEqualTo(100);
        Pedido segundo = criar();
        pedidos.pagarPedido(segundo.getId());
        pedidos.cancelarPedido(segundo.getId());
        assertThat(pedidos.findById(segundo.getId()).getPedidoFrete()).isEqualTo(ida);
        assertThat(((Number)valor("SELECT estoque FROM PRODUTOS WHERE id=9601")).intValue()).isEqualTo(100);
    }

    @Test
    void falhaDeFreteMantemPedidoAguardandoPagamento() {
        var pedido = criar();
        when(fretes.criarPedidoFrete(any())).thenThrow(new IllegalStateException("Frete indisponivel"));
        assertThatThrownBy(() -> pedidos.pagarPedido(pedido.getId())).isInstanceOf(IllegalStateException.class);
        var salvo = pedidos.findById(pedido.getId());
        assertThat(salvo.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
        assertThat(salvo.getPedidoFrete()).isNull();
        assertThat(((Number)valor("SELECT estoque FROM PRODUTOS WHERE id=9601")).intValue()).isEqualTo(97);
    }

    @Test
    void loteAtualizaSomentePedidosComStatusElegivel() {
        var pago = criar();
        pedidos.pagarPedido(pago.getId());
        var pendente = criar();
        assertThat(pedidos.organizarPedidosParaEnvio(List.of(pago.getId(),pendente.getId()))).isNull();
        assertThat(pedidos.findById(pago.getId()).getStatus()).isEqualTo(StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        assertThat(pedidos.findById(pendente.getId()).getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void falhaDePersistenciaDesfazBaixaDeEstoque() {
        Pedido invalido = novoPedido();
        invalido.getEnderecoEntrega().setId(999999L);
        assertThatThrownBy(() -> pedidos.fazerPedido(invalido)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(((Number)valor("SELECT estoque FROM PRODUTOS WHERE id=9601")).intValue()).isEqualTo(100);
        assertThat(((Number)valor("SELECT COUNT(*) FROM PEDIDOS WHERE cliente_id=9601")).intValue()).isZero();
    }

    @Test
    void carrinhoFinalizaCompraPreservaItensELimpaCarrinho() {
        Produto produto = banco(() -> produtos.findById(9601L));
        carrinhos.adicionarProduto(new ItemProdutoPedido(produto,3));
        assertThat(carrinhos.visualizarCarrinho().getProdutosAdicionados()).hasSize(1);
        Endereco endereco = banco(() -> enderecos.findById(9601L));
        Pedido pedido = carrinhos.finalizarCompra(endereco,1L);
        assertThat(pedidos.findById(pedido.getId()).getProdutosAdicionados()).hasSize(1);
        assertThat(((Number)valor("SELECT COUNT(*) FROM CARRINHO_PRODUTO WHERE carrinho_id=9601")).intValue()).isZero();
    }

    @Test
    void devolucaoPorArrependimentoCriaEAprovaNaMesmaTransacao() throws Exception {
        var devolucao = solicitar(entregue(),"ARREPENDIMENTO");
        var salva = devolucoes.findById(devolucao.getId());
        assertThat(salva.getStatus()).isEqualTo(StatusDevolucaoEnum.PREPARANDO_RETORNO);
        assertThat(salva.getPedidoFrete()).isEqualTo(volta);
        assertThat(salva.getAprovada()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(StatusEnum.class)
    void todosStatusDePedidoPersistemEFiltram(StatusEnum status) {
        var pedido = criar();
        pedido.setStatus(status);
        pedidoRepo.update(pedido);
        assertThat(pedidos.findById(pedido.getId()).getStatus()).isEqualTo(status);
        assertThat(pedidos.findAllByStatus(0,10,status).pageItems()).extracting(Pedido::getId).contains(pedido.getId());
        assertThat(pedidoRepo.findByIdsAndStatus(List.of(pedido.getId()),status)).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(StatusDevolucaoEnum.class)
    void todosStatusDeDevolucaoPersistemEFiltram(StatusDevolucaoEnum status) throws Exception {
        var devolucao = solicitar(entregue(),"PRODUTO_DEFEITUOSO");
        devolucao.setStatus(status);
        devolucaoRepo.update(devolucao);
        assertThat(devolucoes.findById(devolucao.getId()).getStatus()).isEqualTo(status);
        assertThat(devolucoes.findAllByStatus(0,10,status).pageItems()).extracting(Devolucao::getId).contains(devolucao.getId());
        assertThat(devolucaoRepo.findByIdsAndStatus(List.of(devolucao.getId()),status)).hasSize(1);
    }
}
