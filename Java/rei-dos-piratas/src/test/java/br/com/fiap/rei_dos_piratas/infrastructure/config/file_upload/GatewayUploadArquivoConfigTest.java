package br.com.fiap.rei_dos_piratas.infrastructure.config.file_upload;

import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayUploadArquivoConfigTest {

    @Test
    void deveCriarGatewayComBucketConfigurado() throws Exception {
        S3Template s3Template = mock(S3Template.class);
        Duration duracao = Duration.ofMinutes(10);
        URL urlAssinada = new URL("https://exemplo.com/arquivo");
        URL urlDownload = new URL("https://exemplo.com/arquivo?download=assinado");
        when(s3Template.createSignedPutURL("bucket-teste", "capa.jpg", duracao, null, "image/jpeg"))
                .thenReturn(urlAssinada);
        when(s3Template.createSignedGetURL("bucket-teste", "capa.jpg", duracao))
                .thenReturn(urlDownload);

        new ApplicationContextRunner()
                .withUserConfiguration(GatewayUploadArquivoConfig.class)
                .withBean(S3Template.class, () -> s3Template)
                .withPropertyValues("aws.s3.bucket-name=bucket-teste")
                .run(context -> {
                    GatewayArquivo gateway = context.getBean(GatewayArquivo.class);

                    assertThat(gateway.gerarUrlPreAssinadaUpload("capa.jpg", "image/jpeg", duracao))
                            .isEqualTo(urlAssinada);
                    assertThat(gateway.gerarUrlPreAssinadaVisualizacao("capa.jpg", duracao))
                            .isEqualTo(urlDownload);
                });
    }
}
