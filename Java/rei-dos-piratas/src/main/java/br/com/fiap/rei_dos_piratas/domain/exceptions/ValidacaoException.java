package br.com.fiap.rei_dos_piratas.domain.exceptions;

import java.util.Map;

/**
 * Lançada quando uma entidade de domínio falha na validação de seus campos.
 * Carrega o mapa campo → mensagem para que as camadas superiores possam
 * exibir os erros de forma específica (ex: inline no formulário web ou
 * corpo JSON na API).
 */
public class ValidacaoException extends RuntimeException {

    private final Map<String, String> erros;

    public ValidacaoException(Map<String, String> erros) {
        super("Erro de validação: " + erros);
        this.erros = erros;
    }

    public Map<String, String> getErros() {
        return erros;
    }
}

