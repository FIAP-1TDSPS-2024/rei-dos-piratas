package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;

    private final ProdutoRepository produtoRepository;

    public PedidoServiceImpl(PedidoRepository repository, ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Page<Pedido> findAllByCliente(int pageNumber, int pageSize, Long clienteId) {
        return this.repository
                .listAllByClient(
                    pageNumber,
                    pageSize,
                    clienteId);
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
        //Verificar se os produtos tem estoque suficiente para realizar o pedido
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    if ((produto.getQuantidade() - produto.getProduto().getEstoque()) >= 0){
                        //Faz subtração do estoque se for possível
                        produto.getProduto().setEstoque(produto.getProduto().getEstoque() - produto.getQuantidade());
                        //Salva novo estoque em banco
                        this.produtoRepository.update(produto.getProduto());
                    }
                    else {
                        throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                                produto.getProduto().getNome() + " de ID " +
                                produto.getProduto().getId() + " tem apenas " +
                                produto.getProduto().getEstoque() + " unidades em estoque.");
                    }
                });

        return this.repository.create(pedido);
    }

    @Transactional
    @Override
    public Pedido pagarPedido(Long id) {
        //Busca pedido por ID
        Pedido pedido = this.findById(id);

        //Verifica se o pedido está cancelado
        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO){
            pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
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
                    "para ser enviado para entrega, mas ele está no estado " + pedido.getStatus());
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
                    "para ser entregue, mas ele está no estado " + pedido.getStatus());
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
        else {
            pedido.setStatus(StatusEnum.CANCELADO);
            return this.repository.update(pedido);
        }
    }
}
