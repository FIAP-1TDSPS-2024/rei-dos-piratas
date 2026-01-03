package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosEmpresa;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDadosEmpresaEntity;

public class JpaDadosEmpresaMapper {

    private JpaDadosEmpresaMapper() {}

    public static JpaDadosEmpresaEntity toJpaEntity(DadosEmpresa dadosEmpresa) {
        return new JpaDadosEmpresaEntity(
                dadosEmpresa.getId(),
                dadosEmpresa.getRazaoSocial(),
                dadosEmpresa.getNomeFantasia(),
                dadosEmpresa.getCnpj(),
                dadosEmpresa.getEmail(),
                dadosEmpresa.getTelefone(),
                dadosEmpresa.getDominio());
    }

    public static DadosEmpresa toEntity(JpaDadosEmpresaEntity jpaEntity) {
        return new DadosEmpresa(
                jpaEntity.getId(),
                jpaEntity.getRazaoSocial(),
                jpaEntity.getNomeFantasia(),
                jpaEntity.getCnpj(),
                jpaEntity.getEmail(),
                jpaEntity.getTelefone(),
                jpaEntity.getDominio());
    }
}
