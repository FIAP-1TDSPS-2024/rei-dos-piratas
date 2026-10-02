package br.com.fiap.rei_dos_piratas.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsuarioDetailsService extends UserDetailsService {
    org.springframework.security.core.userdetails.UserDetails loadUserByIdentidade(
            br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta identidade);
}
