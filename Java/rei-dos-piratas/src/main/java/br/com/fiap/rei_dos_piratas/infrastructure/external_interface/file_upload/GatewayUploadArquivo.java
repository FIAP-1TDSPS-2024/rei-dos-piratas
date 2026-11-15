package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.file_upload;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ArmazenamentoArquivoException;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;

import java.net.URL;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Adaptador do S3 (Spring Cloud AWS) para a porta {@link GatewayArquivo}. Erros do SDK viram
 * {@link ArmazenamentoArquivoException} (502) com mensagem para o usuário; o detalhe técnico fica só no log.
 * Nunca loga a URL assinada (ela dá acesso ao objeto enquanto valer).
 */
@Slf4j
public class GatewayUploadArquivo implements GatewayArquivo {

    private static final String INDISPONIVEL = "Não foi possível acessar o armazenamento de imagens agora. Tente novamente em instantes.";

    private final S3Template s3Template;
    private final String bucketName;

    public GatewayUploadArquivo(S3Template s3Template, String bucketName) {
        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    @Override
    public URL gerarUrlPreAssinadaUpload(String key, String contentType, Duration validade) {
        // Com o Content-Type na assinatura, o S3 recusa um PUT com outro tipo de arquivo
        return executar("assinar upload", () -> s3Template.createSignedPutURL(bucket(), key, validade, null, contentType));
    }

    @Override
    public URL gerarUrlPreAssinadaVisualizacao(String key, Duration validade) {
        return executar("assinar visualização", () -> s3Template.createSignedGetURL(bucket(), key, validade));
    }

    @Override
    public boolean existe(String key) {
        return executar("consultar objeto", () -> s3Template.objectExists(bucket(), key));
    }

    private String bucket() {
        if (bucketName == null || bucketName.isBlank()) {
            log.error("[S3] Bucket de imagens não configurado (S3_BUCKET_NAME_PRODUTOS)");
            throw new ArmazenamentoArquivoException("Armazenamento de imagens não configurado. Avise o suporte.");
        }
        return bucketName;
    }

    private <T> T executar(String operacao, Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (SdkException | S3Exception e) {
            log.error("[S3] Falha ao {}: {}", operacao, e.getClass().getSimpleName());
            throw new ArmazenamentoArquivoException(INDISPONIVEL);
        }
    }
}
