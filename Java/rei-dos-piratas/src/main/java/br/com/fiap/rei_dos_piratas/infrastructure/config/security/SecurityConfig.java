package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtAuthenticationFilter;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtUtil jwtUtil,
                                                   UserDetailsService userDetailsService) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Público geral
                        .requestMatchers("/auth/**", "/error", "/health", "/",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Consulta de frete
                        .requestMatchers("/frete/**").permitAll()
                        // Visualização de produtos (API e web)
                        .requestMatchers(HttpMethod.GET, "/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/web/produtos", "/web/produtos/{id:[0-9]+}").permitAll()
                        // Login web — página pública, o token é emitido pelo /auth/login normal
                        .requestMatchers("/web/login").permitAll()
                        // Gerenciamento de carrinho
                        .requestMatchers("/carrinho/**").hasRole("CARRINHO_MANAGE")
                        // Gerenciamento de endereços
                        .requestMatchers("/enderecos/**").hasRole("ENDERECO_MANAGE")
                        // Criação e gestão de produtos (API e web)
                        .requestMatchers(HttpMethod.PUT, "/produtos/**").hasAnyRole("PRODUTO_WRITE")
                        .requestMatchers(HttpMethod.POST, "/produtos/**").hasAnyRole("PRODUTO_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/produtos/**").hasAnyRole("PRODUTO_WRITE")
                        .requestMatchers("/web/produtos/novo", "/web/produtos/save",
                                "/web/produtos/*/editar", "/web/produtos/*/excluir").hasRole("PRODUTO_WRITE")
                        // Gerenciamento funcionários
                        .requestMatchers(HttpMethod.GET, "/funcionarios/**").hasAnyRole("FUNCIONARIO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/funcionarios/**").hasRole("FUNCIONARIO_WRITE")
                        .requestMatchers(HttpMethod.POST, "/funcionarios/**").hasRole("FUNCIONARIO_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/funcionarios/**").hasRole("FUNCIONARIO_WRITE")
                        // Operações de pedidos
                        .requestMatchers(HttpMethod.PUT, "/pedidos/pagamento/**").hasRole("PEDIDO_PAGAMENTO")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/cancelamento/**").hasRole("PEDIDO_CANCEL")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/**").hasAnyRole("PEDIDO_WRITE")
                        .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAnyRole("PEDIDO_READ")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider(userDetailsService))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, userDetailsService),
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
            if (accept != null && accept.contains("text/html")) {
                response.sendRedirect("/web/login");
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://*.vercel.app",
                "https://*.render.com",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
