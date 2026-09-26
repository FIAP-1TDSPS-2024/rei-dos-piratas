package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class RastreioServiceImpl implements RastreioService {

    private final HmacUtil hmacUtil;

    private final ObjectMapper objectMapper;

    private final PedidoService pedidoService;

    private final DevolucaoService devolucaoService;

    public RastreioServiceImpl(HmacUtil hmacUtil, ObjectMapper objectMapper, PedidoService pedidoService, DevolucaoService devolucaoService) {
        this.hmacUtil = hmacUtil;
        this.objectMapper = objectMapper;
        this.pedidoService = pedidoService;
        this.devolucaoService = devolucaoService;
    }

    @Override
    public void rastreioWebhook(String signature, String rawBody) {
        log.debug("Webhook de rastreio recebido — validando assinatura HMAC");

        if (signature == null || !signature.equals(hmacUtil.generateHmac(rawBody))) {
            log.warn("Webhook de rastreio rejeitado — assinatura HMAC inválida");
            return;
        }

        log.debug("Assinatura HMAC válida — processando payload");
        try {
            RastreioWebhookDto rastreio = this.objectMapper.readValue(rawBody, RastreioWebhookDto.class);
            RastreioDataDto data = rastreio.data();

            if (data == null || data.id() == null || data.id().isBlank()) {
                log.warn("Webhook de rastreio ignorado — payload sem identificador de pedido de frete");
                return;
            }

            UUID pedidoFrete = UUID.fromString(data.id());

            log.info("Processando evento webhook: event={}, pedidoFrete UUID={}, status={}",
                    rastreio.event(), pedidoFrete, data.status());

            Optional<Pedido> pedido = this.pedidoService.findByPedidoFrete(pedidoFrete);

            if (pedido.isPresent()) {
                Pedido pedidoEncontrado = pedido.get();
                log.debug("Pedido interno encontrado para rastreio: ID={}, status atual={}",
                        pedidoEncontrado.getId(), pedidoEncontrado.getStatus());
                this.pedidoService.rastreioPedidoWebhook(pedidoEncontrado, rastreio);
                return;
            }

            Optional<Devolucao> devolucao = this.devolucaoService.findByPedidoFrete(pedidoFrete);

            if (devolucao.isPresent()) {
                Devolucao devolucaoEncontrada = devolucao.get();
                log.debug("Devolução interna encontrada para rastreio: ID={}, status atual={}",
                        devolucaoEncontrada.getId(), devolucaoEncontrada.getStatus());
                this.devolucaoService.rastreioDevolucaoWebhook(devolucaoEncontrada, rastreio);
                return;
            }

            log.info("Nenhum pedido ou devolução interno encontrado para o pedidoFrete UUID={} no evento {}",
                    pedidoFrete, rastreio.event());
        } catch (IllegalArgumentException e) {
            log.warn("Webhook de rastreio com UUID inválido recebido: '{}'", rawBody, e);
        } catch (Exception e) {
            log.error("Erro ao processar webhook de rastreio: {}", e.getMessage(), e);
        }
    }
}
