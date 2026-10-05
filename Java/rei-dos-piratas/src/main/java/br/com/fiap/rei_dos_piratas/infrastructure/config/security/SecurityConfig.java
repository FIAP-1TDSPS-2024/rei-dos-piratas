package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtAuthenticationFilter;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService;
import br.com.fiap.rei_dos_piratas.infrastructure.security.AuthCookieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    @Bean
    public CsrfTokenRepository csrfTokenRepository(@Value("${app.auth.cookie-secure:true}") boolean secure) {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookiePath("/web");
        repository.setCookieCustomizer(cookie -> cookie.httpOnly(true).secure(secure).sameSite("Strict"));
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtUtil jwtUtil,
                                                   br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService userDetailsService,
                                                   AutenticacaoService autenticacao, CsrfTokenRepository csrfTokens,
                                                   @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource")
                                                   CorsConfigurationSource corsSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsSource))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens)
                        .requireCsrfProtectionMatcher(request -> request.getRequestURI().startsWith("/web/")
                                && CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request)))
                .authorizeHttpRequests(auth -> auth
                        // Público geral
                        .requestMatchers("/auth/login", "/auth/cadastro", "/auth/refresh", "/error", "/health", "/",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/rastreio/webhook").permitAll()
                        // Logout precisa de autenticação mas deve ser acessível sem session
                        .requestMatchers("/auth/logout").authenticated()
                        // Consulta de frete
                        .requestMatchers("/frete/**").permitAll()
                        // Visualização de produtos (API e web)
                        .requestMatchers(HttpMethod.GET, "/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/web/produtos", "/web/produtos/{id:[0-9]+}").permitAll()
                        // Autenticacao web publica; POSTs continuam protegidos por CSRF.
                        .requestMatchers("/web/login", "/web/renovar", "/web/auth/**", "/assets/**").permitAll()
                        // Logout web — precisa estar autenticado (o filtro extrai o token do cookie)
                        .requestMatchers("/web/logout").authenticated()
                        // Gerenciamento de carrinho
                        .requestMatchers("/carrinho/**").hasRole("ENDERECO_MANAGE")
                        // Gerenciamento de endereços
                        .requestMatchers("/enderecos/**").hasRole("ENDERECO_MANAGE")
                        // Criação e gestão de produtos (API e web)
                        .requestMatchers(HttpMethod.PUT, "/produtos/**").hasAnyRole("PRODUTO_WRITE")
                        .requestMatchers(HttpMethod.POST, "/produtos/**").hasAnyRole("PRODUTO_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/produtos/**").hasAnyRole("PRODUTO_WRITE")
                        .requestMatchers("/web/produtos/novo", "/web/produtos/save",
                                "/web/produtos/*/editar", "/web/produtos/*/excluir").hasRole("PRODUTO_WRITE")
                        // Gerenciamento funcionários
                        .requestMatchers(HttpMethod.PUT, "/funcionarios/**").hasRole("FUNCIONARIO_WRITE")
                        .requestMatchers(HttpMethod.POST, "/funcionarios/**").hasRole("FUNCIONARIO_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/funcionarios/**").hasRole("FUNCIONARIO_WRITE")
                        // Operações de pedidos
                        .requestMatchers(HttpMethod.POST, "/pedidos/**").hasRole("PEDIDO_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/pagamento/**").hasRole("PEDIDO_PAGAMENTO")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/cancelamento/**").hasRole("PEDIDO_CANCEL")
                        .requestMatchers(HttpMethod.GET, "/pedidos/status/**").hasRole("PEDIDO_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/**").hasAnyRole("PEDIDO_WRITE")
                        .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAnyRole("PEDIDO_READ")
                        // Painel web de pedidos para funcionários
                        .requestMatchers("/web/pedidos/**").hasRole("PEDIDO_WRITE")
                        // Operações de devoluções
                        .requestMatchers(HttpMethod.POST, "/devolucoes/**").hasRole("PEDIDO_READ")
                        .requestMatchers(HttpMethod.GET, "/devolucoes/**").hasAnyRole("PEDIDO_READ", "PEDIDO_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/devolucoes/**").hasRole("PEDIDO_WRITE")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider(userDetailsService))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, userDetailsService, autenticacao),
                        UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            // Requisições web (Thymeleaf): redireciona para login
            String accept = request.getHeader("Accept");
            if (request.getRequestURI().startsWith("/web/") && accept != null && accept.contains("text/html")) {
                if ("GET".equals(request.getMethod()) && AuthCookieService.ler(request, "refresh_token") != null) {
                    String destino = request.getRequestURI();
                    if (request.getQueryString() != null) destino += "?" + request.getQueryString();
                    response.sendRedirect("/web/renovar?destino=" + URLEncoder.encode(destino, StandardCharsets.UTF_8));
                } else response.sendRedirect("/web/login");
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:8080}") String origens) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(origens.split(",")).map(String::trim)
                .filter(origem -> !origem.isEmpty()).toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        // O fluxo web e same-origin; CORS configuravel e reservado aos clientes da API.
        return request -> request.getRequestURI().startsWith("/web/")
                ? new CorsConfiguration() : source.getCorsConfiguration(request);
    }
}
