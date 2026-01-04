package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

public interface EnderecoRepository {
    Page<Endereco> findAllByClienteId(Long clienteId, int pageNumber, int pageSize);
    Endereco findById(Long id);
    Endereco save(Endereco endereco);
    Endereco update(Endereco endereco);
    Endereco findFirstByCidade(String cidade);
    Endereco findFirstByEstado(String estado);
    Endereco getEnderecoEmpresa();
    void delete(Long id);
}
