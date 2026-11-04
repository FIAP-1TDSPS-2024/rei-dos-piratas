package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

/** Credenciais OAuth do aplicativo no Melhor Envio (ME_CLIENT_ID, ME_SECRET, ME_REDIRECT_URI). */
public record CredenciaisMelhorEnvio(String clientId, String clientSecret, String redirectUri) {

    @Override
    public String toString() {
        return "CredenciaisMelhorEnvio[***]";
    }
}
