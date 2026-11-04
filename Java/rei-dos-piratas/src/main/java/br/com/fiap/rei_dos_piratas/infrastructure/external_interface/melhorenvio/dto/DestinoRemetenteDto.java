package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

public record DestinoRemetenteDto(
        String name,

        String email,

        String phone,

        String document,

        String companyDocument,

        String stateRegister,

        String economicActivityCode,

        String address,

        String complement,

        String number,

        String district,

        String city,

        String postalCode,

        String stateAbbr
){}
