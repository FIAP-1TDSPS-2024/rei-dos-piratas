package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.application.service.RefreshTokenService;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final SecureRandom random = new SecureRandom();

    public String gerar(UUID sessaoId) {
        byte[] segredo = new byte[32];
        random.nextBytes(segredo);
        return sessaoId + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(segredo);
    }

    public String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    public UUID identificarSessao(String token) {
        if (token == null || !token.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.[A-Za-z0-9_-]{43}")) {
            throw new CredenciaisInvalidasException();
        }
        return UUID.fromString(token.substring(0, 36));
    }
}
