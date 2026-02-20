package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;

    private final ProdutoRepository produtoRepository;

    private final ClienteService clienteService;

    private final EnderecoService enderecoService;
    
    private final DadosEmpresaRepository dadosEmpresaRepository;

    private final FreteService freteService;

    public PedidoServiceImpl(PedidoRepository repository, ProdutoRepository produtoRepository, ClienteService clienteService, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.clienteService = clienteService;
        this.enderecoService = enderecoService;
        this.dadosEmpresaRepository = dadosEmpresaRepository;
        this.freteService = freteService;
    }

    @Override
    public Page<Pedido> findAll(int pageNumber, int pageSize) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            return this.repository
                        .listAllByClient(
                                pageNumber,
                                pageSize,
                                userDetails.getId());
            }
            else {
                return this.repository.listAll(
                                pageNumber,
                                pageSize);
            }
    }

    @Override
    public Pedido findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um pedido com o id " + id);
        }
    }

    @Transactional
    @Override
    public Pedido fazerPedido(Pedido pedido) {
        this.verificaEAtualizaEstoqueparaPedido(pedido);
        return this.repository.create(pedido);
    }

    @Transactional
    @Override
    public Pedido pagarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO){
            pedido.setStatus(StatusEnum.AGUARDANDO_NF);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.AGUARDANDO_PAGAMENTO +
                    "para ser pago, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public Pedido enviarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.PREPARANDO_ENVIO){
            pedido.setStatus(StatusEnum.EM_TRANSITO);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.PREPARANDO_ENVIO +
                    " para ser enviado para entrega, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public Pedido entregarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.EM_TRANSITO){
            pedido.setStatus(StatusEnum.ENTREGUE);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.EM_TRANSITO +
                    " para ser entregue, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public Pedido cancelarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.CANCELADO){
            throw new WrongStatusException("O pedido já está cancelado.");
        }
        else if (pedido.getStatus() == StatusEnum.ENTREGUE) {
            throw new WrongStatusException("O pedido já foi entregue");
        }
        else if ((pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) || pedido.getStatus() == StatusEnum.PREPARANDO_ENVIO) {
            //Retorna os produtos para estoque
            pedido.getProdutosAdicionados()
                    .forEach(produto -> {
                        produto
                                .getProduto()
                                .setEstoque(
                                        produto.getProduto().getEstoque() +
                                        produto.getQuantidade());

                        this.produtoRepository.update(produto.getProduto());
                    });
            pedido.setStatus(StatusEnum.CANCELADO);
            return this.repository.update(pedido);
        }
        else {
            throw new WrongStatusException("O pedido já foi enviado. Para fazer o cancelamento, deve ser solicitada uma devolução.");
        }
    }

    private void verificaEAtualizaEstoqueparaPedido(Pedido pedido) {
        //Verificar se os todos os produtos tem estoque suficiente para lançar exceção se não
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    if (produto.getProduto().getEstoque() < produto.getQuantidade()) {
                        throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                                produto.getProduto().getNome() + " de ID " +
                                produto.getProduto().getId() + " tem apenas " +
                                produto.getProduto().getEstoque() + " unidades em estoque.");
                    }
                });

        //Se todos os itens estiverem OK, o estoque é editado
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    //Faz subtração do estoque se for possível
                    produto.getProduto().setEstoque(produto.getProduto().getEstoque() - produto.getQuantidade());
                    //Salva novo estoque em banco
                    this.produtoRepository.update(produto.getProduto());
                });
    }

    private PedidoFreteRequestDto montarPedidoFreteDto(Pedido pedido) {
        //Definir empresa como remetente
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();
        DadosEmpresa dadosEmpresa = this.dadosEmpresaRepository.get();
        DestinoRemetenteDto remetente = this.definirRemetenteEmpresa(enderecoEmpresa, dadosEmpresa);

        //Definir cliente como destino
        Endereco enderecoDestino = this.enderecoService.findById(pedido.getEnderecoEntrega().getId());
        Cliente cliente = pedido.getCliente();
        DestinoRemetenteDto destino = this.definirDestinoEntrega(enderecoDestino, cliente);

        //Definir lista de produtos e quantidade
        List<ProdutoPedidoFreteDto> produtos = this.definirProdutosFrete(pedido);

        //Definir dimensões do(s) volume(s) para entrega
        List<VolumeFreteDto> volumes = new ArrayList<>();
        volumes.add(definirDimensosVolumes(pedido));

        //Definir opções de entrega:
        FreteOptionsDto opcoes = new FreteOptionsDto(
                "Rei dos Piratas",
                "Entrega do pedido " + pedido.getId(),
                pedido.getValorTotal(),
                false,
                false,
                false,
                false,
                null);

        return new PedidoFreteRequestDto(
                pedido.getServicoEntrega(),
                remetente,
                destino,
                produtos,
                volumes,
                opcoes);
    }

    private DestinoRemetenteDto definirRemetenteEmpresa(Endereco enderecoEmpresa, DadosEmpresa dadosEmpresa) {
        return new DestinoRemetenteDto(
                dadosEmpresa.getNomeFantasia(),
                dadosEmpresa.getEmail(),
                dadosEmpresa.getTelefone(),
                null,
                dadosEmpresa.getCnpj(),
                dadosEmpresa.getStateRegister(),
                dadosEmpresa.getEconomicActivityCode(),
                enderecoEmpresa.getLogradouro(),
                null,
                String.valueOf(enderecoEmpresa.getNumero()),
                enderecoEmpresa.getBairro(),
                enderecoEmpresa.getCidade().getNome(),
                enderecoEmpresa.getCep(),
                enderecoEmpresa.getCidade().getEstado().getSigla());
    }

    private DestinoRemetenteDto definirDestinoEntrega(Endereco enderecoDestino, Cliente cliente) {
        return new DestinoRemetenteDto(
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getCelular(),
                cliente.getCpf(),
                null,
                null,
                null,
                enderecoDestino.getLogradouro(),
                null,
                String.valueOf(enderecoDestino.getNumero()),
                enderecoDestino.getBairro(),
                enderecoDestino.getCidade().getNome(),
                enderecoDestino.getCep(),
                enderecoDestino.getCidade().getEstado().getSigla());
    }

    private List<ProdutoPedidoFreteDto> definirProdutosFrete(Pedido pedido) {
        return pedido.getProdutosAdicionados()
                .stream()
                .map(produto -> new ProdutoPedidoFreteDto(
                        produto.getProduto().getNome(),
                        String.valueOf(produto.getQuantidade()),
                        String.valueOf(produto.getProduto().getPreco())))
                .toList();
    }

    private VolumeFreteDto definirDimensosVolumes(Pedido pedido) {
        int quantidadeTotal = pedido.getProdutosAdicionados().stream()
                .mapToInt(ItemProdutoPedido::getQuantidade)
                .sum();

        BigDecimal pesoTotal = pedido.getProdutosAdicionados().stream()
                .map(produto -> produto.getProduto().getPeso().multiply(BigDecimal.valueOf(produto.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (quantidadeTotal < 6) {
            int altura = pedido.getProdutosAdicionados().stream()
                    .mapToInt(produto -> produto.getProduto().getProfundidade().intValue() * produto.getQuantidade())
                    .sum() + 3;

            return new VolumeFreteDto(altura, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 11) {
            return new VolumeFreteDto(28, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 20) {
            return new VolumeFreteDto(28, 32, 26, pesoTotal);
        }

        return new VolumeFreteDto(28, 32, 52, pesoTotal);
    }
}
