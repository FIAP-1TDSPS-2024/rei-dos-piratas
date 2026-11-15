package br.com.fiap.rei_dos_piratas.domain.exceptions;

/** Falha no armazenamento de arquivos (S3 fora, credencial/bucket inválido ou não configurado). Responde 502. */
public class ArmazenamentoArquivoException extends ApiExternaException {
    public ArmazenamentoArquivoException(String message) {
        super(message);
    }
}
