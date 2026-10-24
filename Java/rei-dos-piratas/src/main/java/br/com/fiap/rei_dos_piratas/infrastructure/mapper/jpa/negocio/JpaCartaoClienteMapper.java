package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCartaoClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaClienteMapper;

public class JpaCartaoClienteMapper {

    public static CartaoCliente toEntity(JpaCartaoClienteEntity jpaCartao) {
        return new CartaoCliente(
                jpaCartao.getId(),
                JpaClienteMapper.toEntity(jpaCartao.getCliente()),
                jpaCartao.getToken(),
                jpaCartao.getUltimosDigitos(),
                jpaCartao.getBandeira(),
                jpaCartao.getAtivo(),
                jpaCartao.getDataCadastro());
    }

    /** {@code cliente} deve ser uma referência gerenciada (ex.: {@code EntityManager.getReference}). */
    public static JpaCartaoClienteEntity toJpaEntity(CartaoCliente cartao, JpaClienteEntity cliente) {
        return new JpaCartaoClienteEntity(
                cartao.getId(),
                cliente,
                cartao.getToken(),
                cartao.getUltimosDigitos(),
                cartao.getBandeira(),
                cartao.getAtivo(),
                cartao.getDataCadastro());
    }

    /** Só o id: o pagamento referencia o cartão sem regravá-lo. */
    public static JpaCartaoClienteEntity toReferencia(CartaoCliente cartao) {
        JpaCartaoClienteEntity referencia = new JpaCartaoClienteEntity();
        referencia.setId(cartao.getId());
        return referencia;
    }

    private JpaCartaoClienteMapper() {}
}
