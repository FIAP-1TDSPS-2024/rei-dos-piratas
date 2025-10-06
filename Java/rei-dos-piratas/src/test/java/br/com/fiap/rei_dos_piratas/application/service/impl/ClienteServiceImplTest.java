package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ClienteServiceImplTest {

    private ClienteService clienteService;
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        this.clienteRepository = mock(ClienteRepository.class);
        this.clienteService = new ClienteServiceImpl(clienteRepository);
    }

    @Test
    void listAll() {
        //O que
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                endereco,
                "12345678978");

        List<Cliente> clientes = new ArrayList<Cliente>();
        clientes.add(cliente);

        Page<Cliente> clientePage = new Page<Cliente>(1, 0, clientes);

        when(this.clienteRepository.listAll(0,10)).thenReturn(clientePage);

        final Page<Cliente> foundClientePage = this.clienteService.listAll(0,10);
        verify(this.clienteRepository, times(1)).listAll(0,10);
        assertThat(foundClientePage).isSameAs(clientePage);

    }

    @Test
    void findById() {
        //O que
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                endereco,
                "12345678978");
        
        //Quando
        when(this.clienteRepository.findById(1L)).thenReturn(cliente);

        //assert
        final Cliente foundCliente = this.clienteService.findById(1L);
        verify(this.clienteRepository,times(1)).findById(any());
        assertThat(foundCliente).isSameAs(cliente);
    }

    @Test
    void create() {
        //O que
        Endereco enderecoParaCriar = new Endereco(
                null,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente clienteParaCriar = new Cliente(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoParaCriar,
                "12345678978");

        Endereco enderecoCriado = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente clienteCriado = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoCriado,
                "12345678978");

        //Quando
        when(this.clienteRepository.create(clienteParaCriar)).thenReturn(clienteCriado);

        //assert
        final Cliente newCliente = this.clienteService.create(clienteParaCriar);
        verify(this.clienteRepository,times(1)).create(any());
        assertThat(newCliente).isSameAs(clienteCriado);
    }

    @Test
    void update() {

        //O que
        Endereco enderecoAntigo = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente clienteAntigo = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoAntigo,
                "12345678978");

        Endereco enderecoNovo = new Endereco(
                1L,
                123,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente clienteNovo = new Cliente(
                1L,
                "jonasdasMontanhas",
                "Jonas da Silva Campos Brito",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                enderecoNovo,
                "12345678978");

        //Quando
        when(this.clienteRepository.findById(1L)).thenReturn(clienteAntigo);
        when(this.clienteRepository.update(clienteNovo)).thenReturn(clienteNovo);

        //assert
        final Cliente newCliente = this.clienteService.update(clienteNovo);
        verify(this.clienteRepository,times(1)).update(any());
        assertThat(newCliente).isSameAs(clienteNovo).isNotSameAs(clienteAntigo);
    }

    @Test
    void delete() {
        // Quando
        doNothing().when(clienteRepository).delete(1L);

        // Executa
        clienteService.delete(1L);

        // Verifica
        verify(clienteRepository, times(1)).delete(1L);
    }
}