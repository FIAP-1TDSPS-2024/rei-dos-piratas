package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clientes", description = "Operações sobre clientes")
@RestController
@RequestMapping("/clientes")
public class ClienteRestController {
    private final ClienteController controller;

    public ClienteRestController(ClienteController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar clientes", description = "Retorna página de clientes para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum cliente encontrado"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<ClienteOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {

        Page<ClienteOutDto> clientes = this.controller.listAll(pageNumber, pageSize);

        if (clientes.numberOfPages() > 0) {
            return ResponseEntity.ok(clientes);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar cliente por id", description = "Retorna os dados de um cliente a partir do id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteOutDto> findById(@PathVariable("id") Long id) {
        ClienteOutDto cliente = this.controller.findById(id);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @PutMapping
    public ResponseEntity<ClienteOutDto> update(@RequestBody ClienteInDto cliente) {
        ClienteOutDto novoCliente = this.controller.update(cliente);
        return ResponseEntity.ok(novoCliente);
    }

    @Operation(summary = "Excluir cliente", description = "Remove o cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @DeleteMapping
    public ResponseEntity<?> delete() {
        this.controller.delete();
        return ResponseEntity.noContent().build();
    }
}
