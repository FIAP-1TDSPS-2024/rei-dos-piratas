package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.MotivoDevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.MotivoDevolucaoOutDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevolucaoControllerImplMotivosTest {

    @Test
    void listarMotivosDeveDevolverOsMotivosCadastrados() {
        // Arrange
        MotivoDevolucaoService motivos = mock(MotivoDevolucaoService.class);
        MotivoDevolucao arrependimento = new MotivoDevolucao();
        arrependimento.setId(1L);
        arrependimento.setCodigo("ARREPENDIMENTO");
        arrependimento.setDescricao("Desisti da compra");
        arrependimento.setArrependimento(true);
        when(motivos.findAll()).thenReturn(List.of(arrependimento));
        DevolucaoControllerImpl controller = new DevolucaoControllerImpl(mock(DevolucaoService.class), motivos, mock(PedidoService.class));

        // Act & Assert
        assertThat(controller.listarMotivos())
                .containsExactly(new MotivoDevolucaoOutDto(1L, "ARREPENDIMENTO", "Desisti da compra", true));
    }
}
