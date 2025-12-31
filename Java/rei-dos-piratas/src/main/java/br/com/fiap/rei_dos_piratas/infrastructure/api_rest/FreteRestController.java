package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.FreteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.FreteServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Frete", description = "Operações relacionadas ao frete melhor envio")
@RestController
@RequestMapping("/frete")
public class FreteRestController {

    private final FreteController controller;

    public FreteRestController(FreteController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Consultar opções e valores de fretes", description = "Consultar opções e valores de fretes para produtos, retornando opções de serviços")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "404", description = "Não foram encontrados serviços para essa entrega"),
            @ApiResponse(responseCode = "502", description = "Erro temporário no serviço de frete")
    })
    @PostMapping
    public List<FreteServiceDto> calcularFreteProdutos(@RequestBody @Valid ConsultaFreteDto dto) {
        return controller.calcularFreteProdutos(dto);
    }
}
