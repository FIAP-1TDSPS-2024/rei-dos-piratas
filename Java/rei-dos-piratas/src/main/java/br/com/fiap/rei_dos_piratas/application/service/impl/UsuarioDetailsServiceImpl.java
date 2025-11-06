package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public class UsuarioDetailsServiceImpl implements UsuarioDetailsService {

    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;

    public UsuarioDetailsServiceImpl(ClienteRepository clienteRepository, FuncionarioRepository funcionarioRepository) {
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByUsername(username);
        if (cliente != null) {
            return new CustomUserDetails(
                    cliente.getId(),
                    cliente.getUsername(),
                    cliente.getPassword(),
                    List.of(new SimpleGrantedAuthority(cliente.getRole().toString())));
        }

        Funcionario funcionario = funcionarioRepository.findByUsername(username);
        if (funcionario != null) {
            return new CustomUserDetails(
                    funcionario.getId(),
                    funcionario.getUsername(),
                    funcionario.getPassword(),
                    List.of(new SimpleGrantedAuthority(funcionario.getRole().toString())));
        }

        throw new UsernameNotFoundException("Usuário não encontrado");
    }

}
