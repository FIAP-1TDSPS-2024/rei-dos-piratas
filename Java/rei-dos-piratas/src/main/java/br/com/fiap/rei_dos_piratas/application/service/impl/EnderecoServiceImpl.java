package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository repository;
    private final ClienteService clienteService;
    private final CidadeRepository cidadeRepository;
    private final EstadoRepository estadoRepository;
    private final Validator validator;

    public EnderecoServiceImpl(EnderecoRepository repository, ClienteService clienteService, CidadeRepository cidadeRepository, EstadoRepository estadoRepository, Validator validator) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.cidadeRepository = cidadeRepository;
        this.estadoRepository = estadoRepository;
        this.validator = validator;
    }


    @Override
    public Page<Endereco> findAll(int pageNumber, int pageSize) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return this.repository
                .findAllByClienteId(userDetails.getId(), pageNumber, pageSize);
    }

    @Override
    public Endereco findById(Long id) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            Endereco endereco = this.repository.findById(id);

            if (endereco.getCliente() != null) {
                if (endereco.getCliente().getId().equals(userDetails.getId())){
                    return endereco;
                }
                else {
                    throw new ResourceNotFoundException("Não foi possível encontrar um endereço com o id " + id);
                }
            }
            else {
                throw new ResourceNotFoundException("Não foi possível encontrar um endereço com o id " + id);
            }
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um endereço com o id " + id);
        }
    }

    @Transactional
    @Override
    public Endereco save(Endereco endereco) {
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

        endereco.setCliente(clienteService.findById(userDetails.getId()));

        validar(endereco);

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

    @Override
    public Endereco getEnderecoEmpresa() {
        return this.repository.getEnderecoEmpresa();
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        Endereco endereco = this.findById(id);
        endereco.setEnderecoAtivo(false);
        this.repository.save(endereco);
    }

    private void validar(Endereco endereco) {
        Set<ConstraintViolation<Endereco>> violacoes = validator.validate(endereco);
        if (!violacoes.isEmpty()) {
            Map<String, String> erros = violacoes.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (m1, m2) -> m1
                    ));
            throw new ValidacaoException(erros);
        }
    }
}
