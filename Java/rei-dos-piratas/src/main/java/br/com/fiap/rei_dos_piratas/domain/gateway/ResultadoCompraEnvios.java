package br.com.fiap.rei_dos_piratas.domain.gateway;

/** Resultado da compra das etiquetas no fornecedor; {@code mensagemErro} nula quando a compra foi aceita. */
public record ResultadoCompraEnvios(String mensagemErro) {

    public boolean sucesso() {
        return mensagemErro == null;
    }
}
