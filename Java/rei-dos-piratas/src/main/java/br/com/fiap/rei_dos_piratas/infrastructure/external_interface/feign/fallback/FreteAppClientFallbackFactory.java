package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.fallback;

import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.FreteAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.ImpressaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fallback factory para o FreteAppClient (Melhor Envio).
 * Ativado pelo Circuit Breaker quando o circuito está OPEN ou a chamada falha.
 * Garante respostas degradadas mas seguras enquanto a instância de scaling sobe (~120s).
 */
@Slf4j
@Component
public class FreteAppClientFallbackFactory implements FallbackFactory<FreteAppClient> {

    @Override
    public FreteAppClient create(Throwable cause) {
        log.warn("[CIRCUIT BREAKER] FreteAppClient degradado. Causa: {}", cause.getMessage());

        return new FreteAppClient() {

            @Override
            public List<FreteServiceDto> calcularFrete(ConsultaFreteServiceDto request) {
                log.warn("[FALLBACK] calcularFrete indisponível — retornando lista vazia.");
                return Collections.emptyList();
            }

            @Override
            public PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto request) {
                log.error("[FALLBACK] criarPedidoFrete indisponível. Causa: {}", cause.getMessage());
                throw new ServiceUnavailableException(
                        "Serviço de frete temporariamente indisponível. Tente novamente em instantes.");
            }

            @Override
            public CompraFreteResponseDto pagarPedidoFrete(List<String> orders) {
                log.error("[FALLBACK] pagarPedidoFrete indisponível. Causa: {}", cause.getMessage());
                throw new ServiceUnavailableException(
                        "Serviço de pagamento de frete temporariamente indisponível. Tente novamente em instantes.");
            }

            @Override
            public GeracaoEtiquetasResponseDto gerarEtiquetas(Map<String, List<String>> request) {
                log.error("[FALLBACK] gerarEtiquetas indisponível. Causa: {}", cause.getMessage());
                throw new ServiceUnavailableException(
                        "Serviço de geração de etiquetas temporariamente indisponível. Tente novamente em instantes.");
            }

            @Override
            public ImpressaoEtiquetasResponseDto gerarLinkImpressaoEtiquetas(Map<String, List<String>> request) {
                log.error("[FALLBACK] gerarLinkImpressaoEtiquetas indisponível. Causa: {}", cause.getMessage());
                throw new ServiceUnavailableException(
                        "Serviço de impressão de etiquetas temporariamente indisponível. Tente novamente em instantes.");
            }
        };
    }
}

