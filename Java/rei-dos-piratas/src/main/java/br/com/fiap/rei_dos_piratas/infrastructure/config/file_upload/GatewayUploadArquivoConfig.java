package br.com.fiap.rei_dos_piratas.infrastructure.config.file_upload;

import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.file_upload.GatewayUploadArquivo;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayUploadArquivoConfig {

    @Bean
    public GatewayArquivo gatewayUploadArquivo(
            S3Template s3Template,
            @Value("${aws.s3.bucket-name}") String bucketName) {
        return new GatewayUploadArquivo(s3Template, bucketName);
    }
}
