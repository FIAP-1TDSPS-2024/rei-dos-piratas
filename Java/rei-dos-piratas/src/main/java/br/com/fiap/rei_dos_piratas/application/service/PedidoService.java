package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosBoleto;
import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.PagamentoCartao;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.gateway.QrCodePix;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PedidoService {
    Page<Pedido> findAll(int pageNumber, int pageSize);
    Page<Pedido> findAllByStatus(int pageNumber, int pageSize, StatusEnum status);
    Pedido findById(Long id);
    Pedido fazerPedido(Pedido pedido);
    /** {@code pagamentoCartao} é nulo para PIX/boleto e obrigatório para cartão. */
    Pedido fazerPedido(Pedido pedido, PagamentoCartao pagamentoCartao);
    void pagarPedido(Long id);
    String organizarPedidosParaEnvio(List<Long> pedidos);
    Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos);
    String imprimirEtiquetasEnvio(List<Long> pedidos);
    void rastreioPedidoWebhook(Pedido pedido, EventoRastreio evento);
    Optional<Pedido> findByPedidoFrete(UUID pedidoFrete);
    Pedido cancelarPedido(Long id);
    QrCodePix obterQrCodeCobrancaPix(Long id);
    /** Linha digitável, código de barras, URL e vencimento do boleto do pedido. */
    DadosBoleto obterBoleto(Long id);

    /** Estornos do pagamento do pedido (só o dono do pedido ou funcionário). */
    List<Estorno> listarEstornos(Long id);
    void processarEventoCobranca(Long pedidoId, EventoPagamento evento);
}
