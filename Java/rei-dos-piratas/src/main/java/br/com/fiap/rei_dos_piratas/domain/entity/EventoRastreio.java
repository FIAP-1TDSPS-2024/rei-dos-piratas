package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoRastreio;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento de rastreio do envio, já traduzido do fornecedor.
 *
 * @param tipoOriginal  nome do evento no fornecedor, para log
 * @param envio         id do envio no fornecedor (pedido ou devolução)
 * @param statusEnvio   status do envio como o fornecedor informa
 * @param dataEntrega   data da entrega no fuso da loja, quando entregue
 */
public record EventoRastreio(
        TipoEventoRastreio tipo,
        String tipoOriginal,
        UUID envio,
        String statusEnvio,
        String protocolo,
        String codigoRastreio,
        String urlRastreio,
        LocalDate dataEntrega
) {}
