package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

public interface EnderecoRepository {
    Page<Endereco> findAllByClienteId(Long clienteId, int pageNumber, int pageSize);
    Endereco findById(Long id);
    Endereco save(Endereco endereco);
    Endereco VerificaEnderecoDuplicado(String cep, int numero, Long clienteId);
    Endereco getEnderecoEmpresa();
}
