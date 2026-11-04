package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.time.LocalDateTime;

public record QrCodePix(Boolean sucesso, String imagemBase64, String copiaECola, LocalDateTime expiraEm, String descricao) {}
