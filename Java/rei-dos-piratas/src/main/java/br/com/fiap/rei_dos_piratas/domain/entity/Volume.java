package br.com.fiap.rei_dos_piratas.domain.entity;

import java.math.BigDecimal;
import java.util.List;

/** Caixa do envio, em cm e kg. A regra de dimensões por quantidade de itens é da loja. */
public record Volume(int altura, int largura, int comprimento, BigDecimal peso) {

    public static Volume para(List<ItemProdutoPedido> itens) {
        int quantidadeTotal = itens.stream().mapToInt(ItemProdutoPedido::getQuantidade).sum();

        BigDecimal pesoTotal = itens.stream()
                .map(item -> item.getProduto().getPeso().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (quantidadeTotal < 6) {
            // Itens empilhados: altura pela soma das profundidades + 3 cm de folga
            int altura = itens.stream()
                    .mapToInt(item -> item.getProduto().getProfundidade().intValue() * item.getQuantidade())
                    .sum() + 3;
            return new Volume(altura, 16, 26, pesoTotal);
        }
        if (quantidadeTotal < 11) {
            return new Volume(28, 16, 26, pesoTotal);
        }
        if (quantidadeTotal < 20) {
            return new Volume(28, 32, 26, pesoTotal);
        }
        return new Volume(28, 32, 52, pesoTotal);
    }
}
