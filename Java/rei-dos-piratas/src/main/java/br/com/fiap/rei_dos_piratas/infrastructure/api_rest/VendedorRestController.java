package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.interfaces.controller.VendedorController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorOutDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedores")
public class VendedorRestController {

    private final VendedorController controller;

    public VendedorRestController(VendedorController controller) {
        this.controller = controller;
    }

    @GetMapping
    public ResponseEntity<Page<VendedorOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<VendedorOutDto> vendedores = this.controller.listAll(pageNumber, pageSize);

        if (vendedores.numberOfPages() > 0){
            return ResponseEntity.ok(vendedores);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorOutDto> findById(@PathVariable("id") Long id) {
        VendedorOutDto vendedor = this.controller.findById(id);
        return ResponseEntity.ok(vendedor);
    }

    @PostMapping
    public ResponseEntity<VendedorOutDto> create(@Valid @RequestBody VendedorInDto vendedor) {
        VendedorOutDto novoVendedor = this.controller.create(vendedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoVendedor);
    }

    @PutMapping
    public ResponseEntity<VendedorOutDto> update(@Valid @RequestBody Vendedor vendedor) {
        VendedorOutDto novoVendedor = this.controller.update(vendedor);
        return ResponseEntity.ok(novoVendedor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        this.controller.delete(id);
        return ResponseEntity.noContent().build();
    }
}
