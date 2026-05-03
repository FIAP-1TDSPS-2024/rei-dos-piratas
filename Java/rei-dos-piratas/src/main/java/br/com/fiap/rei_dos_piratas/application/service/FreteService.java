package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.GeracaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.etiqueta.ImpressaoEtiquetasResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;

import java.util.List;

public interface FreteService {
    List<FreteServiceDto> calcularFreteProdutos(String cepDestino, List<ItemProdutoPedido> itens);
    PedidoFreteResponseDto criarPedidoFrete(PedidoFreteRequestDto pedidoRequest);
    CompraFreteResponseDto organizarPedidoFrete(List<String> pedidos);
    GeracaoEtiquetasResponseDto gerarEtiquetasPedidoFrete(List<String> pedidos);
    ImpressaoEtiquetasResponseDto imprimirEtiquetasPedidoFrete(List<String> pedidos);
}
