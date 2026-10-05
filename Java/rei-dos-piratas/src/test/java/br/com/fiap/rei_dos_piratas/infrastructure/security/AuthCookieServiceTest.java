package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthCookieServiceTest {
    @Test
    void emissaoELimpezaMantemSecureHttpOnlySameSiteEPaths() {
        JwtUtil jwt = mock(JwtUtil.class);
        when(jwt.extractExpiration("jwt")).thenReturn(Date.from(Instant.now().plusSeconds(900)));
        AuthCookieService service = new AuthCookieService(jwt, true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.emitir(new AuthResponse("jwt", null, null, List.of(), "refresh", Instant.now().plusSeconds(604800)), response);
        assertThat(response.getHeaders("Set-Cookie")).hasSize(3)
                .allMatch(value -> value.contains("Secure") && value.contains("SameSite=Strict"));
        assertThat(response.getCookie("jwt_token").isHttpOnly()).isTrue();
        assertThat(response.getCookie("refresh_token").isHttpOnly()).isTrue();
        MockHttpServletResponse logout = new MockHttpServletResponse();
        service.limpar(logout);
        assertThat(logout.getHeaders("Set-Cookie")).hasSize(3)
                .allMatch(value -> value.contains("Max-Age=0") && value.contains("Secure"));
        assertThat(logout.getCookie("jwt_token").getPath()).isEqualTo("/");
        assertThat(logout.getCookie("refresh_token").getPath()).isEqualTo("/web");
    }
}
