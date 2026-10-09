package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record ClienteCobrancaRequestDto(
        String name,
        String cpfCnpj,
        String email,
        String mobilePhone,
        String address,
        String addressNumber,
        String province,
        String postalCode,
        String externalReference,
        Boolean notificationDisabled
) {}

