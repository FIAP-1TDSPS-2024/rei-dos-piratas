package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public record GeracaoEtiquetasResponseDto(
        @JsonProperty("generate_key")
        String generateKey,
        Map<String, StatusPedidoEtiqueta> pedidos
) {}
