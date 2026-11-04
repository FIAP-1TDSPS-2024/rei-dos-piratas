package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaEstornoEntity;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPagamentoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;

public class JpaPagamentoMapper {

    public static Pagamento toEntity(JpaPagamentoEntity jpaPagamento) {
        if (jpaPagamento == null) {
            return null;
        }
        Pagamento pagamento = new Pagamento(
                jpaPagamento.getId(),
                jpaPagamento.getTipo(),
                jpaPagamento.getStatus(),
                jpaPagamento.getIdCobranca(),
                jpaPagamento.getValor(),
                jpaPagamento.getValorLiquido(),
                jpaPagamento.getParcelas(),
                jpaPagamento.getCartao() == null ? null : JpaCartaoClienteMapper.toEntity(jpaPagamento.getCartao()),
                jpaPagamento.getMotivoRecusa(),
                jpaPagamento.getDataCriacao(),
                jpaPagamento.getDataConfirmacao(),
                jpaPagamento.getUrlBoleto(),
                jpaPagamento.getDataVencimento());
        jpaPagamento.getEstornos().forEach(jpa -> pagamento.getEstornos().add(new Estorno(
                jpa.getId(), jpa.getOrigem(), jpa.getDevolucaoId(), jpa.getValor(), jpa.getStatus(),
                jpa.getUrlSolicitacaoBoleto(), jpa.getMotivoFalha(), jpa.getDataSolicitacao(), jpa.getDataConclusao())));
        return pagamento;
    }

    public static JpaPagamentoEntity toJpaEntity(Pagamento pagamento, JpaPedidoEntity pedido) {
        if (pagamento == null) {
            return null;
        }
        JpaPagamentoEntity jpa = new JpaPagamentoEntity(
                pagamento.getId(),
                pedido,
                pagamento.getTipo(),
                pagamento.getStatus(),
                pagamento.getIdCobranca(),
                pagamento.getValor(),
                pagamento.getValorLiquido(),
                pagamento.getParcelas() == null ? 1 : pagamento.getParcelas(),
                pagamento.getCartao() == null ? null : JpaCartaoClienteMapper.toReferencia(pagamento.getCartao()),
                pagamento.getMotivoRecusa(),
                pagamento.getDataCriacao(),
                pagamento.getDataConfirmacao(),
                pagamento.getUrlBoleto(),
                pagamento.getDataVencimento());
        // Estorno com id atualiza a linha existente; sem id, é inserido (cascade a partir do pagamento)
        pagamento.getEstornos().forEach(estorno -> jpa.getEstornos().add(new JpaEstornoEntity(
                estorno.getId(), jpa, estorno.getOrigem(), estorno.getDevolucaoId(), estorno.getValor(), estorno.getStatus(),
                estorno.getUrlSolicitacaoBoleto(), estorno.getMotivoFalha(), estorno.getDataSolicitacao(), estorno.getDataConclusao())));
        return jpa;
    }

    private JpaPagamentoMapper() {}
}
