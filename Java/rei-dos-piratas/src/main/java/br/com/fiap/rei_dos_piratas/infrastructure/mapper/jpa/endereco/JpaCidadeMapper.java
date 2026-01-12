package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.endereco;

import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaCidadeEntity;

public class JpaCidadeMapper {

    public static Cidade toEntity(JpaCidadeEntity cidade) {
        if (cidade == null) {
            return null;
        }

        return new Cidade(
                cidade.getId(),
                cidade.getCidadeNome(),
                JpaEstadoMapper.toEntity(cidade.getEstado()));
    }

    public static JpaCidadeEntity toJpaEntity(Cidade cidade) {
        if (cidade == null) {
            return null;
        }

        return new JpaCidadeEntity(
                cidade.getId(),
                cidade.getNome(),
                JpaEstadoMapper.toJpaEntity(cidade.getEstado()));
    }

    private JpaCidadeMapper() {}
}
