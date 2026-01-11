package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository repository;
    private final ClienteService clienteService;
    private final CidadeRepository cidadeRepository;
    private final EstadoRepository estadoRepository;

    public EnderecoServiceImpl(EnderecoRepository repository, ClienteService clienteService, CidadeRepository cidadeRepository, EstadoRepository estadoRepository) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.cidadeRepository = cidadeRepository;
        this.estadoRepository = estadoRepository;
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

    @Transactional
    @Override
    public Endereco save(Endereco endereco) {
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

        endereco.setCliente(clienteService.findById(userDetails.getId()));

        Endereco enderecoDuplicado = this.repository.VerificaEnderecoDuplicado(endereco.getCep(), endereco.getNumero(), endereco.getCliente().getId());

        if (enderecoDuplicado != null) {
            throw new UniqueKeyDuplicatedException("Esse CEP e número já estão registrados para esse usuário");
        }

        Estado estado =
                estadoRepository.findFirstByNome(
                        endereco.getCidade().getEstado().getNome()
                );

        if (estado == null) {
            estado = estadoRepository.save(
                    endereco.getCidade().getEstado()
            );
        }

        // garante que a cidade aponte para um estado gerenciado
        endereco.getCidade().setEstado(estado);

        Cidade cidade =
                cidadeRepository.findFirstByCidadeNomeAndEstadoNome(
                        endereco.getCidade().getNome(),
                        estado.getNome()
                );

        if (cidade == null) {
            cidade = cidadeRepository.save(endereco.getCidade());
        }

        endereco.setCidade(cidade);

        return repository.save(endereco);
    }

    @Transactional
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
