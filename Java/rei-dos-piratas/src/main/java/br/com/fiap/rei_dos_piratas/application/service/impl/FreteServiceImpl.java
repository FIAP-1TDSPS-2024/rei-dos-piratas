package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayFrete;
import br.com.fiap.rei_dos_piratas.domain.gateway.OpcaoFrete;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FreteServiceImpl implements FreteService {

    private final GatewayFrete gatewayFrete;
    private final EnderecoService enderecoService;

    public FreteServiceImpl(GatewayFrete gatewayFrete, EnderecoService enderecoService) {
        this.gatewayFrete = gatewayFrete;
        this.enderecoService = enderecoService;
    }

    /** Cotação do CEP da loja ao CEP do cliente; erros do fornecedor já chegam traduzidos pelo adaptador. */
    @Override
    public List<OpcaoFrete> calcularFreteProdutos(String cepCliente, List<ItemProdutoPedido> itens) {
        Endereco enderecoEmpresa = this.enderecoService.getEnderecoEmpresa();
        return this.gatewayFrete.cotar(enderecoEmpresa.getCep(), cepCliente, itens);
    }
}
