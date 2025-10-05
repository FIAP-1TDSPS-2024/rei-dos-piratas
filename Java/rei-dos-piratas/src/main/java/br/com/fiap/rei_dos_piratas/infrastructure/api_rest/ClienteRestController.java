package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteRestController {
    private final ClienteController controller;

    public ClienteRestController(ClienteController controller) {
        this.controller = controller;
    }

    @GetMapping
    public ResponseEntity<Page<ClienteOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<ClienteOutDto> clientes = this.controller.listAll(pageNumber, pageSize);

        if (clientes.numberOfPages() > 0){
            return ResponseEntity.ok(clientes);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteOutDto> findById(@PathVariable("id") Long id) {
        ClienteOutDto cliente = this.controller.findById(id);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    public ResponseEntity<ClienteOutDto> create(@Valid @RequestBody ClienteInDto cliente) {
        ClienteOutDto novoCliente = this.controller.create(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCliente);
    }

    @PutMapping
    public ResponseEntity<ClienteOutDto> update(@Valid @RequestBody Cliente cliente) {
        ClienteOutDto novoCliente = this.controller.update(cliente);
        return ResponseEntity.ok(novoCliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        this.controller.delete(id);
        return ResponseEntity.noContent().build();
    }

}
