package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.gateway.*;
import br.com.fiap.rei_dos_piratas.domain.entity.Volume;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.*;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Adaptador da porta {@link GatewayFrete} para a API do Melhor Envio.
 * Erros HTTP são traduzidos pelo {@code MelhorEnvioErrorDecoder}; falha de comunicação (timeout, conexão)
 * vira {@link ApiExternaException} aqui, para a {@code FeignException} crua não chegar ao serviço.
 */
@Slf4j
public class GatewayFreteMelhorEnvio implements GatewayFrete {

    private final FreteAppClient api;

    public GatewayFreteMelhorEnvio(FreteAppClient api) {
        this.api = api;
    }

    @Override
    public List<OpcaoFrete> cotar(String cepOrigem, String cepDestino, List<ItemProdutoPedido> itens) {
        log.info("[MELHOR-ENVIO] Cotando frete de {} item(ns)", itens.size());
        ConsultaFreteServiceDto consulta = new ConsultaFreteServiceDto(
                Map.of("postal_code", cepOrigem),
                Map.of("postal_code", cepDestino),
                itens.stream().map(this::produtoDaCotacao).toList());

        List<FreteServiceDto> opcoes = chamar("cotar o frete", () -> this.api.calcularFrete(consulta));
        log.info("[MELHOR-ENVIO] Cotação concluída — {} opção(ões)", opcoes.size());
        return opcoes.stream().map(this::opcaoDoDominio).toList();
    }

    @Override
    public EnvioCriado criarEnvio(SolicitacaoEnvio solicitacao) {
        log.info("[MELHOR-ENVIO] Criando envio — serviço ID={}", solicitacao.servico());
        Volume volume = solicitacao.volume();
        PedidoFreteRequestDto requisicao = new PedidoFreteRequestDto(
                solicitacao.servico(),
                parte(solicitacao.remetente()),
                parte(solicitacao.destinatario()),
                solicitacao.produtos().stream()
                        .map(produto -> new ProdutoPedidoFreteDto(produto.nome(),
                                String.valueOf(produto.quantidade()), String.valueOf(produto.valorUnitario())))
                        .toList(),
                List.of(new VolumeFreteDto(volume.altura(), volume.largura(), volume.comprimento(), volume.peso())),
                new FreteOptionsDto(solicitacao.plataforma(), solicitacao.lembrete(), solicitacao.valorSeguro(),
                        false, false, false, false, null));

        PedidoFreteResponseDto resposta = chamar("criar o pedido de frete", () -> this.api.criarPedidoFrete(requisicao));
        log.info("[MELHOR-ENVIO] Envio criado — ID={}", resposta.id());
        return new EnvioCriado(resposta.id(), resposta.protocol(), resposta.price(), resposta.deliveryMax());
    }

    @Override
    public EnvioCriado criarEnvioReverso(SolicitacaoEnvioReverso solicitacao) {
        log.info("[MELHOR-ENVIO] Criando envio reverso do envio ID={}", solicitacao.envioOriginal());
        Volume volume = solicitacao.volume();
        DevolucaoFreteRequestDto requisicao = new DevolucaoFreteRequestDto(
                Math.toIntExact(solicitacao.servico()),
                solicitacao.emailRemetente(),
                solicitacao.telefoneRemetente(),
                solicitacao.valorSeguro(),
                solicitacao.envioOriginal(),
                new DevolucaoPackageDto(volume.altura(), volume.largura(), volume.comprimento(), volume.peso()),
                // Por enquanto toda devolução é por envio simples (sem mão própria nem aviso de recebimento)
                new DevolucaoOptionsDto(false, false));

        DevolucaoFreteResponseDto resposta = chamar("criar o pedido de frete reverso", () -> this.api.criarDevolucaoFrete(requisicao));
        log.info("[MELHOR-ENVIO] Envio reverso criado — ID={}", resposta.id());
        return new EnvioCriado(resposta.id(), resposta.protocol(), resposta.price(), resposta.deliveryMax());
    }

    @Override
    public ResultadoCompraEnvios comprarEnvios(List<UUID> envios) {
        log.info("[MELHOR-ENVIO] Comprando {} envio(s)", envios.size());
        CompraFreteResponseDto resposta = chamar("organizar os pedidos de frete",
                () -> this.api.pagarPedidoFrete(textos(envios)));
        String mensagemErro = resposta.message() != null ? resposta.message() : resposta.error();
        if (mensagemErro != null) {
            log.warn("[MELHOR-ENVIO] Compra de {} envio(s) recusada: {}", envios.size(), mensagemErro);
        }
        return new ResultadoCompraEnvios(mensagemErro);
    }

    @Override
    public Map<UUID, ResultadoEtiqueta> gerarEtiquetas(List<UUID> envios) {
        log.info("[MELHOR-ENVIO] Gerando etiquetas de {} envio(s)", envios.size());
        GeracaoEtiquetasResponseDto resposta = chamar("gerar as etiquetas",
                () -> this.api.gerarEtiquetas(Map.of("orders", textos(envios))));

        Map<UUID, ResultadoEtiqueta> resultado = new HashMap<>();
        resposta.pedidos().forEach((id, status) -> {
            try {
                resultado.put(UUID.fromString(id),
                        new ResultadoEtiqueta(Boolean.TRUE.equals(status.status()), status.message()));
            } catch (IllegalArgumentException e) {
                log.warn("[MELHOR-ENVIO] Chave inesperada na geração de etiquetas ignorada: {}", id);
            }
        });
        return resultado;
    }

    @Override
    public String obterLinkImpressao(List<UUID> envios) {
        log.info("[MELHOR-ENVIO] Gerando link de impressão de {} envio(s)", envios.size());
        return chamar("gerar o link de impressão",
                () -> this.api.gerarLinkImpressaoEtiquetas(Map.of("orders", textos(envios)))).url();
    }

    private static List<String> textos(List<UUID> envios) {
        return envios.stream().map(UUID::toString).toList();
    }

    private static DestinoRemetenteDto parte(ParteEnvio parte) {
        return new DestinoRemetenteDto(parte.nome(), parte.email(), parte.telefone(), parte.cpf(), parte.cnpj(),
                parte.inscricaoEstadual(), parte.cnae(), parte.logradouro(), parte.complemento(), parte.numero(),
                parte.bairro(), parte.cidade(), parte.cep(), parte.uf());
    }

    private ProdutoCalculoFreteDto produtoDaCotacao(ItemProdutoPedido item) {
        return new ProdutoCalculoFreteDto(
                item.getProduto().getId(),
                item.getProduto().getLargura(),
                item.getProduto().getAltura(),
                item.getProduto().getProfundidade(),
                item.getProduto().getPeso(),
                item.getPrecoUnitario(),
                item.getQuantidade());
    }

    private OpcaoFrete opcaoDoDominio(FreteServiceDto dto) {
        Transportadora transportadora = dto.company() == null ? null
                : new Transportadora(dto.company().id(), dto.company().name(), dto.company().picture());
        return new OpcaoFrete(dto.id(), dto.name(), dto.price(), dto.customPrice(), dto.discount(),
                dto.currency(), dto.deliveryTime(), transportadora);
    }

    /** Erros já decodificados passam; falha de comunicação vira ApiExternaException. */
    private <T> T chamar(String operacao, Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (ApiExternaException e) {
            throw e;
        } catch (FeignException e) {
            log.error("[MELHOR-ENVIO] Falha de comunicação ao {}: {}", operacao, e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de frete ao " + operacao + ". Tente novamente.");
        }
    }
}
