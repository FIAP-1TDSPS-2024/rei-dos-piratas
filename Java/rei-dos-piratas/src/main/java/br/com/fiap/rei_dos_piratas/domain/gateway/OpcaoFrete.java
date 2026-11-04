package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.math.BigDecimal;

/** Opção de entrega cotada no fornecedor de frete. {@code id} é o serviço escolhido no pedido. */
public record OpcaoFrete(
        Long id,
        String nome,
        BigDecimal preco,
        BigDecimal precoPersonalizado,
        BigDecimal desconto,
        String moeda,
        Integer prazoDias,
        Transportadora transportadora
) {}
