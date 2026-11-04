package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.math.BigDecimal;

public record ProdutoEnvio(String nome, int quantidade, BigDecimal valorUnitario) {}
