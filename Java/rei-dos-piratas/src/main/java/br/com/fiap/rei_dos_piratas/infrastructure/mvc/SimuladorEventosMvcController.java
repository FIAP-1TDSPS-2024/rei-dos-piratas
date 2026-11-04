package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.WebhookRastreioMelhorEnvio;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.TradutorEventoAsaas;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto.EventoCobrancaDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.RastreioWebhookDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simulador de eventos externos para testar o fluxo sem SQS/webhooks (ex.: ambiente local).
 * Só existe com APP_SIMULADOR_EVENTOS=true — nunca ligar em produção.
 */
@Slf4j
@Controller
@RequestMapping("/web/simulador")
@PreAuthorize("hasRole('PEDIDO_WRITE')")
@ConditionalOnProperty(name = "app.simulador-eventos.enabled", havingValue = "true")
public class SimuladorEventosMvcController {

    private final PedidoController pedidos;
    private final DevolucaoController devolucoes;
    private final RastreioCobrancaService rastreioCobranca;
    private final WebhookRastreioMelhorEnvio webhookRastreio;
    private final HmacUtil hmac;
    private final ObjectMapper json;
    private final TradutorEventoAsaas tradutorCobranca = new TradutorEventoAsaas();

    public SimuladorEventosMvcController(PedidoController pedidos, DevolucaoController devolucoes,
                                         RastreioCobrancaService rastreioCobranca, WebhookRastreioMelhorEnvio webhookRastreio,
                                         HmacUtil hmac, ObjectMapper json) {
        this.pedidos = pedidos;
        this.devolucoes = devolucoes;
        this.rastreioCobranca = rastreioCobranca;
        this.webhookRastreio = webhookRastreio;
        this.hmac = hmac;
        this.json = json;
    }

    private static final Set<String> EVENTOS_PAGAMENTO = Set.of("PAYMENT_CONFIRMED", "PAYMENT_RECEIVED", "PAYMENT_OVERDUE",
            "PAYMENT_CREDIT_CARD_CAPTURE_REFUSED", "PAYMENT_REFUNDED", "PAYMENT_REFUND_DENIED");
    private static final Set<String> EVENTOS_RASTREIO = Set.of("order.posted", "order.delivered", "order.canceled");

    /** Evento de cobrança do Asaas para o pedido: tradutor + RastreioCobrancaService (com a idempotência do EventoExterno). */
    @PostMapping("/pedidos/{id}/pagamento")
    public String pagamento(@PathVariable Long id, @RequestParam String evento, RedirectAttributes redirect) {
        String detalhe = "redirect:/web/pedidos/" + id;
        if (!EVENTOS_PAGAMENTO.contains(evento)) {
            redirect.addFlashAttribute("errorMessage", "Evento de pagamento não suportado pelo simulador: " + evento);
            return detalhe;
        }
        try {
            PedidoOutDto pedido = this.pedidos.findById(id);
            EventoCobrancaDto dto = new EventoCobrancaDto("sim_" + UUID.randomUUID(), evento, LocalDateTime.now(),
                    new EventoCobrancaDto.Payment(pedido.idCobranca(), null, id.toString()),
                    evento.equals("PAYMENT_REFUND_DENIED") ? new EventoCobrancaDto.AdditionalInfo("Simulação de estorno negado") : null);
            this.tradutorCobranca.traduzir(dto).ifPresent(eventoPagamento ->
                    this.rastreioCobranca.processarEvento(eventoPagamento, paraJson(dto)));
            log.warn("[SIMULADOR] Evento {} simulado para o pedido ID={}", evento, id);
            redirect.addFlashAttribute("successMessage", "Evento " + evento + " simulado.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
        }
        return detalhe;
    }

    @PostMapping("/pedidos/{id}/rastreio")
    public String rastreioPedido(@PathVariable Long id, @RequestParam String evento, RedirectAttributes redirect) {
        return rastreio("redirect:/web/pedidos/" + id, evento, redirect, () -> this.pedidos.findById(id).pedidoFrete());
    }

    @PostMapping("/devolucoes/{id}/rastreio")
    public String rastreioDevolucao(@PathVariable Long id, @RequestParam String evento, RedirectAttributes redirect) {
        return rastreio("redirect:/web/devolucoes/" + id, evento, redirect, () -> this.devolucoes.findById(id).pedidoFrete());
    }

    /** Evento de rastreio do Melhor Envio: corpo assinado com o HMAC e entregue ao webhook real. */
    private String rastreio(String destino, String evento, RedirectAttributes redirect, Supplier<UUID> envio) {
        if (!EVENTOS_RASTREIO.contains(evento)) {
            redirect.addFlashAttribute("errorMessage", "Evento de rastreio não suportado pelo simulador: " + evento);
            return destino;
        }
        try {
            UUID idEnvio = envio.get();
            if (idEnvio == null) {
                redirect.addFlashAttribute("errorMessage", "Ainda não há envio no Melhor Envio para simular o rastreio.");
                return destino;
            }
            OffsetDateTime agora = OffsetDateTime.now();
            String status = evento.substring("order.".length());
            RastreioWebhookDto webhook = new RastreioWebhookDto(evento, new RastreioDataDto(idEnvio.toString(), "SIMULADO",
                    status, "SIM" + idEnvio.toString().substring(0, 8).toUpperCase(), null, agora, null, null,
                    evento.equals("order.posted") ? agora : null, evento.equals("order.delivered") ? agora : null,
                    evento.equals("order.canceled") ? agora : null, null, null));
            String corpo = paraJson(webhook);
            this.webhookRastreio.receber(this.hmac.generateHmac(corpo), corpo);
            log.warn("[SIMULADOR] Evento de rastreio {} simulado para o envio {}", evento, idEnvio);
            redirect.addFlashAttribute("successMessage", "Evento " + evento + " simulado.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
        }
        return destino;
    }

    private String paraJson(Object valor) {
        try {
            return this.json.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível montar o evento simulado", e);
        }
    }
}
