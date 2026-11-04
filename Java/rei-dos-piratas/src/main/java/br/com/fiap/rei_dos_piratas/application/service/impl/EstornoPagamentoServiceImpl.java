package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EstornoPagamentoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CobrancaSemRespostaException;
import br.com.fiap.rei_dos_piratas.domain.gateway.ExecutorTransacional;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento;
import br.com.fiap.rei_dos_piratas.domain.gateway.ResultadoEstorno;
import br.com.fiap.rei_dos_piratas.domain.gateway.SolicitacaoEstorno;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class EstornoPagamentoServiceImpl implements EstornoPagamentoService {

    private final PedidoRepository repository;
    private final GatewayPagamento gatewayPagamento;
    private final ExecutorTransacional executorTransacional;

    public EstornoPagamentoServiceImpl(PedidoRepository repository, GatewayPagamento gatewayPagamento,
                                       ExecutorTransacional executorTransacional) {
        this.repository = repository;
        this.gatewayPagamento = gatewayPagamento;
        this.executorTransacional = executorTransacional;
    }

    @Override
    public Pedido estornar(Long pedidoId, Function<Pedido, Estorno> registrar, String descricao) {
        // T1: registra o motivo do estorno (cancelamento, devolução...) e o estorno SOLICITADO
        Pedido gravado = this.executorTransacional.emTransacao(() -> {
            Pedido pedido = this.repository.findById(pedidoId);
            registrar.apply(pedido);
            return this.repository.update(pedido);
        });
        Pagamento pagamento = gravado.getPagamento();
        // O estorno recém-gravado é o de maior id
        Estorno estorno = pagamento.getEstornos().stream().max(Comparator.comparing(Estorno::getId)).orElseThrow();
        Long estornoId = estorno.getId();

        if (pagamento.getIdCobranca() == null) {
            log.error("[ESTORNO] Pedido ID={} sem cobrança no fornecedor — estorno ID={} precisa de ação manual", pedidoId, estornoId);
            return this.registrarResultado(pedidoId, estornoId, e -> falhar(e, "Pagamento sem cobrança no fornecedor"));
        }

        // Fora de transação: nenhuma conexão do banco presa durante a chamada ao fornecedor
        ResultadoEstorno resultado;
        try {
            resultado = this.gatewayPagamento.estornar(new SolicitacaoEstorno(
                    pagamento.getIdCobranca(), pagamento.getTipo(), estorno.getValor(), descricao));
        } catch (CobrancaSemRespostaException e) {
            // Pode ter sido feito: não repetir; o evento de estorno do fornecedor resolve
            log.warn("[ESTORNO] Sem resposta do fornecedor para o estorno ID={} do pedido ID={} — aguardando evento", estornoId, pedidoId);
            return gravado;
        } catch (ApiExternaException e) {
            log.error("[ESTORNO] Fornecedor recusou o estorno ID={} do pedido ID={}: {} — ação manual necessária",
                    estornoId, pedidoId, e.getMessage());
            return this.registrarResultado(pedidoId, estornoId, e2 -> falhar(e2, e.getMessage()));
        }

        log.info("[ESTORNO] Estorno ID={} do pedido ID={} com status {} no fornecedor", estornoId, pedidoId, resultado.status());
        return this.registrarResultado(pedidoId, estornoId, null, resultado);
    }

    private Pedido registrarResultado(Long pedidoId, Long estornoId, Consumer<Estorno> aplicar) {
        return this.registrarResultado(pedidoId, estornoId, aplicar, null);
    }

    /** T2: relê o pedido; só aplica se o estorno ainda está SOLICITADO (um evento pode ter chegado antes). */
    private Pedido registrarResultado(Long pedidoId, Long estornoId, Consumer<Estorno> aplicar, ResultadoEstorno resultado) {
        return this.executorTransacional.emTransacao(() -> {
            Pedido pedido = this.repository.findById(pedidoId);
            Pagamento pagamento = pedido.getPagamento();
            Estorno estorno = pagamento.getEstornos().stream()
                    .filter(e -> estornoId.equals(e.getId())).findFirst().orElseThrow();
            if (estorno.getStatus() != StatusEstornoEnum.SOLICITADO) {
                log.info("[ESTORNO] Estorno ID={} já atualizado ({}) — resultado da chamada ignorado", estornoId, estorno.getStatus());
                return pedido;
            }
            if (aplicar != null) {
                aplicar.accept(estorno);
            } else if (resultado.status() == StatusEstornoEnum.CONCLUIDO) {
                pagamento.concluirEstorno(estorno);
            } else {
                estorno.setStatus(resultado.status());
                estorno.setUrlSolicitacaoBoleto(resultado.urlSolicitacaoBoleto());
            }
            return this.repository.update(pedido);
        });
    }

    private static void falhar(Estorno estorno, String motivo) {
        estorno.setStatus(StatusEstornoEnum.FALHOU);
        estorno.setMotivoFalha(motivo == null || motivo.length() <= 255 ? motivo : motivo.substring(0, 255));
    }
}
