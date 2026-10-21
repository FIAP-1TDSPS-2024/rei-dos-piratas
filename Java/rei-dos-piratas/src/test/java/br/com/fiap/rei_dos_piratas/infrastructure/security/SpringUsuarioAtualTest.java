package br.com.fiap.rei_dos_piratas.infrastructure.security;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringUsuarioAtualTest {

    private final SpringUsuarioAtual usuarioAtual = new SpringUsuarioAtual();

    private void autenticar(TipoConta tipo, Long id, String... roles) {
        var authorities = java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        var details = new CustomUserDetails(id, "usuario", "pwd", authorities, tipo, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePossuirPermissaoQuandoUsuarioTemARoleCorrespondente() {
        autenticar(TipoConta.FUNCIONARIO, 5L, "ROLE_PEDIDO_WRITE", "ROLE_PRODUTO_READ");

        assertThat(usuarioAtual.possuiPermissao("PEDIDO_WRITE")).isTrue();
    }

    @Test
    void naoDevePossuirPermissaoQuandoUsuarioNaoTemARole() {
        autenticar(TipoConta.CLIENTE, 1L, "ROLE_PEDIDO_READ");

        assertThat(usuarioAtual.possuiPermissao("PEDIDO_WRITE")).isFalse();
    }

    @Test
    void naoDevePossuirPermissaoQuandoNaoHaUsuarioAutenticado() {
        assertThat(usuarioAtual.possuiPermissao("PEDIDO_WRITE")).isFalse();
    }

    @Test
    void deveRetornarIdentidadeQuandoAutenticado() {
        autenticar(TipoConta.CLIENTE, 42L);

        assertThat(usuarioAtual.identidadeSeAutenticado())
                .contains(new IdentidadeConta(TipoConta.CLIENTE, 42L));
    }

    @Test
    void deveRetornarVazioQuandoNaoHaUsuarioAutenticado() {
        assertThat(usuarioAtual.identidadeSeAutenticado()).isEmpty();
    }

    @Test
    void deveRetornarVazioQuandoPrincipalNaoEUsuarioDaAplicacao() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonimo", null, List.of()));

        assertThat(usuarioAtual.identidadeSeAutenticado()).isEmpty();
    }
}
