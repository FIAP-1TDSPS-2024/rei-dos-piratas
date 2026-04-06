package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Produtos", description = "Operações para consulta e inserção de produtos")
@RestController
@RequestMapping("/produtos")
public class ProdutoRestController {

    private final ProdutoController controller;

    public ProdutoRestController(ProdutoController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar todos os produtos", description = "Retorna todos os produtos da loja paginados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "204", description = "Nenhum produto encontrado")
    })
    @GetMapping
    public ResponseEntity<Page<ProdutoOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<ProdutoOutDto> produtos = this.controller.findAll(pageNumber, pageSize);

        if (produtos.numberOfPages() > 0) {
            return ResponseEntity.ok(produtos);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar produto por id", description = "Retorna um produto pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoOutDto> findById(@PathVariable("id") Long id) {
        ProdutoOutDto produto = this.controller.findById(id);
        return ResponseEntity.ok(produto);
    }

    @Operation(summary = "Criar produto", description = "Inserção de um novo produto na loja, acessível para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ProdutoOutDto> create(@RequestBody ProdutoInDto produto) {
        ProdutoOutDto novoProduto = this.controller.create(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    @Operation(summary = "Atualizar produto", description = "Atualização de um produto na loja, acessível para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping
    public ResponseEntity<ProdutoOutDto> update(@RequestBody Produto produto) {
        ProdutoOutDto produtoAtualizado = this.controller.update(produto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    @Operation(summary = "Deletar produto", description = "Deleção de um produto na loja, acessível para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        this.controller.delete(id);
        return ResponseEntity.noContent().build();
    }
}
