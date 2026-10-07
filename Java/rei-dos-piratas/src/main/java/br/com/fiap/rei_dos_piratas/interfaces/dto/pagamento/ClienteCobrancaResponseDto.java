package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import java.time.LocalDate;

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
