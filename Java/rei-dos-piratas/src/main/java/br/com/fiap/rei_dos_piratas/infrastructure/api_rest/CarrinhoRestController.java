package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoCarrinhoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Carrinho", description = "Operações para manipulação do carrinho do cliente")
@RestController
@RequestMapping("/carrinho")
public class CarrinhoRestController {

    private final CarrinhoController controller;

    public CarrinhoRestController(CarrinhoController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Adicionar produto ao carrinho", description = "Adiciona quantidade do produto ao carrinho")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/adicionar")
    public ResponseEntity<CarrinhoOutDto> adicionarProduto(@Valid @RequestBody ItemProdutoInDto itemProduto) {
        CarrinhoOutDto carrinho = this.controller.adicionarProduto(itemProduto);
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Remover produto do carrinho", description = "Remove quantidade do produto do carrinho")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto removido"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/remover")
    public ResponseEntity<CarrinhoOutDto> removerProduto(@Valid @RequestBody ItemProdutoInDto itemProduto) {
        CarrinhoOutDto carrinho = this.controller.removerProduto(itemProduto);
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Limpar carrinho", description = "Remove todos os itens do carrinho")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrinho limpo")
    })
    @PutMapping("/limpar")
    public ResponseEntity<CarrinhoOutDto> limparCarrinho() {
        CarrinhoOutDto carrinho = this.controller.limparCarrinho();
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Visualizar carrinho", description = "Retorna os itens do carrinho do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrinho retornado"),
            @ApiResponse(responseCode = "204", description = "Carrinho vazio")
    })
    @GetMapping()
    public ResponseEntity<CarrinhoOutDto> visualizarCarrinho() {
        CarrinhoOutDto carrinho = this.controller.visualizarCarrinho();
        return ResponseEntity.ok(carrinho);
    }

    @Operation(summary = "Finalizar compra", description = "Cria pedido com os itens do carrinho e limpa o carrinho")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/finalizar")
    public ResponseEntity<PedidoOutDto> finalizarCompra(PedidoCarrinhoInDto pedidoDto) {
        PedidoOutDto pedido = this.controller.finalizarCompra(pedidoDto);
        return ResponseEntity.ok(pedido);
    }
}
