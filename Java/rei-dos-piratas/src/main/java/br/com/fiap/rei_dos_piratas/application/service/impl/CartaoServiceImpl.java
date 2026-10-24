package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.CartaoClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.CartaoCreditoRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.TitularCartaoRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.TokenizacaoCartaoRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.TokenizacaoCartaoResponseDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
public class CartaoServiceImpl implements CartaoService {

    private final CartaoClienteRepository repository;
    private final CobrancaAppClient apiCobranca;
    private final UsuarioAtualService usuarioAtual;

    public CartaoServiceImpl(CartaoClienteRepository repository, CobrancaAppClient apiCobranca, UsuarioAtualService usuarioAtual) {
        this.repository = repository;
        this.apiCobranca = apiCobranca;
        this.usuarioAtual = usuarioAtual;
    }

    @Override
    public CartaoCliente obterOuTokenizar(Cliente cliente, Endereco endereco, PagamentoCartao pagamentoCartao) {
        log.info("[CARTAO] Obtendo ou tokenizando cartão para o cliente ID={}", cliente.getId());

        if (pagamentoCartao.idCartaoSalvo() != null) {
            return this.obterCartaoSalvoDoCliente(cliente, pagamentoCartao.idCartaoSalvo());
        }
        else if (pagamentoCartao.novoCartao() != null) {
            log.info("[CARTAO] Tokenizando novo cartão para o cliente ID={}", cliente.getId());
            TokenizacaoCartaoRequestDto request = this.definirRequisicaoTokenizacao(
                    cliente,
                    endereco,
                    pagamentoCartao.novoCartao());

            TokenizacaoCartaoResponseDto response = this.apiCobranca.tokenizarCartao(request);

            return this.repository.create(new CartaoCliente(
                    cliente,
                    response.creditCardToken(),
                    response.creditCardNumber(),
                    response.creditCardBrand()));

        }
        else {
            throw new RegraDeNegocioException("Pagamento com cartão inválido: nem cartão salvo nem novo cartão informado");
        }
    }

    private CartaoCliente obterCartaoSalvoDoCliente(Cliente cliente, Long idCartao) {
        CartaoCliente cartao;
        try {
            cartao = this.repository.findById(idCartao);
        } catch (NoSuchElementException e) {
            cartao = null;
        }

        // Mesma resposta para cartão alheio e inexistente, sem revelar que o ID existe
        if (cartao == null || !cartao.getCliente().getId().equals(cliente.getId())) {
            log.warn("[CARTAO] Cartão ID={} não pertence ao cliente ID={}", idCartao, cliente.getId());
            throw new ResourceNotFoundException("Cartão não encontrado");
        }
        if (!Boolean.TRUE.equals(cartao.getAtivo())) {
            throw new RegraDeNegocioException("O cartão informado está inativo");
        }
        return cartao;
    }

    private TokenizacaoCartaoRequestDto definirRequisicaoTokenizacao(Cliente cliente, Endereco endereco, DadosCartaoCredito dadosCartao) {

        CartaoCreditoRequestDto cartaoCredito = new CartaoCreditoRequestDto(
                dadosCartao.titular(),
                dadosCartao.numero(),
                dadosCartao.mesValidade(),
                dadosCartao.anoValidade(),
                dadosCartao.cvv());

        TitularCartaoRequestDto titular = new TitularCartaoRequestDto(
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getCpf(),
                endereco.getCep(),
                String.valueOf(endereco.getNumero()),
                cliente.getCelular());

        return new TokenizacaoCartaoRequestDto(
                cliente.getIdCobranca(),
                cartaoCredito,
                titular);
    }

    @Override
    public List<CartaoCliente> findAllByClienteId(Long clienteId) {
        return this.repository.findAllByClienteId(clienteId);
    }

    @Override
    public List<CartaoCliente> listarCartoesDoClienteAtual() {
        IdentidadeConta usuario = this.usuarioAtual.identidade();

        // IDs de cliente e funcionário vêm de tabelas diferentes e podem coincidir
        if (usuario.tipo() != TipoConta.CLIENTE) {
            log.warn("[CARTAO] Consulta de cartões negada para conta do tipo {} ID={}", usuario.tipo(), usuario.id());
            throw new RegraDeNegocioException("Apenas clientes possuem cartões salvos");
        }

        // Nunca logar token, dígitos ou bandeira: só a quantidade
        List<CartaoCliente> ativos = this.repository.findAllByClienteId(usuario.id())
                .stream()
                .filter(cartao -> Boolean.TRUE.equals(cartao.getAtivo()))
                .toList();
        log.debug("[CARTAO] {} cartão(ões) ativo(s) encontrado(s) para o cliente ID={}", ativos.size(), usuario.id());
        return ativos;
    }
}
