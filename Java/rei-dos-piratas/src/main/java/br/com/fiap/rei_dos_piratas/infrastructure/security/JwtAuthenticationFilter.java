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

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService userDetailsService;
    private final TokenBlocklistService tokenBlocklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioDetailsService userDetailsService, TokenBlocklistService tokenBlocklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractJwtFromRequest(request);

        // Rejeita token presente na blocklist (logout explícito)
        if (StringUtils.hasText(jwt) && !tokenBlocklistService.estaInvalidado(jwt) && jwtUtil.validateToken(jwt)) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByIdentidade(jwtUtil.extractIdentidade(jwt));

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException
                     | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. Authorization header (API clients)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Cookie (web/Thymeleaf pages — same JWT, stored client-side)
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "jwt_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
