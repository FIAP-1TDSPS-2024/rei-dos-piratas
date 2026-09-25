package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoOptionsDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoPackageDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class DevolucaoServiceImpl implements DevolucaoService {

    private final DevolucaoRepository repository;

    private final FreteService freteService;

    public DevolucaoServiceImpl(DevolucaoRepository repository, FreteService freteService) {
        this.repository = repository;
        this.freteService = freteService;
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
    public Page<Devolucao> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByStatus - status={}, página: {}, tamanho: {}", status, pageNumber, pageSize);
        return repository.listAllByStatus(pageNumber, pageSize, status);
    }

    @Override
    public List<Devolucao> findAllByPedidoId(Long pedidoId) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByPedidoId - pedidoId={}", pedidoId);
        return repository.findAllByPedidoId(pedidoId);
    }

    @Override
    public Devolucao findById(Long id) {
        log.debug("[SERVICE-DEVOLUCAO] findById - ID={}", id);
        return repository.findById(id);
    }

    @Override
    public Devolucao solicitarDevolucao(Devolucao devolucao) {
        log.info("[SERVICE-DEVOLUCAO] Solicitando devolução para pedido ID={}, motivo={}", devolucao.getPedido().getId(), devolucao.getMotivo());
        // Regras de negócio serão implementadas aqui
        Pedido pedido = devolucao.getPedido();

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
            this.aprovarDevolucao(devolucao.getId());
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
        devolucao.setStatus(StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO);

        DevolucaoFreteRequestDto request = this.montarDevolucaoFreteDto(devolucao);
        DevolucaoFreteResponseDto response = this.freteService.criarPedidoDevolucaoFrete(request);

        this.mapearDevolucaoFrete(response, devolucao);
        log.debug("Valor do frete definido: R${}, valor total do pedido: R${}", devolucao.getValorFrete(), devolucao.getValorTotal());

        log.info("Pedido de devolucao frete aprovado com sucesso e criado no serviço de frete: ID={}, status={}, valor total=R${}",
                devolucao.getId(), devolucao.getStatus(), devolucao.getValorTotal());

        return repository.update(devolucao);
    }

    @Override
    public Devolucao recusarDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Recusando devolução ID={}", id);
        // Regras de negócio serão implementadas aqui
        Devolucao devolucao = repository.findById(id);
        return repository.update(devolucao);
    }

    @Override
    public Devolucao concluirDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Concluindo devolução ID={}", id);
        // Regras de negócio serão implementadas aqui
        Devolucao devolucao = repository.findById(id);
        return repository.update(devolucao);
    }

    private DevolucaoFreteRequestDto montarDevolucaoFreteDto(Devolucao devolucao){

        //Definir dimensoes do pacote de acordo com a quantidade de volumes
        DevolucaoPackageDto devolucaoPackage = this.definirDimensoesPacote(devolucao);

        //A princípio rodas as devoluções serão por envio
        DevolucaoOptionsDto optionsDto = new DevolucaoOptionsDto(false, false);

        return new DevolucaoFreteRequestDto(
                Math.toIntExact(devolucao.getServicoEntrega()),
                devolucao.getPedido().getCliente().getEmail(),
                devolucao.getPedido().getCliente().getCelular(),
                devolucao.getValorTotal(),
                devolucao.getPedido().getPedidoFrete(),
                devolucaoPackage,
                optionsDto
        );
    }

    private DevolucaoPackageDto definirDimensoesPacote(Devolucao devolucao) {
        int quantidadeTotal = devolucao.getItens().stream()
                .mapToInt(ItemDevolucao::getQuantidade)
                .sum();

        BigDecimal pesoTotal = devolucao.getItens().stream()
                .map(produto -> produto.getItemPedido().getProduto().getPeso().multiply(BigDecimal.valueOf(produto.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (quantidadeTotal < 6) {
            int altura = devolucao.getItens().stream()
                    .mapToInt(produto -> produto.getItemPedido().getProduto().getProfundidade().intValue() * produto.getQuantidade())
                    .sum() + 3;

            return new DevolucaoPackageDto(altura, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 11) {
            return new DevolucaoPackageDto(28, 16, 26, pesoTotal);
        }

        if (quantidadeTotal < 20) {
            return new DevolucaoPackageDto(28, 32, 26, pesoTotal);
        }

        return new DevolucaoPackageDto(28, 32, 52, pesoTotal);
    }

    private BigDecimal calcularValorTotalDevolucao(Devolucao devolucao){

        if (devolucao.getValorFrete() == null){
            throw new RegraDeNegocioException("Consulte o valor do frete do pedido antes do cálculo de valor total");
        }

        //O valor total do pedido é calculado somando os valores dos produtos, multplicando por sua quantidade e por fim adicionando o valor do frete
        return devolucao.getItens()
                .stream()
                .map(item -> item
                        .getItemPedido()
                        .getProduto()
                        .getPreco()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(devolucao.getValorFrete());
    }

    private void mapearDevolucaoFrete(DevolucaoFreteResponseDto response, Devolucao devolucao){
        devolucao.setValorFrete(response.price());
        devolucao.setValorTotal(this.calcularValorTotalDevolucao(devolucao));
        devolucao.setPedidoFrete(response.id());
        devolucao.setProtocoloEnvio(response.protocol());
    }

}

