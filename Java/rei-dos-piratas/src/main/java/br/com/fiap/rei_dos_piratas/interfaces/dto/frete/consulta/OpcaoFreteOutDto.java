package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta;

import java.math.BigDecimal;

/**
 * Opção de frete devolvida ao app. Os nomes dos campos mantêm o contrato JSON já consumido
 * (id, name, price, custom_price, discount, currency, delivery_time, company).
 */
public record OpcaoFreteOutDto(
        Long id,
        String name,
        BigDecimal price,
        BigDecimal customPrice,
        BigDecimal discount,
        String currency,
        Integer deliveryTime,
        Company company
) {
    public record Company(Long id, String name, String picture) {}
}
