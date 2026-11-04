package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/** Pagamento de um pedido (um por pedido; fica dentro do agregado Pedido). */
@Data
@NoArgsConstructor
public class Pagamento {
    private Long id;

    @NotNull(message = "O tipo de pagamento é obrigatório")
    private TipoPagamentoEnum tipo;

    @NotNull(message = "O status do pagamento é obrigatório")
    private StatusPagamentoEnum status;

    /** ID da cobrança na API de pagamentos (Asaas). */
    @Size(max = 50, message = "O ID da cobrança deve ter até 50 caracteres")
    private String idCobranca;

    @NotNull(message = "O valor do pagamento é obrigatório")
    @Digits(integer = 10, fraction = 2, message = "O valor do pagamento deve ter até 12 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O valor do pagamento deve ser maior que zero")
    private BigDecimal valor;

    @Digits(integer = 10, fraction = 2, message = "O valor líquido deve ter até 12 dígitos com 2 após a vírgula")
    private BigDecimal valorLiquido;

    @NotNull(message = "A quantidade de parcelas é obrigatória")
    @Min(value = 1, message = "O pagamento deve ter pelo menos uma parcela")
    private Integer parcelas;

    @Valid
    private CartaoCliente cartao;

    @Size(max = 255, message = "O motivo da recusa deve ter até 255 caracteres")
    private String motivoRecusa;

    @NotNull(message = "A data de criação do pagamento é obrigatória")
    @PastOrPresent(message = "A data de criação do pagamento deve estar no presente ou passado")
    private LocalDateTime dataCriacao;

    @PastOrPresent(message = "A data de confirmação do pagamento deve estar no presente ou passado")
    private LocalDateTime dataConfirmacao;

    /** PDF do boleto no Asaas (só boleto). */
    @Size(max = 500, message = "A URL do boleto deve ter até 500 caracteres")
    private String urlBoleto;

    /** Vencimento da cobrança no Asaas (boleto/PIX). */
    private LocalDate dataVencimento;

    /** Estornos do pagamento, do mais antigo ao mais recente (devolução parcial gera mais de um). */
    @Valid
    private List<Estorno> estornos = new ArrayList<>();

    public Pagamento(Long id, TipoPagamentoEnum tipo, StatusPagamentoEnum status, String idCobranca, BigDecimal valor,
                     BigDecimal valorLiquido, Integer parcelas, CartaoCliente cartao, String motivoRecusa,
                     LocalDateTime dataCriacao, LocalDateTime dataConfirmacao, String urlBoleto, LocalDate dataVencimento) {
        this.id = id;
        this.tipo = tipo;
        this.status = status;
        this.idCobranca = idCobranca;
        this.valor = valor;
        this.valorLiquido = valorLiquido;
        this.parcelas = parcelas;
        this.cartao = cartao;
        this.motivoRecusa = motivoRecusa;
        this.dataCriacao = dataCriacao;
        this.dataConfirmacao = dataConfirmacao;
        this.urlBoleto = urlBoleto;
        this.dataVencimento = dataVencimento;
    }

    /**
     * Registra a intenção de estornar {@code valor}. Exige pagamento confirmado ou recebido e não deixa
     * o total estornado (concluído ou em aberto) passar do valor pago.
     */
    public Estorno solicitarEstorno(OrigemEstornoEnum origem, Long devolucaoId, BigDecimal valor) {
        if (this.status != StatusPagamentoEnum.CONFIRMADO && this.status != StatusPagamentoEnum.RECEBIDO) {
            throw new RegraDeNegocioException("Só é possível estornar um pagamento confirmado ou recebido.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException("O valor do estorno deve ser maior que zero.");
        }
        if (devolucaoId != null && this.estornos.stream().anyMatch(estorno -> devolucaoId.equals(estorno.getDevolucaoId()))) {
            throw new RegraDeNegocioException("Esta devolução já foi reembolsada ou está em reembolso.");
        }
        BigDecimal comprometido = this.estornos.stream()
                .filter(estorno -> estorno.getStatus() == StatusEstornoEnum.CONCLUIDO || estorno.getStatus().emAberto())
                .map(Estorno::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (comprometido.add(valor).compareTo(this.valor) > 0) {
            throw new RegraDeNegocioException("O valor do estorno ultrapassa o valor pago.");
        }

        Estorno estorno = new Estorno(null, origem, devolucaoId, valor, StatusEstornoEnum.SOLICITADO,
                null, null, LocalDateTime.now(), null);
        this.estornos.add(estorno);
        return estorno;
    }

    /** Soma dos estornos concluídos. */
    public BigDecimal valorEstornado() {
        return this.estornos.stream()
                .filter(estorno -> estorno.getStatus() == StatusEstornoEnum.CONCLUIDO)
                .map(Estorno::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Conclui o estorno; com o valor pago todo devolvido, o pagamento vira ESTORNADO. */
    public void concluirEstorno(Estorno estorno) {
        if (estorno.getStatus() == StatusEstornoEnum.CONCLUIDO) {
            return;
        }
        estorno.setStatus(StatusEstornoEnum.CONCLUIDO);
        estorno.setDataConclusao(LocalDateTime.now());
        if (this.valorEstornado().compareTo(this.valor) >= 0) {
            this.status = StatusPagamentoEnum.ESTORNADO;
        }
    }

    /** Estorno em aberto mais antigo (o fornecedor não diz qual estorno o evento conclui). */
    public Optional<Estorno> estornoEmAberto() {
        return this.estornos.stream().filter(estorno -> estorno.getStatus().emAberto()).findFirst();
    }

    /** Pagamento escolhido na criação do pedido, antes de existir cobrança no Asaas. */
    public static Pagamento pendente(TipoPagamentoEnum tipo) {
        Pagamento pagamento = new Pagamento();
        pagamento.setTipo(tipo);
        pagamento.setStatus(StatusPagamentoEnum.PENDENTE);
        pagamento.setParcelas(1);
        pagamento.setDataCriacao(LocalDateTime.now());
        return pagamento;
    }
}
