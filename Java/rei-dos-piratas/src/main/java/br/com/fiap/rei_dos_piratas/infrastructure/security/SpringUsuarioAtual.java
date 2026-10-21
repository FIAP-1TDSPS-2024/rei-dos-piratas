package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringUsuarioAtual implements UsuarioAtualService {
    public java.util.UUID sessaoId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails details)
                || details.getSessaoId() == null) throw new CredenciaisInvalidasException();
        return details.getSessaoId();
    }

    public IdentidadeConta identidade() {
        return identidadeSeAutenticado().orElseThrow(CredenciaisInvalidasException::new);
    }

    @Override
    public boolean possuiPermissao(String permissao) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String role = "ROLE_" + permissao;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    @Override
    public java.util.Optional<IdentidadeConta> identidadeSeAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new IdentidadeConta(details.getTipo(), details.getId()));
    }
}
