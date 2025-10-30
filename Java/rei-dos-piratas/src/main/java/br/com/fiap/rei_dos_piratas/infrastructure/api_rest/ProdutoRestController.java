package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.FuncionarioOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoOutDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoRestController {

    private final ProdutoController controller;

    public ProdutoRestController(ProdutoController controller) {
        this.controller = controller;
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<ProdutoOutDto> produtos = this.controller.findAll(pageNumber, pageSize);

        if (produtos.numberOfPages() > 0){
            return ResponseEntity.ok(produtos);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoOutDto> findById(@PathVariable("id") Long id) {
        ProdutoOutDto produto = this.controller.findById(id);
        return ResponseEntity.ok(produto);
    }

    @PostMapping
    public ResponseEntity<ProdutoOutDto> create(@Valid @RequestBody ProdutoInDto produto) {
        ProdutoOutDto novoProduto = this.controller.create(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    @PutMapping
    public ResponseEntity<ProdutoOutDto> update(@Valid @RequestBody Produto produto) {
        ProdutoOutDto novoVendedor = this.controller.update(produto);
        return ResponseEntity.ok(novoVendedor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        this.controller.delete(id);
        return ResponseEntity.noContent().build();
    }
}
