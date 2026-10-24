package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CartaoClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoClienteOutDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartaoClienteControllerImplTest {

    private CartaoService service;
    private CartaoClienteController controller;

    @BeforeEach
    void setUp() {
        this.service = mock(CartaoService.class);
        this.controller = new CartaoClienteControllerImpl(service);
    }

    @Test
    void listarCartoesDoClienteAtual_DeveConverterOsCartoesParaDto() {
        // Arrange
        LocalDateTime cadastro = LocalDateTime.of(2026, 10, 1, 10, 30);
        when(service.listarCartoesDoClienteAtual()).thenReturn(List.of(
                new CartaoCliente(5L, null, "tok_abc123", "1234", "VISA", true, cadastro)));

        // Act
        List<CartaoClienteOutDto> resultado = controller.listarCartoesDoClienteAtual();

        // Assert
        assertThat(resultado).containsExactly(new CartaoClienteOutDto(5L, "VISA", "1234", cadastro));
    }

    @Test
    void listarCartoesDoClienteAtual_SemCartoesDeveRetornarListaVazia() {
        when(service.listarCartoesDoClienteAtual()).thenReturn(List.of());

        assertThat(controller.listarCartoesDoClienteAtual()).isEmpty();
    }
}
