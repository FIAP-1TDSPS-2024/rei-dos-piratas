package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.OpcaoFreteOutDto;

import java.util.List;

public interface FreteController {
    List<OpcaoFreteOutDto> calcularFreteProdutos(ConsultaFreteDto consultaFreteDto);
}
