package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptSenhaService implements SenhaService {
    private final PasswordEncoder encoder;

    public BCryptSenhaService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String senha) { return encoder.encode(senha); }
    public boolean matches(String senha, String hash) { return encoder.matches(senha, hash); }
}
