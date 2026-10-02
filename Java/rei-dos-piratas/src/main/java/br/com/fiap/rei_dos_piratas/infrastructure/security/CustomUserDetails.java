package br.com.fiap.rei_dos_piratas.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UsuarioDetails{

    @Getter
    private Long id;
    private String user;
    private String password;
    private List<?  extends  GrantedAuthority> authorities;
    @Getter
    private br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta tipo;
    private boolean ativo;

    public CustomUserDetails(Long id, String user, String password, List<? extends GrantedAuthority> authorities) {
        this(id, user, password, authorities, br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta.CLIENTE, true);
    }

    public CustomUserDetails(Long id, String user, String password, List<? extends GrantedAuthority> authorities,
                             br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta tipo, boolean ativo) {
        this.id = id;
        this.user = user;
        this.password = password;
        this.authorities = authorities;
        this.tipo = tipo;
        this.ativo = ativo;
    }

    @Override
    public boolean isEnabled() { return ativo; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.user;
    }
}
