package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido;

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
