package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.net.URL;
import java.time.Duration;

/**
 * Porta para o armazenamento de arquivos (hoje o S3). Falhas do fornecedor ou armazenamento
 * não configurado chegam como {@code ArmazenamentoArquivoException}.
 */
public interface GatewayArquivo {

    /** URL para o navegador enviar o arquivo direto ao armazenamento (PUT), presa ao Content-Type informado. */
    URL gerarUrlPreAssinadaUpload(String key, String contentType, Duration validade);

    /** URL temporária para ler o arquivo (GET). */
    URL gerarUrlPreAssinadaVisualizacao(String key, Duration validade);

    /** Se o arquivo da key já foi enviado. */
    boolean existe(String key);
}
