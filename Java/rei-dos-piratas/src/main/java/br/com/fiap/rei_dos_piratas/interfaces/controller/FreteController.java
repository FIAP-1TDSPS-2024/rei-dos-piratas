package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;

import java.util.List;

public interface FreteController {
    List<FreteServiceDto> calcularFreteProdutos(ConsultaFreteDto consultaFreteDto);
}
