package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.file_upload;

import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo;
import io.awspring.cloud.s3.S3Template;

import java.net.URL;
import java.time.Duration;

public class GatewayUploadArquivo implements GatewayArquivo {

    private final S3Template s3Template;
    private final String bucketName;

    public GatewayUploadArquivo(S3Template s3Template, String bucketName) {
        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    @Override
    public URL gerarUrlPreAssinadaUpload(String destino, String key, Duration duration) {
        return s3Template.createSignedPutURL(bucketName, key, duration);
    }

    @Override
    public URL gerarUrlPreAssinadaVisualizacao(String key, Duration duration) {
        return s3Template.createSignedGetURL(bucketName, key, duration);
    }
}
