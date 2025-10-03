package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaCidadeEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaEstadoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaPaisEntity;

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
                        endereco.getCidade().getEstado().getPais().getId(),
                        endereco.getCidade().getEstado().getPais().getPaisNome(),
                        endereco.getCidade().getEstado().getPais().getPaisSigla());
            }
        }
        return null;
    }

    public static JpaEnderecoEntity toJpaEntity(Endereco endereco, Cliente cliente) {
        if (endereco != null) {
            return new JpaEnderecoEntity(
                    endereco.getId(),
                    endereco.getNumero(),
                    endereco.getCep(),
                    endereco.getLogradouro(),
                    endereco.getBairro(),
                    new JpaCidadeEntity(
                            endereco.getCidadeId(),
                            endereco.getCidade(),
                            new JpaEstadoEntity(
                                    endereco.getEstadoId(),
                                    endereco.getEstadoNome(),
                                    endereco.getEstadoSigla(),
                                    new JpaPaisEntity(endereco.getPaisId(),
                                            endereco.getPaisNome(),
                                            endereco.getPaisSigla())
                            )),
                    JpaClienteMapper.toJpaEntity(cliente));
        }
        return null;
    }
}
