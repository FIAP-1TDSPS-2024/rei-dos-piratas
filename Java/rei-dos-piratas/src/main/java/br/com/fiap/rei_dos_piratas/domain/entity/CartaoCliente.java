package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/** Cartão tokenizado no Asaas. Guarda só o token, os 4 últimos dígitos e a bandeira. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartaoCliente {
    private Long id;

    @NotNull(message = "O cartão deve ter um cliente associado")
    private Cliente cliente;

    // Fora do toString para não vazar em logs
    @ToString.Exclude
    @NotBlank(message = "O token do cartão é obrigatório")
    @Size(max = 100, message = "O token do cartão deve ter até 100 caracteres")
    private String token;

    @ToString.Exclude
    @NotNull(message = "Os últimos dígitos do cartão são obrigatórios")
    @Pattern(regexp = "\\d{4}", message = "Os últimos dígitos do cartão devem ter exatamente 4 números")
    private String ultimosDigitos;

    @NotBlank(message = "A bandeira do cartão é obrigatória")
    @Size(max = 30, message = "A bandeira do cartão deve ter até 30 caracteres")
    private String bandeira;

    @NotNull(message = "É obrigatório informar se o cartão está ativo")
    private Boolean ativo;

    @NotNull(message = "A data de cadastro do cartão é obrigatória")
    @PastOrPresent(message = "A data de cadastro do cartão deve estar no presente ou passado")
    private LocalDateTime dataCadastro;

    public CartaoCliente(Cliente cliente, String token, String ultimosDigitos, String bandeira) {
        this.cliente = cliente;
        this.token = token;
        this.ultimosDigitos = ultimosDigitos;
        this.bandeira = bandeira;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }
}
