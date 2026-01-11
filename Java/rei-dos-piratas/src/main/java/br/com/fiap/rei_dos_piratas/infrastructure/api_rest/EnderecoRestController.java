package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.EnderecoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Endereços", description = "Operações sobre endereços para que um cliente adicione novos endereços a sua conta")
@RestController
@RequestMapping("/enderecos")
public class EnderecoRestController {

    private final EnderecoController controller;

    public EnderecoRestController(EnderecoController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar endereços associados ao cliente", description = "Retorna página de endereços associados ao cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "204", description = "Nenhum funcionário encontrado")
    })
    @GetMapping
    ResponseEntity<Page<EnderecoOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber)
    {
        Page<EnderecoOutDto> enderecos = this.controller.findAll(pageNumber, pageSize);

        if (enderecos.numberOfPages() > 0){
            return ResponseEntity.ok(enderecos);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar funcionário por id", description = "Retorna os dados de um endereço por seu ID, se o cliente for dono do ID ou um funcionário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    @GetMapping("/{id}")
    ResponseEntity<EnderecoOutDto> findById(@PathVariable("id") Long id){
        EnderecoOutDto endereco = this.controller.findById(id);
        return ResponseEntity.ok(endereco);
    }

    @Operation(summary = "Criar Endereço", description = "Cria um novo funcionário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Endereço criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    ResponseEntity<EnderecoOutDto> save(@RequestBody EnderecoInDto endereco){
        EnderecoOutDto novoEndereco = this.controller.save(endereco);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(novoEndereco);
    }

    @Operation(summary = "Atualizar funcionário", description = "Atualiza dados do funcionário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping
    ResponseEntity<EnderecoOutDto> update(@RequestBody Endereco endereco){
        EnderecoOutDto enderecoAtualizado = this.controller.update(endereco);
        return ResponseEntity.ok(enderecoAtualizado);
    }
}
