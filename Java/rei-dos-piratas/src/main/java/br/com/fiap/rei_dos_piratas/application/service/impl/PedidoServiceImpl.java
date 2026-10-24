package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.Enum.AsaasPaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class PedidoServiceImpl implements PedidoService {

    /** Datas de negócio (ex.: entrega) no fuso da loja; o Jackson entrega OffsetDateTime em UTC. */
    private static final ZoneId FUSO_LOJA = ZoneId.of("America/Sao_Paulo");

    private final PedidoRepository repository;

    private final ProdutoRepository produtoRepository;

    private final EnderecoService enderecoService;
    
    private final DadosEmpresaRepository dadosEmpresaRepository;

    private final FreteService freteService;

    private final ClienteService clienteService;

    private final CobrancaAppClient apiCobranca;
    private final UsuarioAtualService usuarioAtual;

    private final CartaoService cartaoService;
    private final RegraParcelamento regraParcelamento;

    public PedidoServiceImpl(PedidoRepository repository, ProdutoRepository produtoRepository, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService, ClienteService clienteService, CobrancaAppClient apiCobranca, UsuarioAtualService usuarioAtual,
                             CartaoService cartaoService, RegraParcelamento regraParcelamento) {
        this.cartaoService = cartaoService;
        this.regraParcelamento = regraParcelamento;
        this.usuarioAtual = usuarioAtual;
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.enderecoService = enderecoService;
        this.dadosEmpresaRepository = dadosEmpresaRepository;
        this.freteService = freteService;
        this.clienteService = clienteService;
        this.apiCobranca = apiCobranca;
    }

    @Override
    public Page<Pedido> findAll(int pageNumber, int pageSize) {

        IdentidadeConta usuario = this.usuarioAtual.identidade();

        //Verifica se usuário é um funcionário procurando uma permissão comum a todos
        if (this.usuarioAtual.possuiPermissao("PEDIDO_WRITE")) {
            log.debug("Listando todos os pedidos (funcionário ID={}) — página={}, tamanho={}", usuario.id(), pageNumber, pageSize);
            return this.repository.listAll(pageNumber, pageSize);
        } else {
            log.debug("Listando pedidos do cliente ID={} — página={}, tamanho={}", usuario.id(), pageNumber, pageSize);
            return this.repository.listAllByClient(pageNumber, pageSize, usuario.id());
        }
    }

    @Override
    public Page<Pedido> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        log.debug("Listando pedidos por status={} — página={}, tamanho={}", status, pageNumber, pageSize);
        return this.repository.listAllByStatus(pageNumber, pageSize, status);
    }

    @Override
    public Pedido findById(Long id) {
        log.debug("Buscando pedido por ID={}", id);
        Pedido pedido;
        try {
            pedido = this.repository.findById(id);
        } catch (NoSuchElementException e) {
            log.warn("Pedido não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um pedido com o id " + id);
        }

        this.validarAcessoAoPedido(pedido);
        return pedido;
    }

    private void validarAcessoAoPedido(Pedido pedido) {
        // Sem usuário autenticado: chamadas internas (webhooks, processamento de eventos)
        Optional<IdentidadeConta> identidade = this.usuarioAtual.identidadeSeAutenticado();
        if (identidade.isEmpty()) {
            return;
        }
        IdentidadeConta usuario = identidade.get();

        boolean funcionario = usuario.tipo() == TipoConta.FUNCIONARIO;
        boolean dono = pedido.getCliente() != null && usuario.id().equals(pedido.getCliente().getId());

        if (!funcionario && !dono) {
            log.warn("Acesso negado: pedido ID={} solicitado pelo usuário ID={}", pedido.getId(), usuario.id());
            throw new ResourceNotFoundException("Não foi possível encontrar um pedido com o id " + pedido.getId());
        }
    }

    @Transactional
    @Override
    public Pedido fazerPedido(Pedido pedido) {
        Pedido pedidoCriado = this.criarPedido(pedido);
        return this.criarCobrancaPedido(pedidoCriado);
    }

    /**
     * Cartão: grava o pedido, tokeniza (o cliente Asaas já existe nesse ponto) e cobra.
     * A recusa do Asaas grava pedido CANCELADO + pagamento RECUSADO e só então propaga o erro,
     * por isso a transação não é desfeita para {@link PagamentoRecusadoException}.
     */
    @Transactional(noRollbackFor = PagamentoRecusadoException.class)
    @Override
    public Pedido fazerPedido(Pedido pedido, PagamentoCartao pagamentoCartao) {
        if (!TipoPagamentoEnum.CREDIT_CARD.equals(pedido.getTipoPagamento())) {
            return this.fazerPedido(pedido);
        }

        if (pagamentoCartao == null) {
            log.warn("Pedido com pagamento por cartão sem dados do cartão — cliente ID={}", pedido.getCliente().getId());
            throw new RegraDeNegocioException("Informe o cartão de crédito para pagamento com cartão");
        }

        pedido.getPagamento().setParcelas(pagamentoCartao.parcelas());
        Pedido pedidoCriado = this.criarPedido(pedido);

        try {
            CartaoCliente cartao = this.cartaoService.obterOuTokenizar(
                    pedidoCriado.getCliente(), pedidoCriado.getEnderecoEntrega(), pagamentoCartao);
            pedidoCriado.getPagamento().setCartao(cartao);
            return this.criarCobrancaPedido(pedidoCriado);
        } catch (PagamentoRecusadoException e) {
            this.registrarRecusa(pedidoCriado, e.getMessage());
            throw e;
        }
    }

    private void registrarRecusa(Pedido pedido, String motivo) {
        log.warn("Pagamento com cartão recusado para pedido ID={} — cancelando pedido e devolvendo estoque", pedido.getId());
        this.devolverEstoque(pedido);
        pedido.setStatus(StatusEnum.CANCELADO);
        pedido.getPagamento().setStatus(StatusPagamentoEnum.RECUSADO);
        pedido.getPagamento().setMotivoRecusa(motivo);
        this.repository.update(pedido);
    }

    private Pedido criarPedido(Pedido pedido) {
        log.info("Iniciando criação de pedido para cliente ID={}, endereço de entrega CEP={}, serviço de frete ID={}",
                pedido.getCliente().getId(), pedido.getEnderecoEntrega().getCep(), pedido.getServicoEntrega());

        log.debug("Consultando fretes disponíveis para CEP={} com {} produto(s)",
                pedido.getEnderecoEntrega().getCep(), pedido.getProdutosAdicionados().size());

        List<FreteServiceDto> fretes = this.freteService.calcularFreteProdutos(
                pedido.getEnderecoEntrega().getCep(),
                pedido.getProdutosAdicionados());

        log.debug("API de frete retornou {} opção(ões) de entrega", fretes.size());

        Optional<FreteServiceDto> consultaFrete = fretes
                .stream()
                .findFirst()
                .filter(frete -> frete.id().equals(pedido.getServicoEntrega()));

        if (consultaFrete.isEmpty()) {
            log.warn("Serviço de frete ID={} não encontrado entre as opções retornadas pela API", pedido.getServicoEntrega());
            throw new ResourceNotFoundException("Esse serviço de entrega não existe para esse serviço");
        }

        pedido.setValorFrete(consultaFrete.get().price());
        pedido.setValorTotal(calcularValorTotalPedido(pedido));

        if (pedido.getPagamento() != null) {
            pedido.getPagamento().setValor(pedido.getValorTotal());
        }

        log.debug("Valor do frete definido: R${}, valor total do pedido: R${}", pedido.getValorFrete(), pedido.getValorTotal());

        if (TipoPagamentoEnum.CREDIT_CARD.equals(pedido.getTipoPagamento())) {
            this.regraParcelamento.validar(pedido.getValorTotal(), pedido.getPagamento().getParcelas());
        }

        this.verificaEAtualizaEstoqueparaPedido(pedido);

        Cliente cliente = this.clienteService.obterOuCriarClienteCobranca(pedido.getCliente(), pedido.getEnderecoEntrega());
        pedido.setCliente(cliente);

        Pedido pedidoCriado = this.repository.create(pedido);

        log.info("Pedido criado com sucesso: ID={}, status={}, valor total=R${}",
                pedidoCriado.getId(), pedidoCriado.getStatus(), pedidoCriado.getValorTotal());

        return pedidoCriado;
    }

    private Pedido criarCobrancaPedido(Pedido pedido) {

        PagamentoCobrancaRequestDto request = definirPagamentoCobrancaRequest(pedido);
        PagamentoCobrancaResponseDto response = this.apiCobranca.criarNovaCobranca(request);

        Pagamento pagamento = pedido.getPagamento();
        pagamento.setIdCobranca(response.id());
        pagamento.setValorLiquido(response.netValue());

        // Cartão: o Asaas responde na hora; recusa vem como erro (PagamentoRecusadoException).
        // Em análise de risco a cobrança fica pendente até o webhook.
        if (TipoPagamentoEnum.CREDIT_CARD.equals(pagamento.getTipo()) && cobrancaAprovada(response)) {
            pagamento.setStatus(StatusPagamentoEnum.CONFIRMADO);
            pagamento.setDataConfirmacao(LocalDateTime.now());
        }

        log.info("Cobrança criada com sucesso: ID={}, valor líquido=R${}, status do pagamento={}",
                response.id(), response.netValue(), pagamento.getStatus());

        return this.repository.update(pedido);
    }

    private boolean cobrancaAprovada(PagamentoCobrancaResponseDto response) {
        return response.status() == null
                || response.status() == AsaasPaymentStatus.CONFIRMED
                || response.status() == AsaasPaymentStatus.RECEIVED;
    }

    private PagamentoCobrancaRequestDto definirPagamentoCobrancaRequest(Pedido pedido) {

        switch (pedido.getTipoPagamento()){

            case TipoPagamentoEnum.PIX:
                return new PagamentoCobrancaRequestDto(
                        pedido.getCliente().getIdCobranca(),
                        pedido.getTipoPagamento(),
                        pedido.getValorTotal(),
                        LocalDate.now().plusDays(1),
                        "cobrança do pedido " + pedido.getId(),
                        pedido.getId().toString(),
                        null,
                        null,
                        null);

            case TipoPagamentoEnum.CREDIT_CARD:

                CartaoCliente cartaoCliente = pedido.getPagamento().getCartao();
                // Asaas: à vista usa "value"; parcelado usa "installmentCount" + "totalValue"
                boolean parcelado = pedido.getPagamento().getParcelas() != null && pedido.getPagamento().getParcelas() > 1;

                return new PagamentoCobrancaRequestDto(
                        pedido.getCliente().getIdCobranca(),
                        pedido.getTipoPagamento(),
                        parcelado ? null : pedido.getValorTotal(),
                        LocalDate.now().plusDays(1),
                        "cobrança do pedido " + pedido.getId(),
                        pedido.getId().toString(),
                        parcelado ? pedido.getPagamento().getParcelas() : null,
                        cartaoCliente.getToken(),
                        parcelado ? pedido.getValorTotal() : null);

            case TipoPagamentoEnum.BOLETO:
                throw new UnsupportedOperationException("Pagamento por boleto não é suportado no momento");

            default:
                throw new RegraDeNegocioException("Tipo de pagamento não suportado: " + pedido.getTipoPagamento());
        }
    }

    @Transactional
    @Override
    public void pagarPedido(Long id) {
        log.info("Iniciando pagamento do pedido ID={}", id);
        Pedido pedido = this.findById(id);

        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) {
            pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
            log.debug("Status do pedido ID={} atualizado para PREPARANDO_ENVIO, criando pedido de frete na API", id);

            PedidoFreteRequestDto pedidoFrete = this.montarPedidoFreteDto(pedido);
            log.debug("Enviando pedido de frete para API — serviço ID={}, destino CEP={}", pedido.getServicoEntrega(), pedido.getEnderecoEntrega().getCep());

            PedidoFreteResponseDto responsePedidoFrete = this.freteService.criarPedidoFrete(pedidoFrete);
            log.info("Pedido de frete criado com sucesso — pedidoFrete UUID={}, previsão de entrega em {} dias",
                    responsePedidoFrete.id(), responsePedidoFrete.deliveryMax());

            pedido = this.enriquecerPedidoPorFrete(pedido, responsePedidoFrete);
            Pedido pedidoAtualizado = this.repository.update(pedido);
            log.info("Pedido ID={} pago e enriquecido com dados de frete com sucesso", id);
        } else {
            log.warn("Tentativa de pagamento rejeitada: pedido ID={} está no status={}, esperado={}",
                    id, pedido.getStatus(), StatusEnum.AGUARDANDO_PAGAMENTO);
        }
    }

    @Transactional
    @Override
    public String organizarPedidosParaEnvio(List<Long> pedidos) {
        log.info("Iniciando organização de {} pedido(s) para envio: IDs={}", pedidos.size(), pedidos);

        List<Pedido> pedidosParaOrganizacao = this.repository.findByIdsAndStatus(pedidos, StatusEnum.PREPARANDO_ENVIO);

        if (pedidosParaOrganizacao.isEmpty()) {
            log.warn("Nenhum pedido com status PREPARANDO_ENVIO encontrado na lista IDs={}", pedidos);
            throw new ResourceNotFoundException("Nenhum pedido encontrado com status PREPARANDO_ENVIO na lista passada");
        }

        log.debug("{} pedido(s) elegível(is) para organização encontrado(s)", pedidosParaOrganizacao.size());

        List<String> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        log.debug("Enviando {} UUID(s) de pedido de frete para API de organização: {}", pedidosFrete.size(), pedidosFrete);
        CompraFreteResponseDto response = this.freteService.organizarFretes(pedidosFrete);
        if (response.error() != null) {
            log.warn("API de frete retornou mensagem de erro na organização: {}", response.error());
            return response.message();
        }
        if (response.message() != null) {
            log.warn("API de frete retornou mensagem de erro na organização: {}", response.message());
            return response.message();
        } else {
            List<Long> idsParaAtualizar = pedidosParaOrganizacao.stream().map(Pedido::getId).toList();
            this.repository.updateStatusBatch(idsParaAtualizar, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
            log.info("Pedidos IDs={} organizados com sucesso — status atualizado para AGUARDANDO_GERACAO_ETIQUETA", idsParaAtualizar);
            return null;
        }
    }

    @Override
    public Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos) {
        log.info("Iniciando geração de etiquetas para {} pedido(s): IDs={}", pedidos.size(), pedidos);

        List<Pedido> pedidosParaOrganizacao = this.repository.findByIdsAndStatus(pedidos, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);

        if (pedidosParaOrganizacao.isEmpty()) {
            log.warn("Nenhum pedido com status AGUARDANDO_GERACAO_ETIQUETA encontrado na lista IDs={}", pedidos);
            throw new ResourceNotFoundException("Nenhum pedido encontrado com status AGUARDANDO_ETIQUETA na lista passada");
        }

        log.debug("{} pedido(s) elegível(is) para geração de etiqueta encontrado(s)", pedidosParaOrganizacao.size());

        Map<String, Long> freteParaPedidoMap = pedidosParaOrganizacao
                .stream()
                .collect(HashMap::new,
                    (map, pedido) -> map.put(pedido.getPedidoFrete().toString(), pedido.getId()),
                    HashMap::putAll);

        List<String> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        log.debug("Solicitando geração de etiquetas à API de frete para UUIDs: {}", pedidosFrete);
        GeracaoEtiquetasResponseDto response = this.freteService.gerarEtiquetasPedidoFrete(pedidosFrete);

        Map<Long, String> resultado = new HashMap<>();
        List<Long> pedidosComSucesso = new ArrayList<>();

        if (response.pedidos() != null && !response.pedidos().isEmpty()) {
            response.pedidos().forEach((freteId, statusEtiqueta) -> {
                Long pedidoId = freteParaPedidoMap.get(freteId);
                if (pedidoId != null) {
                    if (statusEtiqueta.status()) {
                        log.debug("Etiqueta gerada com sucesso para pedido ID={} (freteId={})", pedidoId, freteId);
                        resultado.put(pedidoId, "Etiqueta gerada com sucesso");
                        pedidosComSucesso.add(pedidoId);
                    } else {
                        log.warn("Falha ao gerar etiqueta para pedido ID={} (freteId={}): {}", pedidoId, freteId, statusEtiqueta.message());
                        resultado.put(pedidoId, "Erro ao gerar etiqueta: " + statusEtiqueta.message());
                    }
                }
            });
        } else {
            log.warn("API de frete não retornou dados individuais de etiqueta — aplicando erro genérico a todos os pedidos");
            pedidosParaOrganizacao.forEach(pedido ->
                resultado.put(pedido.getId(), "Erro ao processar geração de etiquetas")
            );
        }

        if (!pedidosComSucesso.isEmpty()) {
            this.repository.updateStatusBatch(pedidosComSucesso, StatusEnum.AGUARDANDO_POSTAGEM);
            log.info("{} pedido(s) com etiqueta gerada — status atualizado para AGUARDANDO_POSTAGEM: IDs={}", pedidosComSucesso.size(), pedidosComSucesso);
        }

        return resultado;
    }

    @Override
    public String imprimirEtiquetasEnvio(List<Long> pedidos) {
        log.info("Iniciando impressão de etiquetas para {} pedido(s): IDs={}", pedidos.size(), pedidos);

        List<Pedido> pedidosParaImpressao = this.repository.findByIdsAndStatus(pedidos, StatusEnum.AGUARDANDO_POSTAGEM);

        if (pedidosParaImpressao.isEmpty()) {
            log.warn("Nenhum pedido com status AGUARDANDO_POSTAGEM encontrado na lista IDs={}", pedidos);
            throw new ResourceNotFoundException("Nenhum pedido válido encontrado para impressão na lista passada");
        }

        List<String> pedidosFrete = pedidosParaImpressao
                .stream()
                .map(pedido -> pedido.getPedidoFrete().toString())
                .toList();

        log.debug("Solicitando URL de impressão de etiquetas à API de frete para UUIDs: {}", pedidosFrete);
        String url = this.freteService.imprimirEtiquetasPedidoFrete(pedidosFrete).url();
        log.info("URL de impressão de etiquetas obtida com sucesso para {} pedido(s)", pedidosParaImpressao.size());
        return url;
    }

    @Transactional
    @Override
    public void rastreioPedidoWebhook(Pedido pedido, RastreioWebhookDto rastreio) {
        log.debug("Webhook de rastreio recebido — validando assinatura HMAC");
        RastreioDataDto data = rastreio.data();

        // Sempre atualiza o status de entrega com o status vindo do payload
        pedido.setStatusEnvio(data.status());

        //Define variáveis de tracking quando elas chegam
        //Pode levar até um dia
        pedido.setTracking(data.tracking());
        pedido.setTrackingUrl(data.trackingUrl());

        switch (rastreio.event()) {

            case "order.created":

                log.info("Etiqueta criada para pedido ID={} — protocolo={}, tracking={}",
                        pedido.getId(), data.protocol(), data.tracking());
                break;

            case "order.released":
                // Etiqueta paga — status interno já gerenciado pelo fluxo de organizarPedidosParaEnvio
                log.debug("Etiqueta paga (order.released) para pedido ID={} — nenhuma ação interna necessária", pedido.getId());
                break;

            case "order.generated":
                // Etiqueta gerada — status interno já gerenciado pelo fluxo de gerarEtiquetasParaEnvio
                log.debug("Etiqueta gerada (order.generated) para pedido ID={} — nenhuma ação interna necessária", pedido.getId());
                break;

            case "order.ready-to-print":
                // Etiqueta gerada — status interno já gerenciado pelo fluxo de gerarEtiquetasParaEnvio
                log.debug("Etiqueta pronta para impressão (order.ready-to-print) para pedido ID={} — nenhuma ação interna necessária", pedido.getId());
                break;

            case "order.received":
                // Encomenda recebida em ponto de distribuição Pegaki — sem mudança de status interno
                log.info("Encomenda recebida em ponto de distribuição para pedido ID={}", pedido.getId());
                break;

            case "order.posted":
                // Encomenda postada → transita internamente para EM_TRANSITO
                pedido.setStatus(StatusEnum.EM_TRANSITO);
                log.info("Encomenda postada — pedido ID={} atualizado para EM_TRANSITO", pedido.getId());
                break;

            case "order.delivered":
                // Encomenda entregue → status ENTREGUE + data de entrega vinda da API
                pedido.setStatus(StatusEnum.ENTREGUE);
                if (data.deliveredAt() != null) {
                    pedido.setDataEntrega(data.deliveredAt().atZoneSameInstant(FUSO_LOJA).toLocalDate());
                }
                log.info("Encomenda entregue — pedido ID={} atualizado para ENTREGUE em {}", pedido.getId(), pedido.getDataEntrega());
                break;

            case "order.undelivered":
                // Tentativa de entrega falhou — apenas statusEnvio atualizado (já feito acima)
                log.warn("Tentativa de entrega falhou (order.undelivered) para pedido ID={} — statusEnvio atualizado para '{}'",
                        pedido.getId(), data.status());
                break;

            case "order.paused":
                // Entrega interrompida, aguardando ação do destinatário — apenas statusEnvio
                log.warn("Entrega pausada (order.paused) para pedido ID={} — ação do destinatário necessária", pedido.getId());
                break;

            case "order.suspended":
                // Encomenda suspensa — apenas statusEnvio
                log.warn("Encomenda suspensa (order.suspended) para pedido ID={}", pedido.getId());
                break;

            case "order.canceled":
                // Etiqueta cancelada (falha interna de nota, não cancelamento do pedido)
                // Retrocede para PREPARANDO_ENVIO para permitir geração de nova etiqueta
                pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
                pedido.setStatusEnvio(null);
                pedido.setTracking(null);
                pedido.setTrackingUrl(null);
                log.warn("Etiqueta cancelada (order.canceled) para pedido ID={} — campos de envio resetados, status revertido para PREPARANDO_ENVIO para nova emissão",
                        pedido.getId());
                break;

            case "order.expired":
                // Etiqueta expirada sem postagem (falha interna de nota, não cancelamento do pedido)
                // Mesmo tratamento do canceled: reabre o pedido para nova emissão de etiqueta
                pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
                pedido.setStatusEnvio(null);
                pedido.setTracking(null);
                pedido.setTrackingUrl(null);
                log.warn("Etiqueta expirada (order.expired) para pedido ID={} — campos de envio resetados, status revertido para PREPARANDO_ENVIO para nova emissão",
                        pedido.getId());
                break;

            default:
                log.warn("Evento de rastreio desconhecido recebido: event={}, pedidoFrete UUID={}", rastreio.event(), data.id());
                break;
        }

        this.repository.update(pedido);
        log.debug("Pedido ID={} persistido após processamento do evento '{}'", pedido.getId(), rastreio.event());
    }

    @Override
    public Optional<Pedido> findByPedidoFrete(UUID pedidoFrete) {
        log.debug("Buscando pedido por pedidoFrete UUID={}", pedidoFrete);
        return this.repository.findByPedidoFrete(pedidoFrete);
    }


    @Transactional
    @Override
    public Pedido cancelarPedido(Long id) {
        log.info("Iniciando cancelamento do pedido ID={}", id);
        Pedido pedido = this.findById(id);

        if (pedido.getStatus() == StatusEnum.CANCELADO) {
            log.warn("Pedido ID={} já está cancelado", id);
            throw new WrongStatusException("O pedido já está cancelado.");
        } else if (pedido.getStatus() == StatusEnum.ENTREGUE) {
            log.warn("Pedido ID={} já foi entregue, cancelamento não permitido", id);
            throw new WrongStatusException("O pedido já foi entregue");
        } else if ((pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) || pedido.getStatus() == StatusEnum.PREPARANDO_ENVIO) {
            this.devolverEstoque(pedido);
            pedido.setStatus(StatusEnum.CANCELADO);
            Pedido pedidoCancelado = this.repository.update(pedido);
            log.info("Pedido ID={} cancelado com sucesso", id);
            return pedidoCancelado;
        } else {
            log.warn("Pedido ID={} não pode ser cancelado diretamente no status={} — devolução necessária", id, pedido.getStatus());
            throw new WrongStatusException("O pedido já foi tramitado. Para fazer o cancelamento, deve ser solicitada uma devolução.");
        }
    }

    @Override
    public PixQrCodeResponseDto obterQrCodeCobrancaPix(Long id) {
        log.info("Iniciando consulta de QR Code de cobrança do pedido ID={}", id);
        Pedido pedido = this.findById(id);

        if (pedido.getStatus() != StatusEnum.AGUARDANDO_PAGAMENTO) {
            log.warn("Pedido ID={} não está aguardando pagamento — status atual={}", id, pedido.getStatus());
            throw new WrongStatusException("O pedido não está aguardando pagamento.");
        }

        if (pedido.getPagamento() == null || pedido.getPagamento().getIdCobranca() == null) {
            log.warn("Pedido ID={} não possui cobrança associada — não é possível gerar QR Code", id);
            throw new RegraDeNegocioException("O pedido não possui cobrança associada.");
        }

        return this.apiCobranca.obterQrCodePix(pedido.getPagamento().getIdCobranca());
    }

    private void devolverEstoque(Pedido pedido) {
        log.debug("Revertendo estoque de {} produto(s) do pedido ID={}", pedido.getProdutosAdicionados().size(), pedido.getId());
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    produto.getProduto().setEstoque(produto.getProduto().getEstoque() + produto.getQuantidade());
                    log.debug("Estoque do produto ID={} restaurado em {} unidade(s)", produto.getProduto().getId(), produto.getQuantidade());
                    this.produtoRepository.update(produto.getProduto());
                });
    }

    /** Marca o pagamento e libera o pedido para envio; idempotente (eventos podem repetir ou chegar fora de ordem). */
    private void registrarPagamento(Long pedidoId, StatusPagamentoEnum status) {
        Pedido pedido = this.findById(pedidoId);
        Pagamento pagamento = pedido.getPagamento();
        if (pagamento != null && pagamento.getStatus() != StatusPagamentoEnum.RECEBIDO) {
            pagamento.setStatus(status);
            if (pagamento.getDataConfirmacao() == null) {
                pagamento.setDataConfirmacao(LocalDateTime.now());
            }
        }

        // Grava antes: pagarPedido relê o pedido do repositório
        this.repository.update(pedido);

        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) {
            this.pagarPedido(pedidoId);
        }
    }

    /** Recusa do cartão pelo Asaas (captura ou análise de risco): cancela e devolve o estoque uma única vez. */
    private void registrarRecusaPorEvento(Long pedidoId, EventoCobrancaDto evento) {
        Pedido pedido = this.findById(pedidoId);
        if (pedido.getStatus() != StatusEnum.AGUARDANDO_PAGAMENTO) {
            log.info("Recusa ignorada para pedido ID={} — status atual={}", pedidoId, pedido.getStatus());
            return;
        }

        String motivo = evento.additionalInfo() != null && evento.additionalInfo().denialReason() != null
                ? evento.additionalInfo().denialReason()
                : "Pagamento recusado pela operadora (" + evento.event() + ")";
        this.registrarRecusa(pedido, motivo);
    }

    @Transactional
    @Override
    public void processarEventoCobranca(Long pedidoId, EventoCobrancaDto evento) {
        log.info("Processando evento de cobrança para pedido ID={} — evento={}", pedidoId, evento.event());

        switch (evento.event()) {
            case "PAYMENT_RECEIVED":
                log.info("Pagamento recebido para pedido ID={}", pedidoId);
                this.registrarPagamento(pedidoId, StatusPagamentoEnum.RECEBIDO);
                break;

            case "PAYMENT_CREDIT_CARD_CAPTURE_REFUSED":
            case "PAYMENT_REPROVED_BY_RISK_ANALYSIS":
                log.warn("Pagamento com cartão recusado para pedido ID={} — evento={}", pedidoId, evento.event());
                this.registrarRecusaPorEvento(pedidoId, evento);
                break;

            case "PAYMENT_OVERDUE":
                log.info("Pagamento vencido para pedido ID={} — atualizando status para CANCELADO", pedidoId);
                break;

            case "PAYMENT_CREATED":
                log.info("Cobrança criada para pedido ID={}", pedidoId);
                break;

            case "PAYMENT_AUTHORIZED":
                log.info("Pagamento autorizado para pedido ID={}", pedidoId);
                break;

            case "PAYMENT_CONFIRMED":
                // Cartão: confirmado já libera o envio; o RECEIVED só chega quando o valor é creditado (~30 dias)
                log.info("Pagamento confirmado para pedido ID={} — liberando para envio", pedidoId);
                this.registrarPagamento(pedidoId, StatusPagamentoEnum.CONFIRMADO);
                break;

            case "PAYMENT_DELETED":
                log.info("Cobrança deletada para pedido ID={}", pedidoId);
                break;

            default:
                log.warn("Evento de cobrança desconhecido recebido: event={}, pedido ID={}", evento.event(), pedidoId);
                break;
        }
    }

    private void verificaEAtualizaEstoqueparaPedido(Pedido pedido) {
        log.debug("Verificando estoque para {} produto(s) do pedido", pedido.getProdutosAdicionados().size());
        pedido.getProdutosAdicionados()
                .forEach(produto -> {
                    if (produto.getProduto().getEstoque() < produto.getQuantidade()) {
                        log.warn("Estoque insuficiente: produto ID={} '{}' — disponível={}, solicitado={}",
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
                    log.debug("Estoque do produto ID={} decrementado em {} unidade(s) — novo estoque={}",
                            produto.getProduto().getId(), produto.getQuantidade(), produto.getProduto().getEstoque());
                    this.produtoRepository.update(produto.getProduto());
                });
    }

    private Pedido enriquecerPedidoPorFrete(Pedido pedido, PedidoFreteResponseDto pedidoFreteResponseDto) {
        pedido.setPedidoFrete(pedidoFreteResponseDto.id());
        pedido.setDataPrevisaoEntrega(LocalDate.now().plusDays(pedidoFreteResponseDto.deliveryMax()));
        pedido.setProtocoloEnvio(pedidoFreteResponseDto.protocol());
        log.debug("Pedido ID={} enriquecido: pedidoFrete UUID={}, previsão de entrega={}",
                pedido.getId(), pedidoFreteResponseDto.id(), pedido.getDataPrevisaoEntrega());
        return this.repository.update(pedido);
    }

    private PedidoFreteRequestDto montarPedidoFreteDto(Pedido pedido) {
        log.debug("Montando DTO de pedido de frete para pedido ID={}", pedido.getId());
        //Definir empresa como remetente
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();
        DadosEmpresa dadosEmpresa = this.dadosEmpresaRepository.get();
        DestinoRemetenteDto remetente = this.definirRemetenteEmpresa(enderecoEmpresa, dadosEmpresa);

        //Definir cliente como destino
        Endereco enderecoDestino = pedido.getEnderecoEntrega();
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

        log.debug("DTO de pedido de frete montado com sucesso para pedido ID={}", pedido.getId());
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
                        String.valueOf(produto.getPrecoUnitario())))
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
                        .getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(pedido.getValorFrete());
    }


}
