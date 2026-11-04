package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.gateway.EnderecoCep;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayCep;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep.dto.ViaCepRespostaDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/** Adaptador do ViaCEP para a porta {@link GatewayCep}. */
@Slf4j
public class GatewayCepViaCep implements GatewayCep {

    private final ViaCepClient client;

    public GatewayCepViaCep(ViaCepClient client) {
        this.client = client;
    }

    @Override
    public Optional<EnderecoCep> consultar(String cep) {
        if (cep == null || !cep.matches("[0-9]{8}")) {
            return Optional.empty();
        }
        ViaCepRespostaDto resposta;
        try {
            resposta = client.consultar(cep);
        } catch (FeignException e) {
            log.warn("[VIACEP] Falha ao consultar CEP: status={}", e.status());
            throw new ApiExternaException("Não foi possível consultar o CEP agora. Tente novamente em instantes.");
        }
        if (resposta == null || Boolean.TRUE.equals(resposta.erro()) || resposta.localidade() == null) {
            log.info("[VIACEP] CEP não encontrado");
            return Optional.empty();
        }
        return Optional.of(new EnderecoCep(cep, vazioSeNulo(resposta.logradouro()), vazioSeNulo(resposta.bairro()),
                resposta.localidade(), resposta.estado(), resposta.uf()));
    }

    private static String vazioSeNulo(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
