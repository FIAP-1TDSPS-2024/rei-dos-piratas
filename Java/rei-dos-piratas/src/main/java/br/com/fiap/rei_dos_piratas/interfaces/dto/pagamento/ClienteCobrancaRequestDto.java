package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

public record ClienteCobrancaRequestDto(
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
) {}

