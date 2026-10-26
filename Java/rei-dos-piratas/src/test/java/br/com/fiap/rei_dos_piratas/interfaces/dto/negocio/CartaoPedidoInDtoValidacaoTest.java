package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.PagamentoCartao;
import br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio.PagamentoCartaoCreditoMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartaoPedidoInDtoValidacaoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @Test
    void cartaoSalvoNaoExigeDadosDeCartaoNovo() {
        CartaoPedidoInDto dto = new CartaoPedidoInDto(5L, null, null, null, null, null, 1);

        assertThat(validator.validate(dto)).isEmpty();

        PagamentoCartao pagamento = PagamentoCartaoCreditoMapper.toEntity(dto);
        assertThat(pagamento.idCartaoSalvo()).isEqualTo(5L);
        assertThat(pagamento.novoCartao()).isNull();
    }

    @Test
    void dadosDeCartaoNovoSaoValidadosEmCascata() {
        PedidoInDto pedido = new PedidoInDto(
                1L,
                1L,
                List.of(),
                TipoPagamentoEnum.CREDIT_CARD,
                new CartaoPedidoInDto(null, "JOAO SILVA", "invalido", "12", "2030", "987", 1));

        assertThat(validator.validate(pedido))
                .anyMatch(violacao -> violacao.getPropertyPath().toString().endsWith(".numero"));
    }

    @Test
    void cartaoNovoValidoEMapeadoComSeusDados() {
        CartaoPedidoInDto dto = new CartaoPedidoInDto(null, "JOAO SILVA", "4111111111111234", "12", "2030", "987", 1);

        assertThat(validator.validate(dto)).isEmpty();

        PagamentoCartao pagamento = PagamentoCartaoCreditoMapper.toEntity(dto);
        assertThat(pagamento.idCartaoSalvo()).isNull();
        assertThat(pagamento.novoCartao().numero()).isEqualTo("4111111111111234");
    }
}
