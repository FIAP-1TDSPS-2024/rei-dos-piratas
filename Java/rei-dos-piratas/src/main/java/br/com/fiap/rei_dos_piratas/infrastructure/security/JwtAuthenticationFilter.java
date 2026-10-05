package br.com.fiap.rei_dos_piratas.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.springframework.lang.NonNull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService userDetailsService;
    private final br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService autenticacao;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioDetailsService userDetailsService,
                                   br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService autenticacao) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.autenticacao = autenticacao;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractJwtFromRequest(request);

        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            try {
                var identidade = jwtUtil.extractIdentidade(jwt);
                var sessaoId = jwtUtil.extractSessaoId(jwt);
                UserDetails userDetails = userDetailsService.loadUserByIdentidade(identidade);

                if (!jwtUtil.validateToken(jwt, userDetails)) {
                    log.debug("[AUTENTICACAO] Acesso recusado - sessaoId={}, usuarioId={}, tipoConta={}, motivo=CONTA_OU_TOKEN_INVALIDO",
                            sessaoId, identidade.id(), identidade.tipo());
                } else if (autenticacao.sessaoValida(sessaoId, identidade)) {
                    ((CustomUserDetails) userDetails).setSessaoId(sessaoId);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException
                     | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                SecurityContextHolder.clearContext();
                log.debug("[AUTENTICACAO] Acesso recusado - identidade ou conta indisponivel");
            }
        } else if (StringUtils.hasText(jwt)) {
            log.debug("[AUTENTICACAO] Acesso recusado - token invalido ou expirado");
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        boolean web = request.getRequestURI().startsWith("/web/");
        // API autentica exclusivamente por Bearer; cookies autenticam somente o fluxo web.
        String bearerToken = request.getHeader("Authorization");
        if (!web && StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Cookie (web/Thymeleaf pages — same JWT, stored client-side)
        if (web && request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "jwt_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
