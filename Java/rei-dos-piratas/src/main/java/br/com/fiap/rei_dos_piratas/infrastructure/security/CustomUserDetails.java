package br.com.fiap.rei_dos_piratas.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UsuarioDetails{

    private String user;
    private String password;
    private List<?  extends  GrantedAuthority> authorities;

    public CustomUserDetails(String user, String password, List<? extends GrantedAuthority> authorities) {
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
