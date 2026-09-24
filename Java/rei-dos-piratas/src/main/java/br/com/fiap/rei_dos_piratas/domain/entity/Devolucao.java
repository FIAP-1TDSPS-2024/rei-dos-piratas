package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Devolucao {

    private Long id;

    @NotNull(message = "O pedido da devolução não pode ser nulo")
    private Pedido pedido;

    @NotNull(message = "O motivo da devolução não pode ser nulo")
    private MotivoDevolucao motivo;

    @Size(max = 500, message = "A descrição da devolução deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "A devolução deve possuir itens")
    private List<ItemDevolucao> itens;

    @Digits(fraction = 2, integer = 6, message = "O preço total do pedido deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço total não pode ser negativo")
    private BigDecimal valorTotal;

    @Digits(fraction = 2, integer = 6, message = "O preço do frete do pedido deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço do frete do pedido não pode ser negativo")
    private BigDecimal valorFrete;

    @NotNull(message = "O status do pedido não pode ser nulo")
    private StatusDevolucaoEnum status;

    @NotNull(message = "A data da solicitação não pode ser nula")
    @PastOrPresent(message = "A data de solicitação deve estar no presente ou passado")
    private LocalDate dataSolicitacao;

    @PastOrPresent(message = "A data de aprovação deve estar no presente ou passado")
    private LocalDate dataAprovacao;

    @PastOrPresent(message = "A data de conclusão deve estar no presente ou passado")
    private LocalDate dataConclusao;

    private Boolean aprovada;

    @NotNull(message = "O pedido deve definir um serviço para entrega")
    private Long servicoEntrega;

    private UUID pedidoFrete;

    private String protocoloEnvio;

    private String statusEnvio;

    private String tracking;

    private String trackingUrl;

    public Devolucao(Long id,
                     Pedido pedido,
                     MotivoDevolucao motivo,
                     String descricao,
                     List<ItemDevolucao> itens,
                     LocalDate dataSolicitacao,
                     LocalDate dataAprovacao,
                     LocalDate dataConclusao,
                     Boolean aprovada) {
        this.id = id;
        this.pedido = pedido;
        this.motivo = motivo;
        this.descricao = descricao;
        this.itens = itens == null ? Collections.emptyList() : itens;
        this.dataSolicitacao = dataSolicitacao;
        this.dataAprovacao = dataAprovacao;
        this.dataConclusao = dataConclusao;
        this.aprovada = aprovada;
        validarPedido(this.itens, pedido, false);
    }

    public Devolucao(Pedido pedido, MotivoDevolucao motivo, String descricao, List<ItemDevolucao> itens) {
        this(pedido, motivo, descricao, null, itens);
    }

    public Devolucao(Pedido pedido, MotivoDevolucao motivo, String descricao, Long servicoEntrega, List<ItemDevolucao> itens) {
        this.pedido = pedido;
        this.motivo = motivo;
        this.descricao = descricao;
        this.itens = itens;
        this.servicoEntrega = servicoEntrega != null
                ? servicoEntrega
                : (pedido != null ? pedido.getServicoEntrega() : null);
        this.status = StatusDevolucaoEnum.EM_ANALISE;
        this.dataSolicitacao = LocalDate.now();
        this.aprovada = null;
        validarPedido(itens, pedido, true);
        validarDatasEMotivos();
    }

    private void validarDatasEMotivos(){
        long diferencaDias = ChronoUnit.DAYS.between(this.getPedido().getDataEntrega(), this.getDataSolicitacao());

        //Verifica o motivo da devolução e tempo desde a entrega do pedido para definir se é necessário análise da loja
        if (this.getMotivo().getArrependimento() && diferencaDias > 7) {
            throw new RegraDeNegocioException("A devolução de pedidos pelo motivo '" + this.getMotivo().getDescricao() + "' só pode ser solicitada em até uma semana após a entrega do produto");
        }
        else if (diferencaDias > 90){
            throw new RegraDeNegocioException("A devolução de pedidos só é possível em até 90 dias após a entrega do produto.");
        }
    }

    private static void validarPedido(List<ItemDevolucao> itens, Pedido pedido, boolean obrigatorios) {
        if (obrigatorios && (itens == null || itens.isEmpty())) {
            throw new RegraDeNegocioException("Informe ao menos um item do pedido para devolucao.");
        }

        if (itens == null || itens.isEmpty()) {
            return;
        }

        if (pedido == null || pedido.getProdutosAdicionados() == null || pedido.getProdutosAdicionados().isEmpty()) {
            throw new RegraDeNegocioException("Pedido invalido para associar itens de devolucao.");
        }

        if (!pedido.getStatus().equals(StatusEnum.ENTREGUE)){
            throw new RegraDeNegocioException("O pedido deve estar entregue para que seja feita uma solicitação de devolução");
        }

        Map<Long, Integer> quantidadePorItemPedido = pedido.getProdutosAdicionados().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ItemProdutoPedido::getId, ItemProdutoPedido::getQuantidade));

        Map<Long, Integer> quantidadeSolicitadaPorItem = itens.stream()
                .peek(item -> {
                    if (item == null || item.getItemPedido() == null || item.getItemPedido().getId() == null) {
                        throw new RegraDeNegocioException("Todos os itens da devolucao devem referenciar um item do pedido.");
                    }
                    if (item.getQuantidade() <= 0) {
                        throw new RegraDeNegocioException("A quantidade de cada item devolvido deve ser maior que zero.");
                    }
                })
                .collect(Collectors.groupingBy(item -> item.getItemPedido().getId(), Collectors.summingInt(ItemDevolucao::getQuantidade)));

        for (Map.Entry<Long, Integer> itemSolicitado : quantidadeSolicitadaPorItem.entrySet()) {
            Integer quantidadeDisponivel = quantidadePorItemPedido.get(itemSolicitado.getKey());
            if (quantidadeDisponivel == null) {
                throw new RegraDeNegocioException("Item informado nao pertence ao pedido.");
            }
            if (itemSolicitado.getValue() > quantidadeDisponivel) {
                throw new RegraDeNegocioException("Quantidade devolvida maior que a quantidade comprada para um dos itens.");
            }
        }

        Map<Long, ItemProdutoPedido> itensDoPedido = pedido.getProdutosAdicionados().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ItemProdutoPedido::getId, Function.identity()));

        itens.forEach(item -> item.setItemPedido(itensDoPedido.get(item.getItemPedido().getId())));
    }
}
