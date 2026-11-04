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
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CobrancaSemRespostaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.domain.gateway.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;
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
    private final GatewayFrete gatewayFrete;

    private final ClienteService clienteService;

    private final GatewayPagamento gatewayPagamento;
    private final UsuarioAtualService usuarioAtual;

    private final CartaoService cartaoService;
    private final RegraParcelamento regraParcelamento;
    private final RegraBoleto regraBoleto;
    private final ExecutorTransacional executorTransacional;

    public PedidoServiceImpl(PedidoRepository repository, ProdutoRepository produtoRepository, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService, GatewayFrete gatewayFrete, ClienteService clienteService, GatewayPagamento gatewayPagamento, UsuarioAtualService usuarioAtual,
                             CartaoService cartaoService, RegraParcelamento regraParcelamento, RegraBoleto regraBoleto,
                             ExecutorTransacional executorTransacional) {
        this.cartaoService = cartaoService;
        this.regraParcelamento = regraParcelamento;
        this.regraBoleto = regraBoleto;
        this.executorTransacional = executorTransacional;
        this.usuarioAtual = usuarioAtual;
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.enderecoService = enderecoService;
        this.dadosEmpresaRepository = dadosEmpresaRepository;
        this.freteService = freteService;
        this.gatewayFrete = gatewayFrete;
        this.clienteService = clienteService;
        this.gatewayPagamento = gatewayPagamento;
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
        // PIX/boleto: o Asaas não exige o IP do comprador
        return this.criarCobrancaPedido(pedidoCriado, null);
    }

    /**
     * Cartão, em três passos para nunca haver cobrança sem pedido gravado:
     * <ol>
     *   <li>T1: grava o pedido (estoque baixado, AGUARDANDO_PAGAMENTO / PENDENTE) e confirma;</li>
     *   <li>sem transação: tokeniza e cobra no fornecedor (até 60s, sem segurar conexão do banco);</li>
     *   <li>T2: relê o pedido (o evento do fornecedor pode ter chegado antes) e registra o resultado.</li>
     * </ol>
     * Recusa grava CANCELADO + RECUSADO e propaga; erro do fornecedor (cobrança certamente não criada)
     * cancela e propaga; sem resposta, o pedido fica pendente e o evento de pagamento resolve (sem nova tentativa).
     * Não é {@code @Transactional}: as transações são abertas pelo {@link ExecutorTransacional}.
     */
    @Override
    public Pedido fazerPedido(Pedido pedido, PagamentoCartao pagamentoCartao) {
        if (!TipoPagamentoEnum.CREDIT_CARD.equals(pedido.getTipoPagamento())) {
            // Autochamada não passa pelo proxy do @Transactional: abre a transação explicitamente
            return this.executorTransacional.emTransacao(() -> this.fazerPedido(pedido));
        }

        if (pagamentoCartao == null) {
            log.warn("Pedido com pagamento por cartão sem dados do cartão — cliente ID={}", pedido.getCliente().getId());
            throw new RegraDeNegocioException("Informe o cartão de crédito para pagamento com cartão");
        }

        pedido.getPagamento().setParcelas(pagamentoCartao.parcelas());
        Pedido pedidoCriado = this.executorTransacional.emTransacao(() -> this.criarPedido(pedido));
        Long pedidoId = pedidoCriado.getId();

        CartaoCliente cartao;
        try {
            cartao = this.cartaoService.obterOuTokenizar(
                    pedidoCriado.getCliente(), pedidoCriado.getEnderecoEntrega(), pagamentoCartao);
        } catch (PagamentoRecusadoException e) {
            this.executorTransacional.emTransacaoSemRetorno(() -> this.registrarRecusaDoCartao(pedidoId, e.getMessage()));
            throw e;
        } catch (RuntimeException e) {
            // Nada foi cobrado: cancela para devolver o estoque
            this.executorTransacional.emTransacaoSemRetorno(() -> this.cancelarPorFalhaNaCobranca(pedidoId));
            throw e;
        }

        pedidoCriado.getPagamento().setCartao(cartao);
        SolicitacaoCobranca solicitacao = SolicitacaoCobranca.para(
                pedidoCriado, this.regraBoleto, LocalDate.now(FUSO_LOJA), pagamentoCartao.ipRemoto());

        CobrancaCriada cobranca;
        try {
            cobranca = this.gatewayPagamento.criarCobranca(solicitacao);
        } catch (PagamentoRecusadoException e) {
            this.executorTransacional.emTransacaoSemRetorno(() -> this.registrarRecusaDoCartao(pedidoId, e.getMessage()));
            throw e;
        } catch (CobrancaSemRespostaException e) {
            log.warn("Cobrança do pedido ID={} sem resposta do fornecedor — pedido mantido pendente até o evento de pagamento", pedidoId);
            return this.executorTransacional.emTransacao(() -> this.registrarCartaoDoPagamento(pedidoId, cartao));
        } catch (ApiExternaException e) {
            this.executorTransacional.emTransacaoSemRetorno(() -> this.cancelarPorFalhaNaCobranca(pedidoId));
            throw e;
        }

        return this.executorTransacional.emTransacao(() -> this.registrarCobrancaDoCartao(pedidoId, cartao, cobranca));
    }

    /** T2 do cartão aprovado ou em análise. Idempotente com o evento de pagamento, que pode ter chegado antes. */
    private Pedido registrarCobrancaDoCartao(Long pedidoId, CartaoCliente cartao, CobrancaCriada cobranca) {
        Pedido pedido = this.repository.findById(pedidoId);
        Pagamento pagamento = pedido.getPagamento();
        pagamento.setCartao(cartao);
        if (pagamento.getIdCobranca() == null) {
            pagamento.setIdCobranca(cobranca.idCobranca());
        }
        pagamento.setValorLiquido(cobranca.valorLiquido());

        // Em análise de risco fica PENDENTE até o evento; nunca rebaixa um status já avançado pelo evento
        if (cobranca.confirmada() && pagamento.getStatus() == StatusPagamentoEnum.PENDENTE) {
            pagamento.setStatus(StatusPagamentoEnum.CONFIRMADO);
            if (pagamento.getDataConfirmacao() == null) {
                pagamento.setDataConfirmacao(LocalDateTime.now());
            }
        }

        log.info("Cobrança do cartão registrada: pedido ID={}, cobrança ID={}, status do pagamento={}",
                pedidoId, pagamento.getIdCobranca(), pagamento.getStatus());
        return this.repository.update(pedido);
    }

    /** Sem resposta do fornecedor: guarda o cartão usado e mantém o pedido como está. */
    private Pedido registrarCartaoDoPagamento(Long pedidoId, CartaoCliente cartao) {
        Pedido pedido = this.repository.findById(pedidoId);
        pedido.getPagamento().setCartao(cartao);
        return this.repository.update(pedido);
    }

    /** Recusa síncrona do cartão (tokenização ou cobrança); ignorada se um evento já tirou o pedido de AGUARDANDO_PAGAMENTO. */
    private void registrarRecusaDoCartao(Long pedidoId, String motivo) {
        Pedido pedido = this.repository.findById(pedidoId);
        if (pedido.getStatus() != StatusEnum.AGUARDANDO_PAGAMENTO) {
            log.info("Recusa síncrona ignorada para pedido ID={} — status atual={}", pedidoId, pedido.getStatus());
            return;
        }
        this.registrarRecusa(pedido, motivo);
    }

    /** O fornecedor respondeu com erro (não é recusa): a cobrança não existe, cancela e devolve o estoque. */
    private void cancelarPorFalhaNaCobranca(Long pedidoId) {
        Pedido pedido = this.repository.findById(pedidoId);
        if (pedido.getStatus() != StatusEnum.AGUARDANDO_PAGAMENTO) {
            return;
        }
        log.warn("Falha ao cobrar o pedido ID={} no fornecedor — cancelando e devolvendo estoque", pedidoId);
        this.devolverEstoque(pedido);
        pedido.setStatus(StatusEnum.CANCELADO);
        pedido.getPagamento().setStatus(StatusPagamentoEnum.CANCELADO);
        this.repository.update(pedido);
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

        List<OpcaoFrete> fretes = this.freteService.calcularFreteProdutos(
                pedido.getEnderecoEntrega().getCep(),
                pedido.getProdutosAdicionados());

        log.debug("API de frete retornou {} opção(ões) de entrega", fretes.size());

        Optional<OpcaoFrete> consultaFrete = fretes
                .stream()
                .filter(frete -> frete.id().equals(pedido.getServicoEntrega()))
                .findFirst();

        if (consultaFrete.isEmpty()) {
            log.warn("Serviço de frete ID={} não encontrado entre as opções retornadas pela API", pedido.getServicoEntrega());
            throw new ResourceNotFoundException("Esse serviço de entrega não existe para esse serviço");
        }

        pedido.setValorFrete(consultaFrete.get().preco());
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

    private Pedido criarCobrancaPedido(Pedido pedido, String ipRemoto) {

        SolicitacaoCobranca solicitacao = SolicitacaoCobranca.para(pedido, this.regraBoleto, LocalDate.now(FUSO_LOJA), ipRemoto);
        CobrancaCriada cobranca = this.gatewayPagamento.criarCobranca(solicitacao);

        Pagamento pagamento = pedido.getPagamento();
        pagamento.setIdCobranca(cobranca.idCobranca());
        pagamento.setValorLiquido(cobranca.valorLiquido());

        if (TipoPagamentoEnum.BOLETO.equals(pagamento.getTipo())) {
            pagamento.setUrlBoleto(cobranca.urlBoleto());
            pagamento.setDataVencimento(solicitacao.vencimento());
        }

        // Cartão: o fornecedor responde na hora; recusa vem como erro (PagamentoRecusadoException).
        // Em análise de risco a cobrança fica pendente até o evento de pagamento.
        if (TipoPagamentoEnum.CREDIT_CARD.equals(pagamento.getTipo()) && cobranca.confirmada()) {
            pagamento.setStatus(StatusPagamentoEnum.CONFIRMADO);
            pagamento.setDataConfirmacao(LocalDateTime.now());
        }

        log.info("Cobrança criada com sucesso: ID={}, valor líquido=R${}, status do pagamento={}",
                cobranca.idCobranca(), cobranca.valorLiquido(), pagamento.getStatus());

        return this.repository.update(pedido);
    }

    @Transactional
    @Override
    public void pagarPedido(Long id) {
        log.info("Iniciando pagamento do pedido ID={}", id);
        Pedido pedido = this.findById(id);

        if (pedido.getStatus() == StatusEnum.AGUARDANDO_PAGAMENTO) {
            pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
            log.debug("Status do pedido ID={} atualizado para PREPARANDO_ENVIO, criando pedido de frete na API", id);

            SolicitacaoEnvio solicitacao = SolicitacaoEnvio.para(
                    pedido, this.enderecoService.getEnderecoEmpresa(), this.dadosEmpresaRepository.get());
            EnvioCriado envio = this.gatewayFrete.criarEnvio(solicitacao);
            log.info("Pedido de frete criado com sucesso — pedidoFrete UUID={}, previsão de entrega em {} dias",
                    envio.id(), envio.prazoMaximoDias());

            pedido = this.enriquecerPedidoPorFrete(pedido, envio);
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

        List<UUID> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(Pedido::getPedidoFrete)
                .toList();

        log.debug("Enviando {} UUID(s) de pedido de frete para API de organização: {}", pedidosFrete.size(), pedidosFrete);
        ResultadoCompraEnvios compra = this.gatewayFrete.comprarEnvios(pedidosFrete);
        if (!compra.sucesso()) {
            log.warn("Compra dos fretes recusada pelo fornecedor: {}", compra.mensagemErro());
            return compra.mensagemErro();
        }

        List<Long> idsParaAtualizar = pedidosParaOrganizacao.stream().map(Pedido::getId).toList();
        this.repository.updateStatusBatch(idsParaAtualizar, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        log.info("Fretes organizados para pedidos IDs={}", idsParaAtualizar);
        return null;
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

        Map<UUID, Long> freteParaPedidoMap = pedidosParaOrganizacao
                .stream()
                .collect(HashMap::new,
                    (map, pedido) -> map.put(pedido.getPedidoFrete(), pedido.getId()),
                    HashMap::putAll);

        List<UUID> pedidosFrete = pedidosParaOrganizacao
                .stream()
                .map(Pedido::getPedidoFrete)
                .toList();

        log.debug("Solicitando geração de etiquetas à API de frete para UUIDs: {}", pedidosFrete);
        Map<UUID, ResultadoEtiqueta> etiquetas = this.gatewayFrete.gerarEtiquetas(pedidosFrete);

        Map<Long, String> resultado = new HashMap<>();
        List<Long> pedidosComSucesso = new ArrayList<>();

        if (etiquetas != null && !etiquetas.isEmpty()) {
            etiquetas.forEach((freteId, etiqueta) -> {
                Long pedidoId = freteParaPedidoMap.get(freteId);
                if (pedidoId != null) {
                    if (etiqueta.gerada()) {
                        log.debug("Etiqueta gerada com sucesso para pedido ID={} (freteId={})", pedidoId, freteId);
                        resultado.put(pedidoId, "Etiqueta gerada com sucesso");
                        pedidosComSucesso.add(pedidoId);
                    } else {
                        log.warn("Falha ao gerar etiqueta para pedido ID={} (freteId={}): {}", pedidoId, freteId, etiqueta.mensagem());
                        resultado.put(pedidoId, "Erro ao gerar etiqueta: " + etiqueta.mensagem());
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

        List<UUID> pedidosFrete = pedidosParaImpressao
                .stream()
                .map(Pedido::getPedidoFrete)
                .toList();

        log.debug("Solicitando URL de impressão de etiquetas à API de frete para UUIDs: {}", pedidosFrete);
        String url = this.gatewayFrete.obterLinkImpressao(pedidosFrete);
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
    public DadosBoleto obterBoleto(Long id) {
        log.info("Consultando boleto do pedido ID={}", id);
        Pedido pedido = this.findById(id);
        Pagamento pagamento = pedido.getPagamento();

        if (pagamento == null || !TipoPagamentoEnum.BOLETO.equals(pagamento.getTipo())) {
            throw new RegraDeNegocioException("O pedido não foi pago com boleto.");
        }
        if (pagamento.getIdCobranca() == null) {
            log.warn("Pedido ID={} não possui cobrança de boleto associada", id);
            throw new RegraDeNegocioException("O pedido não possui cobrança associada.");
        }

        // Linha digitável consultada na hora: o Asaas recomenda buscar de novo se valor/vencimento mudarem
        LinhaDigitavel linha = this.gatewayPagamento.obterLinhaDigitavel(pagamento.getIdCobranca());
        return new DadosBoleto(linha.linhaDigitavel(), linha.codigoBarras(), pagamento.getUrlBoleto(), pagamento.getDataVencimento());
    }

    @Override
    public QrCodePix obterQrCodeCobrancaPix(Long id) {
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

        return this.gatewayPagamento.obterQrCodePix(pedido.getPagamento().getIdCobranca());
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

    /**
     * Marca o pagamento e libera o pedido para envio; idempotente (eventos podem repetir ou chegar fora de ordem).
     * Se a criação da cobrança ficou sem resposta, o pagamento não tem id da cobrança: usa o do evento.
     */
    private void registrarPagamento(Long pedidoId, StatusPagamentoEnum status, String idCobrancaDoEvento) {
        Pedido pedido = this.findById(pedidoId);
        Pagamento pagamento = pedido.getPagamento();
        if (pagamento != null && pagamento.getIdCobranca() == null && idCobrancaDoEvento != null) {
            log.info("Pagamento do pedido ID={} sem cobrança registrada — usando a cobrança {} do evento", pedidoId, idCobrancaDoEvento);
            pagamento.setIdCobranca(idCobrancaDoEvento);
        }
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
        } else if (pedido.getStatus() == StatusEnum.CANCELADO) {
            // Ex.: boleto compensado depois do vencimento. Não reativa (o estoque pode já ter sido vendido).
            log.error("Pagamento recebido para pedido CANCELADO ID={} (cobrança {}) — estorno manual necessário",
                    pedidoId, pagamento != null ? pagamento.getIdCobranca() : null);
        }
    }

    /** Cobrança vencida sem pagamento (boleto/PIX): cancela e devolve o estoque uma única vez. */
    private void registrarVencimento(Long pedidoId) {
        Pedido pedido = this.findById(pedidoId);
        if (pedido.getStatus() != StatusEnum.AGUARDANDO_PAGAMENTO) {
            log.info("Vencimento ignorado para pedido ID={} — status atual={}", pedidoId, pedido.getStatus());
            return;
        }

        log.info("Cancelando pedido ID={} por cobrança vencida e devolvendo estoque", pedidoId);
        this.devolverEstoque(pedido);
        pedido.setStatus(StatusEnum.CANCELADO);
        if (pedido.getPagamento() != null) {
            pedido.getPagamento().setStatus(StatusPagamentoEnum.CANCELADO);
        }
        this.repository.update(pedido);
    }

    /** Recusa do cartão pelo Asaas (captura ou análise de risco): cancela e devolve o estoque uma única vez. */
    private void registrarRecusaPorEvento(Long pedidoId, EventoPagamento evento) {
        Pedido pedido = this.findById(pedidoId);
        if (pedido.getStatus() != StatusEnum.AGUARDANDO_PAGAMENTO) {
            log.info("Recusa ignorada para pedido ID={} — status atual={}", pedidoId, pedido.getStatus());
            return;
        }

        this.registrarRecusa(pedido, evento.motivoRecusaOuPadrao());
    }

    @Transactional
    @Override
    public void processarEventoCobranca(Long pedidoId, EventoPagamento evento) {
        log.info("Processando evento de cobrança para pedido ID={} — evento={} ({})", pedidoId, evento.tipo(), evento.tipoOriginal());

        switch (evento.tipo()) {
            case RECEBIDA -> {
                log.info("Pagamento recebido para pedido ID={}", pedidoId);
                this.registrarPagamento(pedidoId, StatusPagamentoEnum.RECEBIDO, evento.idCobranca());
            }
            case CONFIRMADA -> {
                // Cartão: confirmado já libera o envio; o RECEBIDA só chega quando o valor é creditado (~30 dias)
                log.info("Pagamento confirmado para pedido ID={} — liberando para envio", pedidoId);
                this.registrarPagamento(pedidoId, StatusPagamentoEnum.CONFIRMADO, evento.idCobranca());
            }
            case RECUSADA -> {
                log.warn("Pagamento com cartão recusado para pedido ID={} — evento={}", pedidoId, evento.tipoOriginal());
                this.registrarRecusaPorEvento(pedidoId, evento);
            }
            case VENCIDA -> {
                log.info("Cobrança vencida sem pagamento para pedido ID={}", pedidoId);
                this.registrarVencimento(pedidoId);
            }
            case CRIADA, AUTORIZADA, REMOVIDA ->
                    log.info("Evento {} sem efeito no pedido ID={}", evento.tipoOriginal(), pedidoId);
            default ->
                    log.warn("Evento de cobrança sem tratamento recebido: event={}, pedido ID={}", evento.tipoOriginal(), pedidoId);
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

    private Pedido enriquecerPedidoPorFrete(Pedido pedido, EnvioCriado envio) {
        pedido.setPedidoFrete(envio.id());
        pedido.setDataPrevisaoEntrega(LocalDate.now().plusDays(envio.prazoMaximoDias()));
        pedido.setProtocoloEnvio(envio.protocolo());
        log.debug("Pedido ID={} enriquecido: pedidoFrete UUID={}, previsão de entrega={}",
                pedido.getId(), envio.id(), pedido.getDataPrevisaoEntrega());
        return this.repository.update(pedido);
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
