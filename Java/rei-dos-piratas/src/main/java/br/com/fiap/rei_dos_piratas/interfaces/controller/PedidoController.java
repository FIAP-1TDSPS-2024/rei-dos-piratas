package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.BoletoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EstornoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PixQrCodeOutDto;

import java.util.List;
import java.util.Map;

public interface PedidoController {
    Page<PedidoOutDto> findAllByCliente(int pageNumber, int pageSize);
    Page<PedidoOutDto> findAllByStatus(int pageNumber, int pageSize, StatusEnum status);
    PedidoOutDto findById(Long id);
    /** {@code ipRemoto}: IP do comprador, exigido pelo Asaas nos pagamentos com cartão. */
    PedidoOutDto fazerPedido(PedidoInDto pedido, String ipRemoto);
    String organizarPedidosParaEnvio(List<Long> pedidos);
    Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos);
    String imprimirEtiquetasEnvio(List<Long> pedidos);
    PedidoOutDto cancelarPedido(Long id);
    PixQrCodeOutDto obterQrCodeCobrancaPix(Long id);
    BoletoOutDto obterBoleto(Long id);

    List<EstornoOutDto> listarEstornos(Long id);
}
