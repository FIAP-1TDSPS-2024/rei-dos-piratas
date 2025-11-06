package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.CarrinhoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrinho")
public class CarrinhoRestController {

    private final CarrinhoController controller;

    public CarrinhoRestController(CarrinhoController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Método para que o cliente adicione um item, em quantidade desejada, ao seu carrinho")
    @PutMapping("/adicionar")
    public ResponseEntity<CarrinhoOutDto> adicionarProduto(@Valid @RequestBody ItemProdutoInDto itemProduto) {
        CarrinhoOutDto carrinho = this.controller.adicionarProduto(itemProduto);
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Método para que o cliente REMOVA um item, em quantidade desejada, de seu carrinho")
    @PutMapping("/remover")
    public ResponseEntity<CarrinhoOutDto> removerProduto(@Valid @RequestBody ItemProdutoInDto itemProduto) {
        CarrinhoOutDto carrinho = this.controller.removerProduto(itemProduto);
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Método para que o cliente remova TODOS os itens de seu carrinho")
    @PutMapping("/limpar")
    public ResponseEntity<CarrinhoOutDto> limparCarrinho() {
        CarrinhoOutDto carrinho = this.controller.limparCarrinho();
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Método para que o cliente visualize seu carrinho e os itens adicionados no mesmo")
    @GetMapping()
    public ResponseEntity<CarrinhoOutDto> visualizarCarrinho() {
        CarrinhoOutDto carrinho = this.controller.visualizarCarrinho();
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Método para que o cliente finalize seu pedido, coletando os itens do carrinho e criando um novo pedido. O carrinho então é limpo.")
    @PutMapping("/finalizar")
    public ResponseEntity<PedidoOutDto> finalizarCompra() {
       PedidoOutDto pedido = this.controller.finalizarCompra();
       return ResponseEntity.ok(pedido);
    }
}
