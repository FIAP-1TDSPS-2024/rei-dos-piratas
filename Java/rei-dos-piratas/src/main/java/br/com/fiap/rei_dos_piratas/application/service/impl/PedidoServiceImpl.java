package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;

    private final ProdutoRepository produtoRepository;

    private final EnderecoService enderecoService;
    
    private final DadosEmpresaRepository dadosEmpresaRepository;

    private final FreteService freteService;

    private final HmacUtil hmacUtil;

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

    public PedidoServiceImpl(PedidoRepository repository, ProdutoRepository produtoRepository, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService, HmacUtil hmacUtil, ObjectMapper objectMapper) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.enderecoService = enderecoService;
        this.dadosEmpresaRepository = dadosEmpresaRepository;
        this.freteService = freteService;
        this.hmacUtil = hmacUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<Pedido> findAll(int pageNumber, int pageSize) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Verifica se usuário é um funcionário procurando uma ROLE comum a todos
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("PEDIDO_WRITE"))) {
            logger.debug("Listando todos os pedidos (funcionário ID={}) — página={}, tamanho={}", userDetails.getId(), pageNumber, pageSize);
            return this.repository.listAll(pageNumber, pageSize);
        } else {
            logger.debug("Listando pedidos do cliente ID={} — página={}, tamanho={}", userDetails.getId(), pageNumber, pageSize);
            return this.repository.listAllByClient(pageNumber, pageSize, userDetails.getId());
        }
    }

    @Override
    public Page<Pedido> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        logger.debug("Listando pedidos por status={} — página={}, tamanho={}", status, pageNumber, pageSize);
        return this.repository.listAllByStatus(pageNumber, pageSize, status);
    }

    @Override
    public Pedido findById(Long id) {
        logger.debug("Buscando pedido por ID={}", id);
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            logger.warn("Pedido não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um pedido com o id " + id);
        }
    }

    @Transactional
    @Override
    public Pedido fazerPedido(Pedido pedido) {
        logger.info("Iniciando criação de pedido para cliente ID={}, endereço de entrega CEP={}, serviço de frete ID={}",
                pedido.getCliente().getId(), pedido.getEnderecoEntrega().getCep(), pedido.getServicoEntrega());

        logger.debug("Consultando fretes disponíveis para CEP={} com {} produto(s)",
                pedido.getEnderecoEntrega().getCep(), pedido.getProdutosAdicionados().size());

        List<FreteServiceDto> fretes = this.freteService.calcularFreteProdutos(
                pedido.getEnderecoEntrega().getCep(),
                pedido.getProdutosAdicionados());

        logger.debug("API de frete retornou {} opção(ões) de entrega", fretes.size());

        Optional<FreteServiceDto> consultaFrete = fretes
                .stream()
                .findFirst()
                .filter(frete -> frete.id().equals(pedido.getServicoEntrega()));

        if (consultaFrete.isEmpty()) {
            logger.warn("Serviço de frete ID={} não encontrado entre as opções retornadas pela API", pedido.getServicoEntrega());
            throw new ResourceNotFoundException("Esse serviço de entrega não existe para esse serviço");
        }

        pedido.setValorFrete(consultaFrete.get().price());
        pedido.setValorTotal(calcularValorTotalPedido(pedido));

        logger.debug("Valor do frete definido: R${}, valor total do pedido: R${}", pedido.getValorFrete(), pedido.getValorTotal());

        this.verificaEAtualizaEstoqueparaPedido(pedido);
        Pedido pedidoCriado = this.repository.create(pedido);
        logger.info("Pedido criado com sucesso: ID={}, status={}, valor total=R${}",
                pedidoCriado.getId(), pedidoCriado.getStatus(), pedidoCriado.getValorTotal());
        return pedidoCriado;
    }

    @Transactional
    @Override
    public Pedido pagarPedido(Long id) {
        logger.info("Iniciando pagamento do pedido ID={}", id);
        Pedido pedido = this.findById(id);

        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) {
            pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
            logger.debug("Status do pedido ID={} atualizado para PREPARANDO_ENVIO, criando pedido de frete na API", id);

            PedidoFreteRequestDto pedidoFrete = this.montarPedidoFreteDto(pedido);
            logger.debug("Enviando pedido de frete para API — serviço ID={}, destino CEP={}", pedido.getServicoEntrega(), pedido.getEnderecoEntrega().getCep());

            PedidoFreteResponseDto responsePedidoFrete = this.freteService.criarPedidoFrete(pedidoFrete);
            logger.info("Pedido de frete criado com sucesso — pedidoFrete UUID={}, previsão de entrega em {} dias",
                    responsePedidoFrete.id(), responsePedidoFrete.deliveryMax());

            pedido = this.enriquecerPedidoPorFrete(pedido, responsePedidoFrete);
            Pedido pedidoAtualizado = this.repository.update(pedido);
            logger.info("Pedido ID={} pago e enriquecido com dados de frete com sucesso", id);
            return pedidoAtualizado;
        } else {
            logger.warn("Tentativa de pagamento rejeitada: pedido ID={} está no status={}, esperado={}",
                    id, pedido.getStatus(), StatusEnum.AGUARDANDO_PAGAMENTO);
            throw new WrongStatusException(
                    "O pedido deve estar no estado " + StatusEnum.AGUARDANDO_PAGAMENTO +
                    "para ser pago, mas ele está no estado " + pedido.getStatus());
        }
    }

    @Transactional
    @Override
    public String organizarPedidosParaEnvio(List<Long> pedidos) {
        logger.info("Iniciando organização de {} pedido(s) para envio: IDs={}", pedidos.size(), pedidos);

        List<Pedido> pedidosParaOrganizacao = this.repository.findByIdsAndStatus(pedidos, StatusEnum.PREPARANDO_ENVIO);

        if (pedidosParaOrganizacao.isEmpty()) {
            logger.warn("Nenhum pedido com status PREPARANDO_ENVIO encontrado na lista IDs={}", pedidos);
            throw new ResourceNotFoundException("Nenhum pedido encontrado com status PREPARANDO_ENVIO na lista passada");
        }

        logger.debug("{} pedido(s) elegível(is) para organização encontrado(s)", pedidosParaOrganizacao.size());

        List<String> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        logger.debug("Enviando {} UUID(s) de pedido de frete para API de organização: {}", pedidosFrete.size(), pedidosFrete);
        CompraFreteResponseDto response = this.freteService.organizarPedidoFrete(pedidosFrete);

        if (response.message() != null) {
            logger.warn("API de frete retornou mensagem de erro na organização: {}", response.message());
            return response.message();
        } else {
            List<Long> idsParaAtualizar = pedidosParaOrganizacao.stream().map(Pedido::getId).toList();
            this.repository.updateStatusBatch(idsParaAtualizar, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
            logger.info("Pedidos IDs={} organizados com sucesso — status atualizado para AGUARDANDO_GERACAO_ETIQUETA", idsParaAtualizar);
            return null;
        }
    }

    @Override
    public Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos) {
        logger.info("Iniciando geração de etiquetas para {} pedido(s): IDs={}", pedidos.size(), pedidos);

        List<Pedido> pedidosParaOrganizacao = this.repository.findByIdsAndStatus(pedidos, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);

        if (pedidosParaOrganizacao.isEmpty()) {
            logger.warn("Nenhum pedido com status AGUARDANDO_GERACAO_ETIQUETA encontrado na lista IDs={}", pedidos);
            throw new ResourceNotFoundException("Nenhum pedido encontrado com status AGUARDANDO_ETIQUETA na lista passada");
        }

        logger.debug("{} pedido(s) elegível(is) para geração de etiqueta encontrado(s)", pedidosParaOrganizacao.size());

        Map<String, Long> freteParaPedidoMap = pedidosParaOrganizacao
                .stream()
                .collect(HashMap::new,
                    (map, pedido) -> map.put(pedido.getPedidoFrete().toString(), pedido.getId()),
                    HashMap::putAll);

        List<String> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        logger.debug("Solicitando geração de etiquetas à API de frete para UUIDs: {}", pedidosFrete);
        GeracaoEtiquetasResponseDto response = this.freteService.gerarEtiquetasPedidoFrete(pedidosFrete);

        Map<Long, String> resultado = new HashMap<>();
        List<Long> pedidosComSucesso = new ArrayList<>();

        if (response.pedidos() != null && !response.pedidos().isEmpty()) {
            response.pedidos().forEach((freteId, statusEtiqueta) -> {
                Long pedidoId = freteParaPedidoMap.get(freteId);
                if (pedidoId != null) {
                    if (statusEtiqueta.status()) {
                        logger.debug("Etiqueta gerada com sucesso para pedido ID={} (freteId={})", pedidoId, freteId);
                        resultado.put(pedidoId, "Etiqueta gerada com sucesso");
                        pedidosComSucesso.add(pedidoId);
                    } else {
                        logger.warn("Falha ao gerar etiqueta para pedido ID={} (freteId={}): {}", pedidoId, freteId, statusEtiqueta.message());
                        resultado.put(pedidoId, "Erro ao gerar etiqueta: " + statusEtiqueta.message());
                    }
                }
            });
        } else {
            logger.warn("API de frete não retornou dados individuais de etiqueta — aplicando erro genérico a todos os pedidos");
            pedidosParaOrganizacao.forEach(pedido ->
                resultado.put(pedido.getId(), "Erro ao processar geração de etiquetas")
            );
        }

        if (!pedidosComSucesso.isEmpty()) {
            this.repository.updateStatusBatch(pedidosComSucesso, StatusEnum.AGUARDANDO_POSTAGEM);
            logger.info("{} pedido(s) com etiqueta gerada — status atualizado para AGUARDANDO_POSTAGEM: IDs={}", pedidosComSucesso.size(), pedidosComSucesso);
        }

        return resultado;
    }

    @Override
    public String imprimirEtiquetasEnvio(List<Long> pedidos) {
        logger.info("Iniciando impressão de etiquetas para {} pedido(s): IDs={}", pedidos.size(), pedidos);

        List<Pedido> pedidosParaImpressao = this.repository.findByIdsAndStatus(pedidos, StatusEnum.AGUARDANDO_POSTAGEM);

        if (pedidosParaImpressao.isEmpty()) {
            logger.warn("Nenhum pedido com status AGUARDANDO_POSTAGEM encontrado na lista IDs={}", pedidos);
            throw new ResourceNotFoundException("Nenhum pedido válido encontrado para impressão na lista passada");
        }

        List<String> pedidosFrete = pedidosParaImpressao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        logger.debug("Solicitando URL de impressão de etiquetas à API de frete para UUIDs: {}", pedidosFrete);
        String url = this.freteService.imprimirEtiquetasPedidoFrete(pedidosFrete).url();
        logger.info("URL de impressão de etiquetas obtida com sucesso para {} pedido(s)", pedidosParaImpressao.size());
        return url;
    }

    @Transactional
    @Override
    public void rastreioPedidoWebhook(String signature, String rawBody) {
        logger.debug("Webhook de rastreio recebido — validando assinatura HMAC");

        if (signature.equals(hmacUtil.generateHmac(rawBody))) {
            logger.debug("Assinatura HMAC válida — processando payload");
            try {
                RastreioWebhookDto rastreio = this.objectMapper.readValue(rawBody, RastreioWebhookDto.class);
                RastreioDataDto data = rastreio.data();

                logger.info("Processando evento webhook: event={}, pedidoFrete UUID={}, status={}",
                        rastreio.event(), data.id(), data.status());

                Pedido pedido = this.repository.findByPedidoFrete(UUID.fromString(data.id()));
                logger.debug("Pedido interno encontrado: ID={}, status atual={}", pedido.getId(), pedido.getStatus());

                // Sempre atualiza o status de entrega com o status vindo do payload
                pedido.setStatusEnvio(data.status());

                switch (rastreio.event()) {

                    case "order.created":
                        // Primeiro evento: preenche dados de rastreamento e data de criação da etiqueta
                        pedido.setTracking(data.tracking());
                        pedido.setTrackingUrl(data.trackingUrl());
                        logger.info("Etiqueta criada para pedido ID={} — protocolo={}, tracking={}",
                                pedido.getId(), data.protocol(), data.tracking());
                        break;

                    case "order.released":
                        // Etiqueta paga — status interno já gerenciado pelo fluxo de organizarPedidosParaEnvio
                        logger.debug("Etiqueta paga (order.released) para pedido ID={} — nenhuma ação interna necessária", pedido.getId());
                        break;

                    case "order.generated":
                        // Etiqueta gerada — status interno já gerenciado pelo fluxo de gerarEtiquetasParaEnvio
                        logger.debug("Etiqueta gerada (order.generated) para pedido ID={} — nenhuma ação interna necessária", pedido.getId());
                        break;

                    case "order.received":
                        // Encomenda recebida em ponto de distribuição Pegaki — sem mudança de status interno
                        logger.info("Encomenda recebida em ponto de distribuição para pedido ID={}", pedido.getId());
                        break;

                    case "order.posted":
                        // Encomenda postada → transita internamente para EM_TRANSITO
                        pedido.setStatus(StatusEnum.EM_TRANSITO);
                        logger.info("Encomenda postada — pedido ID={} atualizado para EM_TRANSITO", pedido.getId());
                        break;

                    case "order.delivered":
                        // Encomenda entregue → status ENTREGUE + data de entrega vinda da API
                        pedido.setStatus(StatusEnum.ENTREGUE);
                        if (data.deliveredAt() != null) {
                            pedido.setDataEntrega(data.deliveredAt().toLocalDate());
                        }
                        logger.info("Encomenda entregue — pedido ID={} atualizado para ENTREGUE em {}", pedido.getId(), pedido.getDataEntrega());
                        break;

                    case "order.undelivered":
                        // Tentativa de entrega falhou — apenas statusEnvio atualizado (já feito acima)
                        logger.warn("Tentativa de entrega falhou (order.undelivered) para pedido ID={} — statusEnvio atualizado para '{}'",
                                pedido.getId(), data.status());
                        break;

                    case "order.paused":
                        // Entrega interrompida, aguardando ação do destinatário — apenas statusEnvio
                        logger.warn("Entrega pausada (order.paused) para pedido ID={} — ação do destinatário necessária", pedido.getId());
                        break;

                    case "order.suspended":
                        // Encomenda suspensa — apenas statusEnvio
                        logger.warn("Encomenda suspensa (order.suspended) para pedido ID={}", pedido.getId());
                        break;

                    case "order.canceled":
                        // Etiqueta cancelada (falha interna de nota, não cancelamento do pedido)
                        // Retrocede para PREPARANDO_ENVIO para permitir geração de nova etiqueta
                        pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
                        pedido.setStatusEnvio(null);
                        pedido.setPedidoFrete(null);
                        pedido.setProtocoloEnvio(null);
                        pedido.setTracking(null);
                        pedido.setTrackingUrl(null);
                        logger.warn("Etiqueta cancelada (order.canceled) para pedido ID={} — campos de envio resetados, status revertido para PREPARANDO_ENVIO para nova emissão",
                                pedido.getId());
                        break;

                    case "order.expired":
                        // Etiqueta expirada sem postagem (falha interna de nota, não cancelamento do pedido)
                        // Mesmo tratamento do canceled: reabre o pedido para nova emissão de etiqueta
                        pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
                        pedido.setStatusEnvio(null);
                        pedido.setPedidoFrete(null);
                        pedido.setProtocoloEnvio(null);
                        pedido.setTracking(null);
                        pedido.setTrackingUrl(null);
                        logger.warn("Etiqueta expirada (order.expired) para pedido ID={} — campos de envio resetados, status revertido para PREPARANDO_ENVIO para nova emissão",
                                pedido.getId());
                        break;

                    default:
                        logger.warn("Evento de rastreio desconhecido recebido: event={}, pedidoFrete UUID={}", rastreio.event(), data.id());
                        break;
                }

                this.repository.update(pedido);
                logger.debug("Pedido ID={} persistido após processamento do evento '{}'", pedido.getId(), rastreio.event());

            } catch (Exception e) {
                logger.error("Erro ao processar webhook de rastreio: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Webhook de rastreio rejeitado — assinatura HMAC inválida");
        }
    }


    @Transactional
    @Override
    public Pedido cancelarPedido(Long id) {
        logger.info("Iniciando cancelamento do pedido ID={}", id);
        Pedido pedido = this.findById(id);

        if (pedido.getStatus() == StatusEnum.CANCELADO) {
            logger.warn("Pedido ID={} já está cancelado", id);
            throw new WrongStatusException("O pedido já está cancelado.");
        } else if (pedido.getStatus() == StatusEnum.ENTREGUE) {
            logger.warn("Pedido ID={} já foi entregue, cancelamento não permitido", id);
            throw new WrongStatusException("O pedido já foi entregue");
        } else if ((pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) || pedido.getStatus() == StatusEnum.PREPARANDO_ENVIO) {
            logger.debug("Revertendo estoque de {} produto(s) para cancelamento do pedido ID={}", pedido.getProdutosAdicionados().size(), id);
            pedido.getProdutosAdicionados()
                    .forEach(produto -> {
                        produto.getProduto().setEstoque(produto.getProduto().getEstoque() + produto.getQuantidade());
                        logger.debug("Estoque do produto ID={} restaurado em {} unidade(s)", produto.getProduto().getId(), produto.getQuantidade());
                        this.produtoRepository.update(produto.getProduto());
                    });
            pedido.setStatus(StatusEnum.CANCELADO);
            Pedido pedidoCancelado = this.repository.update(pedido);
            logger.info("Pedido ID={} cancelado com sucesso", id);
            return pedidoCancelado;
        } else {
            logger.warn("Pedido ID={} não pode ser cancelado diretamente no status={} — devolução necessária", id, pedido.getStatus());
            throw new WrongStatusException("O pedido já foi tramitado. Para fazer o cancelamento, deve ser solicitada uma devolução.");
        }
    }

    private void verificaEAtualizaEstoqueparaPedido(Pedido pedido) {
        logger.debug("Verificando estoque para {} produto(s) do pedido", pedido.getProdutosAdicionados().size());
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    if (produto.getProduto().getEstoque() < produto.getQuantidade()) {
                        logger.warn("Estoque insuficiente: produto ID={} '{}' — disponível={}, solicitado={}",
                                produto.getProduto().getId(), produto.getProduto().getNome(),
                                produto.getProduto().getEstoque(), produto.getQuantidade());
                        throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                                produto.getProduto().getNome() + " de ID " +
                                produto.getProduto().getId() + " tem apenas " +
                                produto.getProduto().getEstoque() + " unidades em estoque.");
                    }
                });

        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    produto.getProduto().setEstoque(produto.getProduto().getEstoque() - produto.getQuantidade());
                    logger.debug("Estoque do produto ID={} decrementado em {} unidade(s) — novo estoque={}",
                            produto.getProduto().getId(), produto.getQuantidade(), produto.getProduto().getEstoque());
                    this.produtoRepository.update(produto.getProduto());
                });
    }

    private Pedido enriquecerPedidoPorFrete(Pedido pedido, PedidoFreteResponseDto pedidoFreteResponseDto) {
        pedido.setPedidoFrete(pedidoFreteResponseDto.id());
        pedido.setDataPrevisaoEntrega(LocalDate.now().plusDays(pedidoFreteResponseDto.deliveryMax()));
        pedido.setProtocoloEnvio(pedidoFreteResponseDto.protocol());
        logger.debug("Pedido ID={} enriquecido: pedidoFrete UUID={}, previsão de entrega={}",
                pedido.getId(), pedidoFreteResponseDto.id(), pedido.getDataPrevisaoEntrega());
        return this.repository.update(pedido);
    }

    private PedidoFreteRequestDto montarPedidoFreteDto(Pedido pedido) {
        logger.debug("Montando DTO de pedido de frete para pedido ID={}", pedido.getId());
        //Definir empresa como remetente
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();
        DadosEmpresa dadosEmpresa = this.dadosEmpresaRepository.get();
        DestinoRemetenteDto remetente = this.definirRemetenteEmpresa(enderecoEmpresa, dadosEmpresa);

        //Definir cliente como destino
        Endereco enderecoDestino = this.enderecoService.findById(pedido.getEnderecoEntrega().getId());
        Cliente cliente = pedido.getCliente();
        DestinoRemetenteDto destino = this.definirDestinoEntrega(enderecoDestino, cliente);

        //Definir lista de produtos e quantidade
        List<ProdutoPedidoFreteDto> produtos = this.definirProdutosFrete(pedido);

        //Definir dimensões do(s) volume(s) para entrega
        List<VolumeFreteDto> volumes = new ArrayList<>();
        volumes.add(definirDimensoesVolumes(pedido));

        //Definir opções de entrega:
        FreteOptionsDto opcoes = new FreteOptionsDto(
                "Rei dos Piratas",
                "Entrega do pedido " + pedido.getId(),
                pedido.getValorTotal(),
                false,
                false,
                false,
                false,
                null);

        logger.debug("DTO de pedido de frete montado com sucesso para pedido ID={}", pedido.getId());
        return new PedidoFreteRequestDto(
                pedido.getServicoEntrega(),
                remetente,
                destino,
                produtos,
                volumes,
                opcoes);
    }

    private DestinoRemetenteDto definirRemetenteEmpresa(Endereco enderecoEmpresa, DadosEmpresa dadosEmpresa) {
        return new DestinoRemetenteDto(
                dadosEmpresa.getNomeFantasia(),
                dadosEmpresa.getEmail(),
                dadosEmpresa.getTelefone(),
                null,
                dadosEmpresa.getCnpj(),
                dadosEmpresa.getStateRegister(),
                dadosEmpresa.getEconomicActivityCode(),
                enderecoEmpresa.getLogradouro(),
                null,
                String.valueOf(enderecoEmpresa.getNumero()),
                enderecoEmpresa.getBairro(),
                enderecoEmpresa.getCidade().getNome(),
                enderecoEmpresa.getCep(),
                enderecoEmpresa.getCidade().getEstado().getSigla());
    }

    private DestinoRemetenteDto definirDestinoEntrega(Endereco enderecoDestino, Cliente cliente) {
        return new DestinoRemetenteDto(
                cliente.getNomeCompleto(),
                cliente.getEmail(),
                cliente.getCelular(),
                cliente.getCpf(),
                null,
                null,
                null,
                enderecoDestino.getLogradouro(),
                null,
                String.valueOf(enderecoDestino.getNumero()),
                enderecoDestino.getBairro(),
                enderecoDestino.getCidade().getNome(),
                enderecoDestino.getCep(),
                enderecoDestino.getCidade().getEstado().getSigla());
    }

    private List<ProdutoPedidoFreteDto> definirProdutosFrete(Pedido pedido) {
        return pedido.getProdutosAdicionados()
                .stream()
                .map(produto -> new ProdutoPedidoFreteDto(
                        produto.getProduto().getNome(),
                        String.valueOf(produto.getQuantidade()),
                        String.valueOf(produto.getProduto().getPreco())))
                .toList();
    }

    private VolumeFreteDto definirDimensoesVolumes(Pedido pedido) {
        int quantidadeTotal = pedido.getProdutosAdicionados().stream()
                .mapToInt(ItemProdutoPedido::getQuantidade)
                .sum();

        BigDecimal pesoTotal = pedido.getProdutosAdicionados().stream()
                .map(produto -> produto.getProduto().getPeso().multiply(BigDecimal.valueOf(produto.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (quantidadeTotal < 6) {
            int altura = pedido.getProdutosAdicionados().stream()
                    .mapToInt(produto -> produto.getProduto().getProfundidade().intValue() * produto.getQuantidade())
                    .sum() + 3;

            return new VolumeFreteDto(altura, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 11) {
            return new VolumeFreteDto(28, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 20) {
            return new VolumeFreteDto(28, 32, 26, pesoTotal);
        }

        return new VolumeFreteDto(28, 32, 52, pesoTotal);
    }

    private BigDecimal calcularValorTotalPedido(Pedido pedido){

        if (pedido.getValorFrete() == null){
            throw new RegraDeNegocioException("Consulte o valor do frete do pedido antes do cálculo de valor total");
        }

        //O valor total do pedido é calculado somando os valores dos produtos, multplicando por sua quantidade e por fim adicionando o valor do frete
        return pedido.getProdutosAdicionados()
                .stream()
                .map(item -> item
                        .getProduto()
                        .getPreco()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(pedido.getValorFrete());
    }
}
