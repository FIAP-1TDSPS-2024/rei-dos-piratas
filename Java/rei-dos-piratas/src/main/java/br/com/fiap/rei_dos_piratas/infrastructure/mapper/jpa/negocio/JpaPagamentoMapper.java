package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPagamentoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;

public class JpaPagamentoMapper {

    public static Pagamento toEntity(JpaPagamentoEntity jpaPagamento) {
        if (jpaPagamento == null) {
            return null;
        }
        return new Pagamento(
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
                jpaPagamento.getDataConfirmacao());
    }

    public static JpaPagamentoEntity toJpaEntity(Pagamento pagamento, JpaPedidoEntity pedido) {
        if (pagamento == null) {
            return null;
        }
        return new JpaPagamentoEntity(
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
                pagamento.getDataConfirmacao());
    }

    private JpaPagamentoMapper() {}
}
