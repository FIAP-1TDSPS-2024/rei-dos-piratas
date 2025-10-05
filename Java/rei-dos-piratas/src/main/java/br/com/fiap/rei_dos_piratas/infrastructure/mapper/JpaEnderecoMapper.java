package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.*;

public class JpaEnderecoMapper {

    public static Endereco toEntity(JpaEnderecoEntity endereco){
        if (endereco != null) {
            if (endereco.getCidade() != null){
                return new Endereco(endereco.getId(),
                        endereco.getNumero(),
                        endereco.getCep(),
                        endereco.getLogradouro(),
                        endereco.getBairro(),
                        endereco.getCidade().getId(),
                        endereco.getCidade().getCidadeNome(),
                        endereco.getCidade().getEstado().getId(),
                        endereco.getCidade().getEstado().getEstadoNome(),
                        endereco.getCidade().getEstado().getEstadoSigla(),
                        "Brasil",
                        "BRA");
            }
        }
        return null;
    }

    public static JpaEnderecoEntity toJpaEntity(Endereco endereco) {
        if (endereco == null) return null;

        JpaEstadoEntity jpaEstado = new JpaEstadoEntity(
                endereco.getEstadoId(),
                endereco.getEstadoNome(),
                endereco.getEstadoSigla()
        );

        JpaCidadeEntity jpaCidade = new JpaCidadeEntity(
                endereco.getCidadeId(),
                endereco.getCidade(),
                jpaEstado
        );


        return new JpaEnderecoEntity(
                endereco.getId(),
                endereco.getNumero(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getBairro(),
                jpaCidade,
                null
        );
    }
}
