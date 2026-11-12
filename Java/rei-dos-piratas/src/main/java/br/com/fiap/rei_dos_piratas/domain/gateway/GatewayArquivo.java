package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.net.URL;
import java.time.Duration;

public interface GatewayArquivo {
    URL gerarUrlPreAssinadaUpload(String destino, String key, Duration duration);

    URL gerarUrlPreAssinadaVisualizacao(String key, Duration duration);
}
