package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;

import java.util.List;

public interface EnderecoRepository {
    List<Endereco> findAllByClienteId(Long clienteId);
    Endereco findById(Long id);
    Endereco save(Endereco endereco);
    Endereco findFirstByCidade(String cidade);
    Endereco findFirstByEstado(String estado);
}
