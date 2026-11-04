package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.EstornoPagamentoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.gateway.EnvioCriado;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayFrete;
import br.com.fiap.rei_dos_piratas.domain.gateway.ResultadoCompraEnvios;
import br.com.fiap.rei_dos_piratas.domain.gateway.SolicitacaoEnvioReverso;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class DevolucaoServiceImpl implements DevolucaoService {

    /** Datas de negócio (ex.: conclusão) no fuso da loja; o Jackson entrega OffsetDateTime em UTC. */
    private static final java.time.ZoneId FUSO_LOJA = java.time.ZoneId.of("America/Sao_Paulo");

    private final DevolucaoRepository repository;

    private final GatewayFrete gatewayFrete;

    /** Id do servico de entrega (Melhor Envio) usado nas devolucoes; vem de ME_SERVICE_DEVOLUCAO. */
    private final Long servicoEntregaDevolucao;

    private final EstornoPagamentoService estornoPagamentoService;

    public DevolucaoServiceImpl(DevolucaoRepository repository, GatewayFrete gatewayFrete, Long servicoEntregaDevolucao,
                                EstornoPagamentoService estornoPagamentoService) {
        this.estornoPagamentoService = estornoPagamentoService;
        this.servicoEntregaDevolucao = servicoEntregaDevolucao;
        this.repository = repository;
        this.gatewayFrete = gatewayFrete;
    }

    @Override
    public Page<Devolucao> findAll(int pageNumber, int pageSize) {
        log.debug("[SERVICE-DEVOLUCAO] findAll - página: {}, tamanho: {}", pageNumber, pageSize);
        return repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Page<Devolucao> findAllByPedido(int pageNumber, int pageSize, Long pedidoId) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByPedido - pedidoId={}, página: {}, tamanho: {}", pedidoId, pageNumber, pageSize);
        return repository.listAllByPedido(pageNumber, pageSize, pedidoId);
    }

    @Override
    public Page<Devolucao> findAllByStatus(int pageNumber, int pageSize, StatusDevolucaoEnum status) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByStatus - status={}, página: {}, tamanho: {}", status, pageNumber, pageSize);
        return repository.listAllByStatus(pageNumber, pageSize, status);
    }

    @Override
    public List<Devolucao> findAllByPedidoId(Long pedidoId) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByPedidoId - pedidoId={}", pedidoId);
        return repository.findAllByPedidoId(pedidoId);
    }

    @Override
    public Optional<Devolucao> findDevolucaoAtivaByPedidoId(Long pedidoId) {
        log.debug("[SERVICE-DEVOLUCAO] findDevolucaoAtivaByPedidoId - pedidoId={}", pedidoId);
        return findAllByPedidoId(pedidoId).stream()
                .filter(devolucao -> devolucao.getStatus() != StatusDevolucaoEnum.CANCELADO
                        && devolucao.getStatus() != StatusDevolucaoEnum.RETORNADO)
                .findFirst();
    }

    @Override
    public Devolucao findById(Long id) {
        log.debug("[SERVICE-DEVOLUCAO] findById - ID={}", id);
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Devolucao solicitarDevolucao(Devolucao devolucao) {
        log.info("[SERVICE-DEVOLUCAO] Solicitando devolução para pedido ID={}, motivo={}", devolucao.getPedido().getId(), devolucao.getMotivo());
        // Regras de negócio serão implementadas aqui
        Pedido pedido = devolucao.getPedido();
        if (devolucao.getServicoEntrega() == null) {
            devolucao.setServicoEntrega(this.servicoEntregaDevolucao);
        }
        devolucao.setValorTotal(devolucao.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        //Verificar se há devoluções em aberto para o pedido
        List<Devolucao> devolucoes = this.findAllByPedidoId(pedido.getId());

        //Valida se não há devoluções ativas
        boolean possuiDevolucaoAtiva = devolucoes
                .stream()
                .anyMatch(d ->
                        d.getStatus() != StatusDevolucaoEnum.CANCELADO &&
                        d.getStatus() != StatusDevolucaoEnum.RETORNADO);

        if (possuiDevolucaoAtiva) {
            throw new RegraDeNegocioException("Não é possível solicitar novas devoluções se há uma devolução ativa");
        }

        //Valida se a devolução é por arrependimento, se for, a aprovação é automática
        //As datas são validadas na classe Devolucao.java
        if (devolucao.getMotivo().getArrependimento()){
            Devolucao criada = repository.create(devolucao);
            return this.aprovarDevolucao(criada.getId());
        }

        log.info("Pedido de devolucao criado com sucesso: ID={}, status={}, valor total=R${}",
                devolucao.getId(), devolucao.getStatus(), devolucao.getValorTotal());

        return repository.create(devolucao);
    }

    @Override
    public Devolucao aprovarDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Aprovando devolução ID={}", id);
        // Atualiza status e datas da devolução aprovada
        Devolucao devolucao = repository.findById(id);

devolucao.setAprovada(true);
        devolucao.setDataAprovacao(LocalDate.now());
        devolucao.setStatus(StatusDevolucaoEnum.PREPARANDO_RETORNO);

        SolicitacaoEnvioReverso solicitacao = SolicitacaoEnvioReverso.para(devolucao, this.servicoEntregaDaDevolucao(devolucao));
        EnvioCriado envio = this.gatewayFrete.criarEnvioReverso(solicitacao);

        this.enriquecerDevolucaoPorFrete(envio, devolucao);
        log.debug("Valor do frete definido: R${}, valor total do pedido: R${}", devolucao.getValorFrete(), devolucao.getValorTotal());

        log.info("Pedido de devolucao frete aprovado com sucesso e criado no serviço de frete: ID={}, status={}, valor total=R${}",
                devolucao.getId(), devolucao.getStatus(), devolucao.getValorTotal());

        return repository.update(devolucao);
    }

    @Transactional
    @Override
    public String organizarDevolucoesParaEnvio(List<Long> devolucoes) {
        if (devolucoes == null || devolucoes.isEmpty()
                || devolucoes.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new RegraDeNegocioException("Informe ao menos um ID válido de devolução.");
        }

        List<Devolucao> devolucoesParaOrganizacao = repository.findByIdsAndStatus(
                devolucoes, StatusDevolucaoEnum.PREPARANDO_RETORNO);

        if (devolucoesParaOrganizacao.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Nenhuma devolução com status PREPARANDO_RETORNO encontrada na lista passada");
        }

        if (devolucoesParaOrganizacao.stream().anyMatch(devolucao -> devolucao.getPedidoFrete() == null)) {
            throw new RegraDeNegocioException(
                    "Todas as devoluções selecionadas para organização devem possuir um pedido de frete.");
        }

        List<UUID> pedidosFrete = devolucoesParaOrganizacao.stream()
                .map(Devolucao::getPedidoFrete)
                .toList();

        log.info("[SERVICE-DEVOLUCAO] Organizando fretes de {} devolução(ões)", devolucoesParaOrganizacao.size());
        ResultadoCompraEnvios compra = this.gatewayFrete.comprarEnvios(pedidosFrete);
        if (!compra.sucesso()) {
            return compra.mensagemErro();
        }

        // A devolução reutiliza a nota existente e segue diretamente para postagem.
        List<Long> idsParaAtualizar = devolucoesParaOrganizacao.stream().map(Devolucao::getId).toList();
        repository.updateStatusBatch(idsParaAtualizar, StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO);
        log.info("[SERVICE-DEVOLUCAO] Fretes organizados para devoluções IDs={}", idsParaAtualizar);
        return null;
    }

    @Override
    public Devolucao recusarDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Recusando devolução ID={}", id);
        // Regras de negócio serão implementadas aqui
        Devolucao devolucao = repository.findById(id);
        return repository.update(devolucao);
    }

    @Override
    /**
     * O funcionário conclui a devolução depois de receber e conferir os itens: o cliente é reembolsado
     * pelo valor dos itens (e do frete de ida no arrependimento do pedido inteiro). Uma vez por devolução.
     */
    public Devolucao concluirDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Concluindo devolução ID={}", id);
        Devolucao devolucao = repository.findById(id);
        if (devolucao.getStatus() != StatusDevolucaoEnum.RETORNADO) {
            log.warn("[SERVICE-DEVOLUCAO] Devolução ID={} no status {} não pode ser concluída", id, devolucao.getStatus());
            throw new WrongStatusException("A devolução só pode ser concluída depois que os itens retornarem à loja.");
        }

        Long pedidoId = devolucao.getPedido().getId();
        BigDecimal valor = devolucao.valorAReembolsar();
        log.info("[SERVICE-DEVOLUCAO] Reembolsando R${} da devolução ID={} do pedido ID={}", valor, id, pedidoId);
        this.estornoPagamentoService.estornar(pedidoId,
                pedido -> pedido.getPagamento().solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, id, valor),
                "Devolução " + id + " do pedido " + pedidoId);
        return devolucao;
    }

    @Override
    public Optional<Devolucao> findByPedidoFrete(UUID pedidoFrete) {
        log.debug("[SERVICE-DEVOLUCAO] findByPedidoFrete - UUID={}", pedidoFrete);
        return repository.findByPedidoFrete(pedidoFrete);
    }

    @Transactional
    @Override
    public void rastreioDevolucaoWebhook(Devolucao devolucao, EventoRastreio evento) {

        devolucao.setStatusEnvio(evento.statusEnvio());
        devolucao.setTracking(evento.codigoRastreio());
        devolucao.setTrackingUrl(evento.urlRastreio());

        if (evento.protocolo() != null && !evento.protocolo().isBlank()) {
            devolucao.setProtocoloEnvio(evento.protocolo());
        }

        switch (evento.tipo()) {
            case CRIADO:
                log.info("[SERVICE-DEVOLUCAO] Etiqueta de devolução criada - devolucaoId={}, pedidoFrete={}, protocolo={}",
                        devolucao.getId(), devolucao.getPedidoFrete(), devolucao.getProtocoloEnvio());
                break;

            case PAGO:
            case ETIQUETA_GERADA:
                log.debug("[SERVICE-DEVOLUCAO] Evento '{}' recebido para devolucaoId={} sem transição interna adicional",
                        evento.tipoOriginal(), devolucao.getId());
                break;

            case RECEBIDO_PONTO:
                log.info("[SERVICE-DEVOLUCAO] Encomenda de devolução recebida no ponto de distribuição - devolucaoId={}", devolucao.getId());
                break;

            case POSTADO:
                devolucao.setStatus(StatusDevolucaoEnum.EM_RETORNO);
                log.info("[SERVICE-DEVOLUCAO] Devolução postada - devolucaoId={} atualizada para EM_RETORNO", devolucao.getId());
                break;

            case ENTREGUE:
                devolucao.setStatus(StatusDevolucaoEnum.RETORNADO);
                if (evento.dataEntrega() != null) {
                    devolucao.setDataConclusao(evento.dataEntrega());
                }
                log.info("[SERVICE-DEVOLUCAO] Devolução entregue - devolucaoId={} atualizada para RETORNADO em {}",
                        devolucao.getId(), devolucao.getDataConclusao());
                break;

            case NAO_ENTREGUE:
            case PAUSADO:
            case SUSPENSO:
                log.warn("[SERVICE-DEVOLUCAO] Evento '{}' recebido para devolucaoId={} - status de envio atualizado para '{}'",
                        evento.tipoOriginal(), devolucao.getId(), evento.statusEnvio());
                break;

            case CANCELADO:
            case EXPIRADO:
                devolucao.setStatus(StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO);
                devolucao.setStatusEnvio(null);
                devolucao.setTracking(null);
                devolucao.setTrackingUrl(null);
                log.warn("[SERVICE-DEVOLUCAO] Etiqueta de devolução {} - devolucaoId={} revertida para AGUARDANDO_POSTAGEM_RETORNO",
                        evento.tipoOriginal(), devolucao.getId());
                break;

            default:
                log.warn("[SERVICE-DEVOLUCAO] Evento de rastreio desconhecido recebido: event={}, pedidoFrete UUID={}",
                        evento.tipoOriginal(), evento.envio());
                break;
        }

        repository.update(devolucao);
        log.debug("[SERVICE-DEVOLUCAO] Devolução ID={} persistida após processamento do evento '{}'", devolucao.getId(), evento.tipoOriginal());
    }

    /** Usa o servico gravado na devolucao; devolucoes antigas (sem servico) usam o configurado. */
    private Long servicoEntregaDaDevolucao(Devolucao devolucao) {
        if (devolucao.getServicoEntrega() != null) {
            return devolucao.getServicoEntrega();
        }
        if (this.servicoEntregaDevolucao == null) {
            throw new IllegalStateException(
                    "Servico de entrega para devolucoes nao configurado. Defina a variavel ME_SERVICE_DEVOLUCAO.");
        }
        devolucao.setServicoEntrega(this.servicoEntregaDevolucao);
        return this.servicoEntregaDevolucao;
    }

    private BigDecimal calcularValorTotalDevolucao(Devolucao devolucao){

        if (devolucao.getValorFrete() == null){
            throw new RegraDeNegocioException("Consulte o valor do frete do pedido antes do cálculo de valor total");
        }

        //O valor total do pedido é calculado somando os valores dos produtos, multplicando por sua quantidade e por fim adicionando o valor do frete
        return devolucao.getItens()
                .stream()
                .map(item -> item
                        .getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(devolucao.getValorFrete());
    }

    private void enriquecerDevolucaoPorFrete(EnvioCriado envio, Devolucao devolucao){
        devolucao.setValorFrete(envio.preco());
        devolucao.setValorTotal(this.calcularValorTotalDevolucao(devolucao));
        devolucao.setPedidoFrete(envio.id());
        devolucao.setProtocoloEnvio(envio.protocolo());
    }

}
