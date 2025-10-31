package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoOutDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoRestController {

    private final PedidoController controller;

    public PedidoRestController(PedidoController controller) {
        this.controller = controller;
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<Page<PedidoOutDto>> findAllByCliente(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @PathVariable("clienteId") Long clienteId) {

        Page<PedidoOutDto> pagePedidos = this.controller.findAllByCliente(pageNumber, pageSize, clienteId);

        return ResponseEntity.ok(pagePedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoOutDto> findById(@PathVariable("id") Long id) {
        PedidoOutDto pedido = this.controller.findById(id);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<PedidoOutDto> fazerPedido(@Valid @RequestBody PedidoInDto pedido, @Valid @PathVariable Long clienteId) {
        PedidoOutDto novoPedido = this.controller.fazerPedido(pedido, clienteId);
        return ResponseEntity.ok(novoPedido);
    }

    @PutMapping("/pagamento/{id}")
    public ResponseEntity<PedidoOutDto> pagarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.pagarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/envio/{id}")
    public ResponseEntity<PedidoOutDto> enviarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.enviarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/entrega/{id}")
    public ResponseEntity<PedidoOutDto> entregarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.entregarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/cancelamento/{id}")
    public ResponseEntity<PedidoOutDto> cancelarPedido(@PathVariable Long id){
        PedidoOutDto pedido = this.controller.cancelarPedido(id);
        return ResponseEntity.ok(pedido);
    }
}
