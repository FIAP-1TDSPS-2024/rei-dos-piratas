package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Service
public class AuthCookieService {
    private final JwtUtil jwt;
    private final boolean secure;
    public AuthCookieService(JwtUtil jwt, @Value("${app.auth.cookie-secure:true}") boolean secure) {
        this.jwt = jwt;
        this.secure = secure;
    }

    public void emitir(AuthResponse auth, HttpServletResponse response) {
        Instant accessExpira = jwt.extractExpiration(auth.token()).toInstant();
        Duration refreshPrazo = Duration.between(Instant.now(), auth.refreshExpiraEm());
        adicionar(response, "jwt_token", auth.token(), Duration.between(Instant.now(), accessExpira), true, "/");
        adicionar(response, "refresh_token", auth.refreshToken(), refreshPrazo, true, "/web");
        // Apenas a data de expiracao e visivel ao JS, nunca os tokens.
        adicionar(response, "auth_expires_at", Long.toString(accessExpira.toEpochMilli()), refreshPrazo, false, "/web");
    }

    public void limpar(HttpServletResponse response) {
        adicionar(response, "jwt_token", "", Duration.ZERO, true, "/");
        adicionar(response, "refresh_token", "", Duration.ZERO, true, "/web");
        adicionar(response, "auth_expires_at", "", Duration.ZERO, false, "/web");
    }

    public String refresh(HttpServletRequest request) { return ler(request, "refresh_token"); }

    public static String ler(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(cookie -> nome.equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue).findFirst().orElse(null);
    }

    private void adicionar(HttpServletResponse response, String nome, String valor, Duration prazo,
                           boolean httpOnly, String path) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(nome, valor).path(path)
                .httpOnly(httpOnly).secure(secure).sameSite("Strict")
                .maxAge(prazo.isNegative() ? Duration.ZERO : prazo).build().toString());
    }
}
