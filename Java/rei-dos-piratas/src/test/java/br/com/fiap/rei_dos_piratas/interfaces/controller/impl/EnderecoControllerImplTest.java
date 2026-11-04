package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.domain.gateway.EnderecoCep;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.CepOutDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnderecoControllerImplTest {

    private final EnderecoService service = mock(EnderecoService.class);
    private final EnderecoControllerImpl controller = new EnderecoControllerImpl(service);

    @Test
    void consultarCepConverteOEnderecoOficialParaODto() {
        when(service.consultarCep("01001000")).thenReturn(new EnderecoCep("01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP"));

        assertThat(controller.consultarCep("01001000"))
                .isEqualTo(new CepOutDto("01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP", false));
    }

    @Test
    void consultarCepIndicaCepGeralParaOClienteInformarRuaEBairro() {
        when(service.consultarCep("13600000")).thenReturn(new EnderecoCep("13600000", "", "", "Araras", "São Paulo", "SP"));

        assertThat(controller.consultarCep("13600000").geral()).isTrue();
    }
}
