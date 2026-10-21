package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository repository;
    private final ClienteService clienteService;
    private final CidadeRepository cidadeRepository;
    private final EstadoRepository estadoRepository;
    private final Validator validator;
    private final UsuarioAtualService usuarioAtual;

    public EnderecoServiceImpl(EnderecoRepository repository, ClienteService clienteService, CidadeRepository cidadeRepository, EstadoRepository estadoRepository, Validator validator, UsuarioAtualService usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
        this.repository = repository;
        this.clienteService = clienteService;
        this.cidadeRepository = cidadeRepository;
        this.estadoRepository = estadoRepository;
        this.validator = validator;
    }


    @Override
    public Page<Endereco> findAll(int pageNumber, int pageSize) {
        IdentidadeConta usuario = this.usuarioAtual.identidade();
        log.debug("[ENDERECO] Listando endereços do cliente ID={} - página: {}, tamanho: {}", usuario.id(), pageNumber, pageSize);
        return this.repository.findAllByClienteId(usuario.id(), pageNumber, pageSize);
    }

    @Override
    public Endereco findById(Long id) {
        IdentidadeConta usuario = this.usuarioAtual.identidade();
        log.debug("[ENDERECO] Buscando endereço ID={} para cliente ID={}", id, usuario.id());

        try {
            Endereco endereco = this.repository.findById(id);

            if (endereco.getCliente() != null) {
                if (endereco.getCliente().getId().equals(usuario.id())) {
                    return endereco;
                } else {
                    log.warn("[ENDERECO] Acesso negado: endereço ID={} pertence ao cliente ID={}, mas foi solicitado pelo cliente ID={}", id, endereco.getCliente().getId(), usuario.id());
                    throw new ResourceNotFoundException("Não foi possível encontrar um endereço com o id " + id);
                }
            } else {
                log.warn("[ENDERECO] Endereço ID={} não possui cliente associado", id);
                throw new ResourceNotFoundException("Não foi possível encontrar um endereço com o id " + id);
            }
        } catch (NoSuchElementException e) {
            log.warn("[ENDERECO] Endereço não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um endereço com o id " + id);
        }
    }

    @Transactional
    @Override
    public Endereco save(Endereco endereco) {
        IdentidadeConta usuario = this.usuarioAtual.identidade();

        log.info("[ENDERECO] Salvando endereço para cliente ID={} - CEP: {}, número: {}",
                usuario.id(), endereco.getCep(), endereco.getNumero());

        endereco.setCliente(clienteService.findById(usuario.id()));
        validar(endereco);

        Endereco enderecoDuplicado = this.repository.VerificaEnderecoDuplicado(endereco.getCep(), endereco.getNumero(), endereco.getCliente().getId());
        if (enderecoDuplicado != null) {
            log.warn("[ENDERECO] Endereço duplicado detectado: CEP={}, número={}, clienteID={}", endereco.getCep(), endereco.getNumero(), endereco.getCliente().getId());
            throw new UniqueKeyDuplicatedException("Esse CEP e número já estão registrados para esse usuário");
        }

        Estado estado = estadoRepository.findFirstByNome(endereco.getCidade().getEstado().getNome());
        if (estado == null) {
            log.debug("[ENDERECO] Estado '{}' não encontrado - criando novo registro", endereco.getCidade().getEstado().getNome());
            estado = estadoRepository.save(endereco.getCidade().getEstado());
        }

        endereco.getCidade().setEstado(estado);

        Cidade cidade = cidadeRepository.findFirstByCidadeNomeAndEstadoNome(
                endereco.getCidade().getNome(), estado.getNome());
        if (cidade == null) {
            log.debug("[ENDERECO] Cidade '{}' não encontrada no estado '{}' - criando novo registro",
                    endereco.getCidade().getNome(), estado.getNome());
            cidade = cidadeRepository.save(endereco.getCidade());
        }

        endereco.setCidade(cidade);

        Endereco enderecoSalvo = repository.save(endereco);
        log.info("[ENDERECO] Endereço ID={} salvo com sucesso para cliente ID={}", enderecoSalvo.getId(), usuario.id());
        return enderecoSalvo;
    }

    @Override
    public Endereco getEnderecoEmpresa() {
        log.debug("[ENDERECO] Buscando endereço da empresa");
        return this.repository.getEnderecoEmpresa();
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        log.info("[ENDERECO] Desativando endereço ID={}", id);
        Endereco endereco = this.findById(id);
        endereco.setEnderecoAtivo(false);
        this.repository.save(endereco);
        log.info("[ENDERECO] Endereço ID={} desativado com sucesso", id);
    }

    private void validar(Endereco endereco) {
        Set<ConstraintViolation<Endereco>> violacoes = validator.validate(endereco);
        if (!violacoes.isEmpty()) {
            log.warn("[ENDERECO] Validação falhou para endereço CEP='{}' - {} violação(ões)", endereco.getCep(), violacoes.size());
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
