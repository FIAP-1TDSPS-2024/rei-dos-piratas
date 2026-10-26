package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagamentoCartaoTest {

    private final DadosCartaoCredito cartaoNovo = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987");

    @Test
    void deveExigirCartaoSalvoOuCartaoNovo() {
        assertThatThrownBy(() -> new PagamentoCartao(null, null, 1, "200.1.2.3"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void naoDevePermitirCartaoSalvoECartaoNovoAoMesmoTempo() {
        assertThatThrownBy(() -> new PagamentoCartao(5L, cartaoNovo, 1, "200.1.2.3"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void deveAceitarApenasCartaoSalvo() {
        PagamentoCartao pagamento = new PagamentoCartao(5L, null, 1, "200.1.2.3");

        assertThat(pagamento.idCartaoSalvo()).isEqualTo(5L);
    }

    @Test
    void deveAceitarApenasCartaoNovo() {
        PagamentoCartao pagamento = new PagamentoCartao(null, cartaoNovo, 2, "200.1.2.3");

        assertThat(pagamento.novoCartao()).isEqualTo(cartaoNovo);
    }
}
