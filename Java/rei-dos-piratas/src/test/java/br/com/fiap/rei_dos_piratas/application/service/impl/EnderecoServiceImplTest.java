package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnderecoServiceImplTest {

    private static final Long ID_CLIENTE_LOGADO = 10L;

    private EnderecoRepository repository;
    private ClienteService clienteService;
    private CidadeRepository cidadeRepository;
    private EstadoRepository estadoRepository;
    private Validator validator;
    private UsuarioAtualService usuarioAtual;
    private EnderecoServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(EnderecoRepository.class);
        clienteService = mock(ClienteService.class);
        cidadeRepository = mock(CidadeRepository.class);
        estadoRepository = mock(EstadoRepository.class);
        validator = mock(Validator.class);
        when(validator.validate(any())).thenReturn(Set.of());
        usuarioAtual = mock(UsuarioAtualService.class);
        when(usuarioAtual.identidade()).thenReturn(new IdentidadeConta(TipoConta.CLIENTE, ID_CLIENTE_LOGADO));

        service = new EnderecoServiceImpl(repository, clienteService, cidadeRepository, estadoRepository, validator, usuarioAtual);
    }

    private Cliente clienteComId(Long id) {
        Cliente cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(id);
        return cliente;
    }

    private Endereco enderecoDoCliente(Long idEndereco, Long idCliente) {
        Endereco endereco = new Endereco();
        endereco.setId(idEndereco);
        endereco.setCliente(clienteComId(idCliente));
        return endereco;
    }

    @Test
    void findAll_DeveListarSomenteEnderecosDoClienteLogado() {
        Page<Endereco> pagina = new Page<>(1, 0, List.of());
        when(repository.findAllByClienteId(ID_CLIENTE_LOGADO, 0, 10)).thenReturn(pagina);

        Page<Endereco> resultado = service.findAll(0, 10);

        assertThat(resultado).isSameAs(pagina);
        verify(repository).findAllByClienteId(ID_CLIENTE_LOGADO, 0, 10);
    }

    @Test
    void findById_DeveRetornarEnderecoQuandoPertenceAoClienteLogado() {
        Endereco endereco = enderecoDoCliente(1L, ID_CLIENTE_LOGADO);
        when(repository.findById(1L)).thenReturn(endereco);

        assertThat(service.findById(1L)).isSameAs(endereco);
    }

    @Test
    void findById_DeveOcultarEnderecoDeOutroCliente() {
        Endereco enderecoDeOutroCliente = enderecoDoCliente(1L, 99L);
        when(repository.findById(1L)).thenReturn(enderecoDeOutroCliente);

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void save_DeveAssociarEnderecoAoClienteLogado() {
        Cliente clienteLogado = clienteComId(ID_CLIENTE_LOGADO);
        when(clienteService.findById(ID_CLIENTE_LOGADO)).thenReturn(clienteLogado);
        Estado sp = new Estado(1L, "São Paulo", "SP");
        Cidade cidade = new Cidade();
        cidade.setNome("São Paulo");
        cidade.setEstado(sp);
        Endereco novo = new Endereco();
        novo.setCep("01001000");
        novo.setNumero(100);
        novo.setCidade(cidade);
        when(estadoRepository.findFirstByNome("São Paulo")).thenReturn(sp);
        when(cidadeRepository.findFirstByCidadeNomeAndEstadoNome("São Paulo", "São Paulo")).thenReturn(cidade);
        when(repository.save(any(Endereco.class))).thenAnswer(inv -> inv.getArgument(0));

        Endereco salvo = service.save(novo);

        assertThat(salvo.getCliente()).isSameAs(clienteLogado);
    }
}
