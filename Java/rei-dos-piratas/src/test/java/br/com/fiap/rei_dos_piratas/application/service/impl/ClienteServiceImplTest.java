package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ClienteServiceImplTest {

    private ClienteService clienteService;
    private ClienteRepository clienteRepository;
    private PerfilRepository perfilRepository;
    private SenhaService passwordEncoder;
    private Perfil perfilCliente;
    private Validator validator;

    @BeforeEach
    void setUp() {
        this.clienteRepository = mock(ClienteRepository.class);
        this.perfilRepository = mock(PerfilRepository.class);
        this.passwordEncoder = mock(SenhaService.class);
        this.perfilCliente = new Perfil(1L, "CLIENT", "Perfil de cliente", null);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }

        when(perfilRepository.findByNome("CLIENT")).thenReturn(perfilCliente);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        this.gatewayPagamento = mock(br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento.class);
        this.clienteService = new ClienteServiceImpl(clienteRepository, passwordEncoder, perfilRepository, validator, mock(ContaRepository.class), new br.com.fiap.rei_dos_piratas.infrastructure.security.SpringUsuarioAtual(), gatewayPagamento);
    }

    private br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento gatewayPagamento;

    private Cliente clienteParaCobranca(String idCobranca) {
        Cliente cliente = new Cliente(1L, "jonasdasneves", "Jonas da Silva Campos Melo", "jonas@gmail.com", "SenhaSegura123",
                true, LocalDate.now(), perfilCliente, LocalDate.of(2000, 3, 16), SexoEnum.M, "52998224725", "11991231234", new Carrinho());
        cliente.setIdCobranca(idCobranca);
        return cliente;
    }

    private Endereco enderecoParaCobranca() {
        return new Endereco(1L, 123, "01001000", "Avenida Paulista", "Bela Vista", true,
                new Cidade(1L, "São Paulo", new Estado(1L, "São Paulo", "SP")), "Brasil", "BR", null);
    }

    @Test
    void obterOuCriarClienteCobranca_ClienteJaCadastradoNoFornecedorNaoCadastraDeNovo() {
        Cliente cliente = clienteParaCobranca("cus_existente");

        Cliente resultado = clienteService.obterOuCriarClienteCobranca(cliente, enderecoParaCobranca());

        assertThat(resultado.getIdCobranca()).isEqualTo("cus_existente");
        verifyNoInteractions(gatewayPagamento);
        verify(clienteRepository, never()).update(any());
    }

    @Test
    void obterOuCriarClienteCobranca_ClienteNovoCadastraNoFornecedorEGravaOIdExterno() {
        // Arrange
        mockSecurityContext(1L);
        Cliente cliente = clienteParaCobranca(null);
        when(clienteRepository.findById(1L)).thenReturn(clienteParaCobranca(null));
        when(clienteRepository.update(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gatewayPagamento.registrarCliente(any(), any())).thenReturn("cus_novo");

        // Act
        Cliente resultado = clienteService.obterOuCriarClienteCobranca(cliente, enderecoParaCobranca());

        // Assert
        assertThat(resultado.getIdCobranca()).isEqualTo("cus_novo");
        verify(clienteRepository).update(any(Cliente.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /** Configura um SecurityContext mockado com o ID informado. */
    private void mockSecurityContext(Long userId) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getTipo()).thenReturn(br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta.CLIENTE);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
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

        List<Cliente> clientes = new ArrayList<>();
        clientes.add(cliente);
        Page<Cliente> clientePage = new Page<>(1, 0, clientes);

        when(this.clienteRepository.listAll(0, 10)).thenReturn(clientePage);

        final Page<Cliente> foundClientePage = this.clienteService.listAll(0, 10);
        verify(this.clienteRepository, times(1)).listAll(0, 10);
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

        when(this.clienteRepository.findById(1L)).thenReturn(cliente);

        final Cliente foundCliente = this.clienteService.findById(1L);
        verify(this.clienteRepository, times(1)).findById(any());
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

        when(this.clienteRepository.create(any(Cliente.class))).thenReturn(clienteCriado);

        final Cliente newCliente = this.clienteService.create(clienteParaCriar);
        verify(this.clienteRepository, times(1)).create(any());
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

        mockSecurityContext(1L);
        when(this.clienteRepository.findById(1L)).thenReturn(clienteAntigo);
        when(this.clienteRepository.update(any(Cliente.class))).thenReturn(clienteNovo);

        final Cliente newCliente = this.clienteService.update(clienteNovo);
        verify(this.clienteRepository, times(1)).update(any());
        assertThat(newCliente).isSameAs(clienteNovo).isNotSameAs(clienteAntigo);
    }

    @Test
    void delete() {
        mockSecurityContext(1L);
        doNothing().when(clienteRepository).delete(1L);

        clienteService.delete();

        verify(clienteRepository, times(1)).delete(1L);
    }
}