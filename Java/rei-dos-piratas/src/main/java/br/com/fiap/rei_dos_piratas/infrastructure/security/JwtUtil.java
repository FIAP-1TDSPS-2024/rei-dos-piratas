package br.com.fiap.rei_dos_piratas.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil implements br.com.fiap.rei_dos_piratas.application.service.TokenAcessoService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:900000}")
    private Long expiration;

    private SecretKey signingKey;

    @Override
    public String emitir(br.com.fiap.rei_dos_piratas.domain.entity.Conta conta,
                         br.com.fiap.rei_dos_piratas.domain.entity.Sessao sessao) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", conta.identidade().tipo().name());
        claims.put("uid", conta.identidade().id());
        claims.put("sid", sessao.getId().toString());
        long restante = sessao.getExpiraEm().toEpochMilli() - System.currentTimeMillis();
        return createToken(claims, conta.identidade().subject(), Math.min(expiration, restante));
    }

    public java.util.UUID extractSessaoId(String token) {
        String id = extractAllClaims(token).get("sid", String.class);
        if (id == null) throw new IllegalArgumentException("Token sem sessao");
        return java.util.UUID.fromString(id);
    }

    public br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta extractIdentidade(String token) {
        Claims claims = extractAllClaims(token);
        String tipoClaim = claims.get("tipo", String.class);
        Number id = claims.get("uid", Number.class);
        if (tipoClaim == null || id == null) throw new IllegalArgumentException("Token sem identidade de conta");
        var tipo = br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta.valueOf(tipoClaim);
        var identidade = new br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta(tipo, id.longValue());
        if (!identidade.subject().equals(claims.getSubject())) throw new IllegalArgumentException("Identidade invalida");
        return identidade;
    }

    @PostConstruct
    void init() {
        // Expecting BASE64-encoded key material
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (!(userDetails instanceof CustomUserDetails details) || !details.isEnabled()) return false;
            var identidade = extractIdentidade(token);
            return identidade.id().equals(details.getId()) && identidade.tipo() == details.getTipo() && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
