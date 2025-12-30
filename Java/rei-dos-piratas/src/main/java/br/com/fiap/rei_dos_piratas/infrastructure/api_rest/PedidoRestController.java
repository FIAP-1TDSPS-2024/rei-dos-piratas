package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pedidos", description = "Operações para criação e atualização de pedidos")
@RestController
@RequestMapping("/pedidos")
public class PedidoRestController {

    private final PedidoController controller;

    public PedidoRestController(PedidoController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar pedidos do cliente", description = "Retorna pedidos do cliente paginados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "204", description = "Nenhum pedido encontrado")
    })
    @GetMapping()
    public ResponseEntity<Page<PedidoOutDto>> findAllByCliente(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<PedidoOutDto> pagePedidos = this.controller.findAllByCliente(pageNumber, pageSize);

        return ResponseEntity.ok(pagePedidos);
    }

    @Operation(summary = "Buscar pedido por id", description = "Retorna um pedido pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PedidoOutDto> findById(@PathVariable("id") Long id) {
        PedidoOutDto pedido = this.controller.findById(id);
        return ResponseEntity.ok(pedido);
    }

    @Operation(summary = "Fazer pedido", description = "Finaliza itens e cria um novo pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping()
    public ResponseEntity<PedidoOutDto> fazerPedido(@Valid @RequestBody PedidoInDto pedido) {
        PedidoOutDto novoPedido = this.controller.fazerPedido(pedido);
        return ResponseEntity.ok(novoPedido);
    }

    @Operation(summary = "Registrar pagamento", description = "Marca pedido como pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido pago"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PutMapping("/pagamento/{id}")
    public ResponseEntity<PedidoOutDto> pagarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.pagarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @Operation(summary = "Registrar envio", description = "Marca pedido como enviado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido enviado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PutMapping("/envio/{id}")
    public ResponseEntity<PedidoOutDto> enviarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.enviarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @Operation(summary = "Registrar entrega", description = "Marca pedido como entregue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido entregue"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PutMapping("/entrega/{id}")
    public ResponseEntity<PedidoOutDto> entregarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.entregarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @Operation(summary = "Cancelar pedido", description = "Cancela o pedido informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PutMapping("/cancelamento/{id}")
    public ResponseEntity<PedidoOutDto> cancelarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.cancelarPedido(id);
        return ResponseEntity.ok(pedido);
    }
}
