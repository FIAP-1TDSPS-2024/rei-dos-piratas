package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.CarrinhoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrinho/cliente/{clienteId}")
public class CarrinhoRestController {

    private final CarrinhoController controller;

    public CarrinhoRestController(CarrinhoController controller) {
        this.controller = controller;
    }

    @PutMapping("/adicionar")
    public ResponseEntity<CarrinhoOutDto> adicionarProduto(@PathVariable("clienteId") Long clienteId, @Valid @RequestBody ItemProdutoInDto itemProduto) {
        CarrinhoOutDto carrinho = this.controller.adicionarProduto(clienteId, itemProduto);
        return ResponseEntity.ok(carrinho);
    }

    @PutMapping("/remover")
    public ResponseEntity<CarrinhoOutDto> removerProduto(@PathVariable("clienteId") Long clienteId, @Valid @RequestBody ItemProdutoInDto itemProduto) {
        CarrinhoOutDto carrinho = this.controller.removerProduto(clienteId, itemProduto);
        return ResponseEntity.ok(carrinho);
    }

    @PutMapping("/limpar")
    public ResponseEntity<CarrinhoOutDto> limparCarrinho(@PathVariable("clienteId") Long clienteId) {
        CarrinhoOutDto carrinho = this.controller.limparCarrinho(clienteId);
        return ResponseEntity.ok(carrinho);
    }

    @GetMapping()
    public ResponseEntity<CarrinhoOutDto> visualizarCarrinho(@PathVariable("clienteId") Long clienteId) {
        CarrinhoOutDto carrinho = this.controller.visualizarCarrinho(clienteId);
        return ResponseEntity.ok(carrinho);
    }

    @PutMapping("/finalizar")
    public ResponseEntity<PedidoOutDto> finalizarCompra(@PathVariable("clienteId") Long clienteId) {
       PedidoOutDto pedido = this.controller.finalizarCompra(clienteId);
       return ResponseEntity.ok(pedido);
    }
}
