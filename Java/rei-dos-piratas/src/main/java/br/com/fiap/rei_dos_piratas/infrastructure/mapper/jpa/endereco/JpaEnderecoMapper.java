package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.endereco;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaCarrinhoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaClienteMapper;

public class JpaEnderecoMapper {

    public static Endereco toEntity(JpaEnderecoEntity endereco){
        if (endereco != null) {
            if (endereco.getCidade() != null){

                Long clienteId = null;

                if (endereco.getCliente() != null){
                    clienteId = endereco.getCliente().getId();
                }

                return new Endereco(
                        endereco.getId(),
                        endereco.getNumero(),
                        endereco.getCep(),
                        endereco.getLogradouro(),
                        endereco.getBairro(),
                        endereco.isEnderecoAtivo(),
                        JpaCidadeMapper.toEntity(endereco.getCidade()),
                        "Brasil",
                        "BRA",
                        JpaClienteMapper
                                .toEntity(endereco.getCliente()));
            }
        }
        return null;
    }

    public static JpaEnderecoEntity toJpaEntity(Endereco endereco) {
        if (endereco == null) return null;


        return new JpaEnderecoEntity(
                endereco.getId(),
                endereco.getNumero(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getBairro(),
                endereco.isEnderecoAtivo(),
                JpaCidadeMapper.toJpaEntity(endereco.getCidade()),
                JpaClienteMapper.toJpaEntity(
                        endereco.getCliente(),
                        JpaCarrinhoMapper
                                .toJpaEntity(endereco
                                        .getCliente()
                                        .getCarrinho())));
    }
}
