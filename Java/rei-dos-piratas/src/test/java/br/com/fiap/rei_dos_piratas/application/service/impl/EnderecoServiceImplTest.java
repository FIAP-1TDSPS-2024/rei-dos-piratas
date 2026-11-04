package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.gateway.EnderecoCep;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayCep;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnderecoServiceImplTest {

    private static final Long ID_CLIENTE_LOGADO = 10L;
    private static final EnderecoCep PRACA_DA_SE = new EnderecoCep("01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP");

    private EnderecoRepository repository;
    private ClienteService clienteService;
    private CidadeRepository cidadeRepository;
    private EstadoRepository estadoRepository;
    private Validator validator;
    private UsuarioAtualService usuarioAtual;
    private GatewayCep gatewayCep;
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

        gatewayCep = mock(GatewayCep.class);
        when(gatewayCep.consultar("01001000")).thenReturn(Optional.of(PRACA_DA_SE));

        service = new EnderecoServiceImpl(repository, clienteService, cidadeRepository, estadoRepository, validator, usuarioAtual, gatewayCep);
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

    private Endereco digitado(String cep) {
        Cliente cliente = clienteComId(ID_CLIENTE_LOGADO);
        when(clienteService.findById(ID_CLIENTE_LOGADO)).thenReturn(cliente);
        when(repository.save(any(Endereco.class))).thenAnswer(inv -> inv.getArgument(0));
        when(estadoRepository.save(any(Estado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cidadeRepository.save(any(Cidade.class))).thenAnswer(inv -> inv.getArgument(0));
        Endereco endereco = new Endereco();
        endereco.setCep(cep);
        endereco.setNumero(100);
        endereco.setLogradouro("Rua Inventada");
        endereco.setBairro("Bairro Inventado");
        endereco.setCidade(new Cidade(null, "Cidade Inventada", new Estado(null, "Estado Inventado", "XX")));
        return endereco;
    }

    @Test
    void save_DeveGravarComOsDadosOficiaisDoCep() {
        Endereco salvo = service.save(digitado("01001000"));

        assertThat(salvo.getLogradouro()).isEqualTo("Praça da Sé");
        assertThat(salvo.getBairro()).isEqualTo("Sé");
        assertThat(salvo.getCidade().getNome()).isEqualTo("São Paulo");
        assertThat(salvo.getCidade().getEstado().getSigla()).isEqualTo("SP");
        assertThat(salvo.getNumero()).isEqualTo(100);
    }

    @Test
    void save_DeveRecusarCepInexistenteSemGravar() {
        when(gatewayCep.consultar("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(digitado("99999999")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CEP não encontrado");
        verify(repository, never()).save(any());
    }

    @Test
    void consultarCep_DeveRetornarOEnderecoOficial() {
        assertThat(service.consultarCep("01001000")).isEqualTo(PRACA_DA_SE);
    }

    @Test
    void consultarCep_DeveRecusarCepInexistente() {
        when(gatewayCep.consultar("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarCep("99999999"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CEP não encontrado");
    }

    @Test
    void consultarCep_DeveRecusarCepForaDoFormatoSemChamarOFornecedor() {
        EnderecoServiceImpl comValidadorReal = new EnderecoServiceImpl(repository, clienteService, cidadeRepository,
                estadoRepository, Validation.buildDefaultValidatorFactory().getValidator(), usuarioAtual, gatewayCep);

        assertThatThrownBy(() -> comValidadorReal.consultarCep("0100-100"))
                .isInstanceOf(ValidacaoException.class);
        verify(gatewayCep, never()).consultar(any());
    }
}
