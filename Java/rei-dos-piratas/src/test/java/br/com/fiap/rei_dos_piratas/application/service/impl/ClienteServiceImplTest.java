package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PerfilRepository perfilRepository;
    private PasswordEncoder passwordEncoder;
    private Perfil perfilCliente;

    @BeforeEach
    void setUp() {
        this.clienteRepository = mock(ClienteRepository.class);
        this.perfilRepository = mock(PerfilRepository.class);
        this.passwordEncoder = mock(PasswordEncoder.class);
        this.perfilCliente = new Perfil(1L, "CLIENT", "Perfil de cliente", null);

        when(perfilRepository.findByNome("CLIENT")).thenReturn(perfilCliente);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        this.clienteService = new ClienteServiceImpl(clienteRepository, passwordEncoder, perfilRepository);
    }

    @Test
    void listAll() {
        //O que
        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

        Estado estado = new Estado(
                1L,
                "São Paulo",
                "SP");

        Cidade cidade = new Cidade(
                1L,
                "São Paulo",
                estado);

        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                true,
                cidade,
                "Brasil",
                "BR",
                cliente);

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
        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());
        
        //Quando
        when(this.clienteRepository.findById(1L)).thenReturn(cliente);

        //assert
        final Cliente foundCliente = this.clienteService.findById(1L);
        verify(this.clienteRepository,times(1)).findById(any());
        assertThat(foundCliente).isSameAs(cliente);
    }

    @Test
    void create() {
        Cliente clienteParaCriar = new Cliente(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234");

        Cliente clienteCriado = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "encodedPassword",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

        //Quando
        when(this.clienteRepository.create(any(Cliente.class))).thenReturn(clienteCriado);

        //assert
        final Cliente newCliente = this.clienteService.create(clienteParaCriar);
        verify(this.clienteRepository,times(1)).create(any());
        assertThat(newCliente).isSameAs(clienteCriado);
    }

    @Test
    void update() {
        Cliente clienteAntigo = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

        Cliente clienteNovo = new Cliente(
                1L,
                "jonasdasMontanhas",
                "Jonas da Silva Campos Brito",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

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