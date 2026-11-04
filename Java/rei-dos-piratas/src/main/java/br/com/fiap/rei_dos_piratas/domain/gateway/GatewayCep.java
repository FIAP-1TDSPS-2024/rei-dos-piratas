package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.util.Optional;

/**
 * Porta para a consulta de CEP (hoje o ViaCEP). Vazio quando o CEP não existe;
 * falha do fornecedor chega como {@code ApiExternaException}.
 */
public interface GatewayCep {

    Optional<EnderecoCep> consultar(String cep);
}
