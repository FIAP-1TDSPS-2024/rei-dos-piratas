package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoCobrancaRequestDto(
        String customer,
        TipoPagamentoEnum billingType,
        BigDecimal value,
        LocalDate dueDate,
        String description,
        String externalReference
) {}
