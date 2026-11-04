package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Volume;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Envio de volta (logística reversa) de uma devolução: o cliente envia à loja usando o envio original. */
public record SolicitacaoEnvioReverso(
        Long servico,
        String emailRemetente,
        String telefoneRemetente,
        BigDecimal valorSeguro,
        UUID envioOriginal,
        Volume volume
) {
    public static SolicitacaoEnvioReverso para(Devolucao devolucao, Long servico) {
        List<ItemProdutoPedido> itensDevolvidos = devolucao.getItens().stream()
                .map(item -> new ItemProdutoPedido(item.getItemPedido().getProduto(), item.getQuantidade()))
                .toList();
        return new SolicitacaoEnvioReverso(
                servico,
                devolucao.getPedido().getCliente().getEmail(),
                devolucao.getPedido().getCliente().getCelular(),
                devolucao.getValorTotal(),
                devolucao.getPedido().getPedidoFrete(),
                Volume.para(itensDevolvidos));
    }

    @Override
    public String toString() {
        return "SolicitacaoEnvioReverso[servico=" + servico + ", envioOriginal=" + envioOriginal + "]";
    }
}
