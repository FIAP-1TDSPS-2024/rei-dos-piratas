package br.com.fiap.rei_dos_piratas.domain.entity;

import java.time.LocalDate;

/** Dados para o cliente pagar o boleto (consultados no Asaas na hora; não são persistidos, exceto a URL). */
public record DadosBoleto(
        String linhaDigitavel,
        String codigoBarras,
        String urlBoleto,
        LocalDate dataVencimento
) {}
