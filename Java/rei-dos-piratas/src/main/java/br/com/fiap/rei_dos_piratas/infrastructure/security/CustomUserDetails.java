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

    public CustomUserDetails(Long id, String user, String password, List<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.user = user;
        this.password = password;
        this.authorities = authorities;
    }

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
