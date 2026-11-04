package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import lombok.extern.slf4j.Slf4j;

/**
 * Mensagem de erro para as telas: exceções de domínio já trazem texto para o usuário;
 * qualquer outra vira mensagem genérica (o detalhe vai só para o log).
 */
@Slf4j
final class MensagemWeb {

    private static final String PACOTE_DOMINIO = "br.com.fiap.rei_dos_piratas.domain.exceptions";

    static String de(RuntimeException e) {
        if (e.getClass().getPackageName().equals(PACOTE_DOMINIO) && e.getMessage() != null) {
            return e.getMessage();
        }
        log.error("[WEB] Erro inesperado na tela", e);
        return "Ocorreu um erro inesperado. Tente novamente.";
    }

    private MensagemWeb() {}
}
