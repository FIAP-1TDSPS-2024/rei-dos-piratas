package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.math.BigDecimal;
import java.util.UUID;

/** Envio criado no fornecedor de frete. {@code prazoMaximoDias} pode ser nulo no envio reverso. */
public record EnvioCriado(UUID id, String protocolo, BigDecimal preco, Integer prazoMaximoDias) {}
