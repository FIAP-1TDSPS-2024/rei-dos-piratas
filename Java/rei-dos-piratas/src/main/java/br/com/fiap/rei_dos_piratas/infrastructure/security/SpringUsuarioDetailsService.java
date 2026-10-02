package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class SpringUsuarioDetailsService implements UsuarioDetailsService {
    private final ContaRepository contas;

    public SpringUsuarioDetailsService(ContaRepository contas) { this.contas = contas; }

    public UserDetails loadUserByUsername(String email) {
        return adaptar(contas.findByEmail(Usuario.normalizarEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Conta nao encontrada")));
    }

    public UserDetails loadUserByIdentidade(IdentidadeConta identidade) {
        return adaptar(contas.findByIdentidade(identidade)
                .orElseThrow(() -> new UsernameNotFoundException("Conta nao encontrada")));
    }

    private CustomUserDetails adaptar(Conta conta) {
        return new CustomUserDetails(conta.identidade().id(), conta.usuario().getEmail(),
                conta.usuario().getSenha(), conta.usuario().getPermissoes().stream()
                .map(nome -> new SimpleGrantedAuthority("ROLE_" + nome)).toList(),
                conta.identidade().tipo(), conta.usuario().isUsuarioAtivo());
    }
}
