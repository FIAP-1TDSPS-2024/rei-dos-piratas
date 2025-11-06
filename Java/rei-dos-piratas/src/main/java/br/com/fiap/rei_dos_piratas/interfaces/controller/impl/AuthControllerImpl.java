package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

public class AuthControllerImpl implements AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;

    public AuthControllerImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ClienteService clienteService, FuncionarioService funcionarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        //verifica se o usuário do funcionário está ativo para fazer o login
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_USER")) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Funcionario funcionario = this.funcionarioService.findById(userDetails.getId());
            if (!funcionario.isUsuarioAtivo()){
                throw new RegraDeNegocioException("O usuário deve estar ativo para o login ser acessado. Contate seu supervisor para ativá-lo novamente");
            }
        }

        String token = jwtUtil.generateToken(authentication);

        return new AuthResponse(token, request.username(), null, roles);
    }

    @Override
    public ClienteOutDto cadastrar(ClienteInDto clienteInDto) {
        Cliente cliente = ClienteDtoMapper.toEntity(clienteInDto);
        Cliente novoCliente = this.clienteService.create(cliente);
        return ClienteDtoMapper.toDto(novoCliente);
    }
}
