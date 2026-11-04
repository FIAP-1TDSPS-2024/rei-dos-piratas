package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

/** Motivo que o cliente escolhe ao pedir devolução. */
public record MotivoDevolucaoOutDto(Long id, String codigo, String descricao, Boolean arrependimento) {}
