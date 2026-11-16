package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoImagemEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.ImagemProduto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ArmazenamentoArquivoException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import java.util.Objects;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.UrlUploadGerada;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;
    private final Validator validator;
    private final GatewayArquivo gatewayArquivo;

    public ProdutoServiceImpl(ProdutoRepository repository, Validator validator, GatewayArquivo gatewayArquivo) {
        this.repository = repository;
        this.validator = validator;
        this.gatewayArquivo = gatewayArquivo;
    }

    @Override
    public Produto findById(Long id) {
        log.debug("[PRODUTO] Buscando produto por ID={}", id);
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            log.warn("[PRODUTO] Produto não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + id);
        }
    }

    @Override
    public Page<Produto> findAll(int pageNumber, int pageSize) {
        log.debug("[PRODUTO] Listando todos os produtos - página: {}, tamanho: {}", pageNumber, pageSize);
        return this.repository.listAtivos(pageNumber, pageSize);
    }


    @Transactional
    @Override
    public Produto create(Produto produto) {
        log.info("[PRODUTO] Criando novo produto - nome='{}', preço={}, estoque={}",
                produto.getNome(), produto.getPreco(), produto.getEstoque());
        validar(produto);
        exigirImagemEnviada(produto.getEnderecoImagem());
        Produto produtoCriado = this.repository.create(produto);
        log.info("[PRODUTO] Produto criado com sucesso - ID={}, nome='{}'", produtoCriado.getId(), produtoCriado.getNome());
        return produtoCriado;
    }

    @Transactional
    @Override
    public Produto update(Produto produto) {
        log.info("[PRODUTO] Atualizando produto ID={}", produto.getId());
        validar(produto);
        Produto atual = buscarAtual(produto.getId());
        if (atual != null && !atual.isAtivo()) {
            // Editar não reativa: o formulário e a API não trazem o campo "ativo"
            produto.desativar();
        }
        if (atual == null || !Objects.equals(atual.getEnderecoImagem(), produto.getEnderecoImagem())) {
            exigirImagemEnviada(produto.getEnderecoImagem());
        }
        Produto produtoAtualizado = this.repository.update(produto);
        if (produtoAtualizado == null) {
            log.error("[PRODUTO] Falha ao atualizar produto ID={} - registro não encontrado", produto.getId());
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + produto.getId() + ". Crie um novo produto.");
        }
        log.info("[PRODUTO] Produto ID={} atualizado com sucesso - nome='{}'", produtoAtualizado.getId(), produtoAtualizado.getNome());
        return produtoAtualizado;
    }

    @Transactional
    @Override
    public void reativar(Long id) {
        Produto produto = findById(id);
        produto.reativar();
        this.repository.update(produto);
        log.info("[PRODUTO] Produto ID={} reativado", id);
    }

    @Override
    public Page<Produto> listarDesativados(int pageNumber, int pageSize) {
        log.debug("[PRODUTO] Listando produtos desativados - página: {}, tamanho: {}", pageNumber, pageSize);
        return this.repository.listDesativados(pageNumber, pageSize);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        // Não apaga: carrinhos, pedidos e devoluções referenciam o produto
        Produto produto = findById(id);
        produto.desativar();
        this.repository.update(produto);
        log.info("[PRODUTO] Produto ID={} desativado", id);
    }

    @Override
    public UrlUploadGerada gerarUrlPreAssinadaUpload(String contentType) {
        TipoImagemEnum tipo = TipoImagemEnum.doContentType(contentType);
        String key = ImagemProduto.novaChave(tipo);
        log.info("[PRODUTO] Gerando URL de upload de imagem - tipo={}", tipo);
        String url = gatewayArquivo.gerarUrlPreAssinadaUpload(key, tipo.contentType(), ImagemProduto.VALIDADE_UPLOAD).toString();
        return new UrlUploadGerada(key, url);
    }

    @Override
    public String gerarUrlPreAssinadaVisualizacao(String key) {
        // Só imagens de produto: o endpoint é público e não pode abrir outros objetos do bucket
        ImagemProduto.exigirChave(key);
        return gatewayArquivo.gerarUrlPreAssinadaVisualizacao(key, ImagemProduto.VALIDADE_VISUALIZACAO).toString();
    }

    @Override
    public String urlDaImagem(String enderecoImagem) {
        if (ImagemProduto.ehUrlLegada(enderecoImagem)) {
            return enderecoImagem;
        }
        if (!ImagemProduto.ehChave(enderecoImagem)) {
            return null;
        }
        try {
            return gatewayArquivo.gerarUrlPreAssinadaVisualizacao(enderecoImagem, ImagemProduto.VALIDADE_VISUALIZACAO).toString();
        } catch (ArmazenamentoArquivoException e) {
            // Sem a imagem a vitrine continua funcionando; o detalhe já foi logado no adaptador
            log.warn("[PRODUTO] Imagem sem URL de visualização: armazenamento indisponível");
            return null;
        }
    }

    private void exigirImagemEnviada(String enderecoImagem) {
        ImagemProduto.exigirChave(enderecoImagem);
        if (!gatewayArquivo.existe(enderecoImagem)) {
            log.warn("[PRODUTO] Imagem informada ainda não foi enviada ao armazenamento");
            throw new RegraDeNegocioException("A imagem do produto ainda não foi enviada. Selecione a imagem novamente.");
        }
    }

    private Produto buscarAtual(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + id + ". Crie um novo produto.");
        }
    }

    /**
     * Valida o objeto Produto contra as anotações da entidade de domínio.
     * Lança ValidacaoException com o mapa campo → mensagem caso haja violações.
     * Centraliza a validação para qualquer entrada (API REST ou formulário web).
     */
    private void validar(Produto produto) {
        Set<ConstraintViolation<Produto>> violacoes = validator.validate(produto);
        if (!violacoes.isEmpty()) {
            log.warn("[PRODUTO] Validação falhou para produto nome='{}' - {} violação(ões)", produto.getNome(), violacoes.size());
            Map<String, String> erros = violacoes.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (m1, m2) -> m1   // em caso de duplicata, mantém a primeira
                    ));
            throw new ValidacaoException(erros);
        }
    }
}
