package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository repository;

    public EnderecoServiceImpl(EnderecoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Endereco> findAll(int pageNumber, int pageSize) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return this.repository
                .findAllByClienteId(userDetails.getId(), pageNumber, pageSize);
    }

    @Override
    public Endereco findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public Endereco save(Endereco endereco) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        endereco.setClienteId(userDetails.getId());

        //Verificar se a cidade já existe na base de dados para evitar duplicidade
        Endereco enderecoCidadeDuplicada = this.repository.findFirstByCidade(endereco.getCidadeNome());

        if (enderecoCidadeDuplicada != null) {
            endereco.setCidadeId(enderecoCidadeDuplicada.getCidadeId());
            endereco.setEstadoId(enderecoCidadeDuplicada.getEstadoId());
        }
        else {
            //Verificar se o estado já existe na base de dados para evitar duplicidade
            Endereco enderecoEstadoDuplicado = this.repository.findFirstByEstado(endereco.getEstadoNome());

            if (enderecoEstadoDuplicado != null) {
                endereco.setEstadoId(enderecoEstadoDuplicado.getEstadoId());
            }
        }

        return this.repository.save(endereco);
    }

    @Override
    public Endereco update(Endereco endereco) {
        Endereco enderecoAtualizado = this.repository.update(endereco);

        if (enderecoAtualizado == null){
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + endereco.getId() + ". Crie um novo produto.");
        }

        return enderecoAtualizado;
    }

    @Override
    public Endereco getEndercoEmpresa() {
        return this.repository.getEnderecoEmpresa();
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }
}
