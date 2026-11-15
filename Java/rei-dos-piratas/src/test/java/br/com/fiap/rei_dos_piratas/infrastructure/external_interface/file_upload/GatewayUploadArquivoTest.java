package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.file_upload;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ArmazenamentoArquivoException;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Adaptador do S3: assina com o Content-Type e traduz falhas do SDK em exceção de domínio. */
class GatewayUploadArquivoTest {

    private static final String KEY = "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png";
    private static final Duration CINCO_MIN = Duration.ofMinutes(5);

    private final S3Template s3 = mock(S3Template.class);
    private final GatewayUploadArquivo gateway = new GatewayUploadArquivo(s3, "bucket-produtos");

    @Test
    void urlDeUploadEhAssinadaComOContentTypeDoArquivo() throws Exception {
        URL assinada = new URL("https://bucket-produtos.s3.amazonaws.com/" + KEY + "?X-Amz-Signature=abc");
        when(s3.createSignedPutURL("bucket-produtos", KEY, CINCO_MIN, null, "image/png")).thenReturn(assinada);

        assertThat(gateway.gerarUrlPreAssinadaUpload(KEY, "image/png", CINCO_MIN)).isEqualTo(assinada);
    }

    @Test
    void urlDeVisualizacaoUsaOBucketConfigurado() throws Exception {
        URL assinada = new URL("https://bucket-produtos.s3.amazonaws.com/" + KEY + "?X-Amz-Signature=def");
        when(s3.createSignedGetURL("bucket-produtos", KEY, CINCO_MIN)).thenReturn(assinada);

        assertThat(gateway.gerarUrlPreAssinadaVisualizacao(KEY, CINCO_MIN)).isEqualTo(assinada);
    }

    @Test
    void falhaDoSdkAoAssinarUploadViraExcecaoDeDominio() {
        when(s3.createSignedPutURL(any(), any(), any(), any(), any()))
                .thenThrow(SdkClientException.create("Unable to load credentials"));

        assertThatThrownBy(() -> gateway.gerarUrlPreAssinadaUpload(KEY, "image/png", CINCO_MIN))
                .isInstanceOf(ArmazenamentoArquivoException.class)
                .hasMessageNotContaining("credentials");
    }

    @Test
    void falhaDoSdkAoAssinarVisualizacaoViraExcecaoDeDominio() {
        when(s3.createSignedGetURL(any(), any(), any())).thenThrow(new S3Exception("erro", new RuntimeException()));

        assertThatThrownBy(() -> gateway.gerarUrlPreAssinadaVisualizacao(KEY, CINCO_MIN))
                .isInstanceOf(ArmazenamentoArquivoException.class);
    }

    @Test
    void existeConsultaOObjetoNoBucket() {
        when(s3.objectExists("bucket-produtos", KEY)).thenReturn(true);

        assertThat(gateway.existe(KEY)).isTrue();
    }

    @Test
    void falhaAoConsultarExistenciaViraExcecaoDeDominio() {
        when(s3.objectExists(any(), any())).thenThrow(SdkClientException.create("timeout"));

        assertThatThrownBy(() -> gateway.existe(KEY)).isInstanceOf(ArmazenamentoArquivoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    void bucketNaoConfiguradoFalhaSemChamarOS3(String bucket) {
        GatewayUploadArquivo semBucket = new GatewayUploadArquivo(s3, bucket);

        assertThatThrownBy(() -> semBucket.gerarUrlPreAssinadaUpload(KEY, "image/png", CINCO_MIN))
                .isInstanceOf(ArmazenamentoArquivoException.class)
                .hasMessageContaining("não configurado");
        assertThatThrownBy(() -> semBucket.gerarUrlPreAssinadaVisualizacao(KEY, CINCO_MIN))
                .isInstanceOf(ArmazenamentoArquivoException.class);
        assertThatThrownBy(() -> semBucket.existe(KEY)).isInstanceOf(ArmazenamentoArquivoException.class);
        verifyNoInteractions(s3);
    }
}
