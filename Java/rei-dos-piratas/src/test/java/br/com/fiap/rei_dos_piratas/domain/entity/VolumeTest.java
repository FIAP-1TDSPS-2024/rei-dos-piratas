package br.com.fiap.rei_dos_piratas.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Regra de dimensões da caixa por quantidade total de itens (a mesma do pedido e da devolução). */
class VolumeTest {

    private ItemProdutoPedido item(String profundidade, String peso, int quantidade) {
        Produto produto = new Produto();
        produto.setProfundidade(new BigDecimal(profundidade));
        produto.setPeso(new BigDecimal(peso));
        produto.setPreco(BigDecimal.TEN);
        return new ItemProdutoPedido(produto, quantidade);
    }

    @Test
    void ateCincoItensAlturaEhASomaDasProfundidadesMaisTresCm() {
        Volume volume = Volume.para(List.of(item("2.0", "0.3", 2), item("1.5", "0.2", 3)));

        // 2 x 2 + 1 x 3 (parte inteira da profundidade) + 3
        assertThat(volume.altura()).isEqualTo(10);
        assertThat(volume.largura()).isEqualTo(16);
        assertThat(volume.comprimento()).isEqualTo(26);
        assertThat(volume.peso()).isEqualByComparingTo("1.2");
    }

    @Test
    void deSeisADezItensCaixaPequena() {
        Volume volume = Volume.para(List.of(item("2.0", "0.3", 6)));

        assertThat(volume.altura()).isEqualTo(28);
        assertThat(volume.largura()).isEqualTo(16);
        assertThat(volume.comprimento()).isEqualTo(26);
        assertThat(volume.peso()).isEqualByComparingTo("1.8");
    }

    @Test
    void deOnzeADezenoveItensCaixaMedia() {
        Volume volume = Volume.para(List.of(item("2.0", "0.1", 11)));

        assertThat(volume.altura()).isEqualTo(28);
        assertThat(volume.largura()).isEqualTo(32);
        assertThat(volume.comprimento()).isEqualTo(26);
    }

    @Test
    void vinteItensOuMaisCaixaGrande() {
        Volume volume = Volume.para(List.of(item("2.0", "0.1", 20)));

        assertThat(volume.altura()).isEqualTo(28);
        assertThat(volume.largura()).isEqualTo(32);
        assertThat(volume.comprimento()).isEqualTo(52);
        assertThat(volume.peso()).isEqualByComparingTo("2.0");
    }
}
