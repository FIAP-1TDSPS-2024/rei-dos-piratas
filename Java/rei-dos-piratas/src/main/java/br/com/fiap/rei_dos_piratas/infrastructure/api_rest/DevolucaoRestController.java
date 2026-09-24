package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Devoluções", description = "Operações para solicitação e gestão de devoluções")
@RestController
@RequestMapping("/devolucoes")
public class DevolucaoRestController {

    private final DevolucaoController controller;

    public DevolucaoRestController(DevolucaoController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar todas as devoluções", description = "Retorna todas as devoluções paginadas — exclusivo para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "204", description = "Nenhuma devolução encontrada")
    })
    @GetMapping
    public ResponseEntity<Page<DevolucaoOutDto>> findAll(
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {
        return ResponseEntity.ok(controller.findAll(pageNumber, pageSize));
    }

    @Operation(summary = "Listar devoluções por pedido (paginado)", description = "Retorna devoluções de um pedido específico com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Page<DevolucaoOutDto>> findAllByPedido(
            @PathVariable Long pedidoId,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {
        return ResponseEntity.ok(controller.findAllByPedido(pageNumber, pageSize, pedidoId));
    }

    @Operation(summary = "Listar devoluções por status (paginado)", description = "Retorna devoluções filtradas por status")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<DevolucaoOutDto>> findAllByStatus(
            @PathVariable StatusEnum status,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber) {
        return ResponseEntity.ok(controller.findAllByStatus(pageNumber, pageSize, status));
    }

    @Operation(summary = "Listar todas as devoluções de um pedido", description = "Retorna todas as devoluções de um pedido sem paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/pedido/{pedidoId}/todas")
    public ResponseEntity<List<DevolucaoOutDto>> findAllByPedidoId(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(controller.findAllByPedidoId(pedidoId));
    }

    @Operation(summary = "Buscar devolução por ID", description = "Retorna uma devolução pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolução encontrada"),
            @ApiResponse(responseCode = "404", description = "Devolução não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DevolucaoOutDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(controller.findById(id));
    }

    @Operation(summary = "Solicitar devolução", description = "Abre uma solicitação de devolução para um pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Devolução solicitada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PostMapping
    public ResponseEntity<DevolucaoOutDto> solicitarDevolucao(@RequestBody DevolucaoInDto devolucaoInDto) {
        return ResponseEntity.status(201).body(controller.solicitarDevolucao(devolucaoInDto));
    }

    @Operation(summary = "Aprovar devolução", description = "Aprova a devolução informada — exclusivo para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolução aprovada"),
            @ApiResponse(responseCode = "404", description = "Devolução não encontrada")
    })
    @PutMapping("/{id}/aprovar")
    public ResponseEntity<DevolucaoOutDto> aprovarDevolucao(@PathVariable Long id) {
        return ResponseEntity.ok(controller.aprovarDevolucao(id));
    }

    @Operation(summary = "Recusar devolução", description = "Recusa a devolução informada — exclusivo para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolução recusada"),
            @ApiResponse(responseCode = "404", description = "Devolução não encontrada")
    })
    @PutMapping("/{id}/recusar")
    public ResponseEntity<DevolucaoOutDto> recusarDevolucao(@PathVariable Long id) {
        return ResponseEntity.ok(controller.recusarDevolucao(id));
    }

    @Operation(summary = "Concluir devolução", description = "Marca a devolução como concluída — exclusivo para funcionários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolução concluída"),
            @ApiResponse(responseCode = "404", description = "Devolução não encontrada")
    })
    @PutMapping("/{id}/concluir")
    public ResponseEntity<DevolucaoOutDto> concluirDevolucao(@PathVariable Long id) {
        return ResponseEntity.ok(controller.concluirDevolucao(id));
    }
}

