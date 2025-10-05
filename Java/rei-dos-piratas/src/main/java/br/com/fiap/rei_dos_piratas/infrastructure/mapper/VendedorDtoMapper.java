package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorOutDto;

public class VendedorDtoMapper {

    public static Vendedor toEntity(VendedorInDto dto){
        if(dto == null) return null;
        return new Vendedor(
                dto.userName(),
                dto.nomeCompleto(),
                dto.email(),
                dto.senha(),
                dto.role());
    }

    public static VendedorOutDto toDto(Vendedor vendedor){
        if(vendedor == null) return null;
        return new VendedorOutDto(
                vendedor.getId(),
                vendedor.getUserName(),
                vendedor.getNomeCompleto(),
                vendedor.getEmail(),
                vendedor.isUsuarioAtivo(),
                vendedor.getDataCadastro(),
                vendedor.getRole());
    }

    private VendedorDtoMapper() {
    }
}
