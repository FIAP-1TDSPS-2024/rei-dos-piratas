package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteControllerImplTest {

    private ClienteService clienteService;
    private ClienteController clienteController;

    @BeforeEach
    void setUp() {
        this.clienteService = mock(ClienteService.class);
        this.clienteController = new ClienteControllerImpl(clienteService);
    }

    private Cliente criarCliente(Long id, String userName) {
        Perfil perfilCliente = new Perfil(1L, "CLIENT", "Perfil de cliente", null);
        return new Cliente(
                id,
                userName,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                "52998224725",
                "11999999999",
                new Carrinho());
    }

    @Test
    void listAll() {
        Page<Cliente> clientePage = new Page<>(1, 0, List.of(criarCliente(1L, "jonasdasneves")));
        when(this.clienteService.listAll(0, 10)).thenReturn(clientePage);

        Page<ClienteOutDto> result = this.clienteController.listAll(0, 10);

        verify(this.clienteService, times(1)).listAll(0, 10);
        assertThat(result.pageItems()).hasSize(1);
        assertThat(result.pageItems().get(0).id()).isEqualTo(1L);
    }

    @Test
    void findById() {
        Cliente cliente = criarCliente(1L, "jonasdasneves");
        when(this.clienteService.findById(1L)).thenReturn(cliente);

        ClienteOutDto result = this.clienteController.findById(1L);

        verify(this.clienteService, times(1)).findById(1L);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userName()).isEqualTo("jonasdasneves");
    }

    @Test
    void update() {
        Cliente clienteNovo = criarCliente(1L, "jonasdasmontanhas");
        when(this.clienteService.update(any(Cliente.class))).thenReturn(clienteNovo);

        ClienteOutDto result = this.clienteController.update(clienteNovo);

        verify(this.clienteService, times(1)).update(any(Cliente.class));
        assertThat(result.userName()).isEqualTo("jonasdasmontanhas");
    }

    @Test
    void delete() {
        doNothing().when(clienteService).delete(1L);

        clienteController.delete(1L);

        verify(clienteService, times(1)).delete(1L);
    }
}