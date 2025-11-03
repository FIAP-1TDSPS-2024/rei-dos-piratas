package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class AuthControllerImpl implements AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ClienteService clienteService;

    public AuthControllerImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ClienteService clienteService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.clienteService = clienteService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtUtil.generateToken(authentication);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new AuthResponse(token, request.username(), null, roles);
    }

    @Override
    public ClienteOutDto cadastrar(ClienteInDto clienteInDto) {
        Cliente cliente = ClienteDtoMapper.toEntity(clienteInDto);
        Cliente novoCliente = this.clienteService.create(cliente);
        return ClienteDtoMapper.toDto(novoCliente);
    }
}
