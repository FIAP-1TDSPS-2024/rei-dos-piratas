package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterLogTest {
    @Test
    void tokenInvalidoGeraDiagnosticoSemSegredosEChamadaAnonimaNaoGeraLog() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(JwtAuthenticationFilter.class);
        Level anterior = logger.getLevel();
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(logs);
        try {
            JwtUtil jwt = mock(JwtUtil.class);
            var filter = new JwtAuthenticationFilter(jwt, mock(UsuarioDetailsService.class), mock(AutenticacaoService.class));
            FilterChain chain = mock(FilterChain.class);
            var request = new MockHttpServletRequest("GET", "/pedidos");
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertThat(logs.list).isEmpty();

            var autenticada = new MockHttpServletRequest("GET", "/pedidos");
            autenticada.addHeader("Authorization", "Bearer jwt-super-secreto");
            when(jwt.validateToken("jwt-super-secreto")).thenReturn(false);
            filter.doFilter(autenticada, response, chain);
            assertThat(logs.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                assertThat(event.getFormattedMessage()).contains("[AUTENTICACAO] Acesso recusado")
                        .doesNotContain("jwt-super-secreto", "Authorization", "Bearer");
                assertThat(event.getArgumentArray()).isNull();
                assertThat(event.getThrowableProxy()).isNull();
            });
            verify(chain).doFilter(request, response);
            verify(chain).doFilter(autenticada, response);
        } finally {
            logger.detachAppender(logs);
            logs.stop();
            logger.setLevel(anterior);
        }
    }
}
