package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.FuncionarioOutDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioRestController {

    private final FuncionarioController controller;

    public FuncionarioRestController(FuncionarioController controller) {
        this.controller = controller;
    }

    @GetMapping
    public ResponseEntity<Page<FuncionarioOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<FuncionarioOutDto> vendedores = this.controller.listAll(pageNumber, pageSize);

        if (vendedores.numberOfPages() > 0){
            return ResponseEntity.ok(vendedores);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioOutDto> findById(@PathVariable("id") Long id) {
        FuncionarioOutDto vendedor = this.controller.findById(id);
        return ResponseEntity.ok(vendedor);
    }

    @PostMapping
    public ResponseEntity<FuncionarioOutDto> create(@Valid @RequestBody FuncionarioInDto vendedor) {
        FuncionarioOutDto novoVendedor = this.controller.create(vendedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoVendedor);
    }

    @PutMapping
    public ResponseEntity<FuncionarioOutDto> update(@Valid @RequestBody Funcionario funcionario) {
        FuncionarioOutDto novoVendedor = this.controller.update(funcionario);
        return ResponseEntity.ok(novoVendedor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        this.controller.delete(id);
        return ResponseEntity.noContent().build();
    }
}
