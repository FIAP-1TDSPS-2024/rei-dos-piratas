package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record ClienteCobrancaResponseDto(
        String object,
        String id,
        LocalDate dateCreated,
        String name,
        String cpfCnpj,
        String email,
        String phone,
        String mobilePhone,
        String address,
        String addressNumber,
        String province,
        String postalCode,
        String externalReference,
        Boolean notificationDisabled
) {
}
