package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

public interface EnderecoService {
    Page<Endereco> findAll(int pageNumber, int pageSize);
    Endereco findById(Long id);
    Endereco save(Endereco endereco);
    Endereco getEnderecoEmpresa();
    void deactivate(Long id);;
}
