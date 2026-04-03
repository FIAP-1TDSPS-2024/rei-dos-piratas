package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeracaoEtiquetasResponseDto {

    // Setter para generate_key
    @Setter
    @JsonProperty("generate_key")
    private String generateKey;

    private final Map<String, StatusPedidoEtiqueta> pedidos = new HashMap<>();

    // Construtor padrão
    public GeracaoEtiquetasResponseDto() {}

    // Getters
    public String generateKey() {
        return generateKey;
    }

    public Map<String, StatusPedidoEtiqueta> pedidos() {
        return pedidos;
    }

    // Este método captura todas as propriedades que não foram mapeadas explicitamente
    @JsonAnySetter
    public void setPedido(String key, StatusPedidoEtiqueta value) {
        // Só adiciona se não for o generate_key (que já é tratado separadamente)
        if (!"generate_key".equals(key)) {
            this.pedidos.put(key, value);
        }
    }
}
