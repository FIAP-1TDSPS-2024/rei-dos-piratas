package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Funcionários", description = "Operações sobre funcionários")
@RestController
@RequestMapping("/funcionarios")
public class FuncionarioRestController {

    private final FuncionarioController controller;

    public FuncionarioRestController(FuncionarioController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar funcionários", description = "Retorna página de funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "204", description = "Nenhum funcionário encontrado")
    })
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

    @Operation(summary = "Buscar funcionário por id", description = "Retorna os dados do funcionário pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<FuncionarioOutDto>> findById(@PathVariable("id") Long id) {
        FuncionarioOutDto funcionario = this.controller.findById(id);

        EntityModel<FuncionarioOutDto> resource = EntityModel.of(funcionario);
        resource.add(linkTo(methodOn(FuncionarioRestController.class).findById(id)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Criar funcionário", description = "Cria um novo funcionário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Funcionário criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<FuncionarioOutDto> create(@RequestBody FuncionarioInDto vendedor) {
        FuncionarioOutDto novoVendedor = this.controller.create(vendedor);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(novoVendedor);
    }

    @Operation(summary = "Atualizar funcionário", description = "Atualiza dados do funcionário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping
    public ResponseEntity<FuncionarioOutDto> update(@RequestBody Funcionario funcionario) {
        FuncionarioOutDto novoVendedor = this.controller.update(funcionario);
        return ResponseEntity.ok(novoVendedor);
    }

    @Operation(summary = "Ativar/Desativar funcionário", description = "Alterna estado ativo do funcionário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioOutDto> ativarDesativar(@PathVariable("id") Long id) {
        FuncionarioOutDto funcionario = this.controller.ativarDesativar(id);
        return ResponseEntity.ok(funcionario);
    }
}
