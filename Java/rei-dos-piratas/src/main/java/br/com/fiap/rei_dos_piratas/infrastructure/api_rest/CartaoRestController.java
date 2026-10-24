package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.CartaoClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoClienteOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Só consulta: os cartões são tokenizados e gravados dentro do fluxo de pedido.
 */
@Tag(name = "Cartões", description = "Cartões de crédito salvos (tokenizados) do cliente logado")
@RestController
@RequestMapping("/cartoes")
public class CartaoRestController {

    private final CartaoClienteController controller;

    public CartaoRestController(CartaoClienteController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Listar cartões salvos",
            description = "Retorna os cartões ativos do cliente logado (bandeira e 4 últimos dígitos). O token não é exposto; use o id como cartao_id no pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se o cliente não tiver cartões)"),
            @ApiResponse(responseCode = "400", description = "A conta logada não é de cliente"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping
    public ResponseEntity<List<CartaoClienteOutDto>> listarCartoesDoClienteAtual() {
        return ResponseEntity.ok(this.controller.listarCartoesDoClienteAtual());
    }
}
