package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosEmpresa;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaDadosEmpresaMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaDadosEmpresaEntityRepository;

public class DadosEmpresaRepositoryImpl implements DadosEmpresaRepository {

    private final JpaDadosEmpresaEntityRepository repository;

    public DadosEmpresaRepositoryImpl(JpaDadosEmpresaEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public DadosEmpresa get() {
        return JpaDadosEmpresaMapper
                .toEntity(this.repository
                        .findById(1L).orElseThrow());
    }
}
