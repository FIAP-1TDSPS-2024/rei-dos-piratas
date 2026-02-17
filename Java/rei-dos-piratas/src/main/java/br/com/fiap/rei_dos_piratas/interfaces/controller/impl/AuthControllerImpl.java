package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.LoginRequest;
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
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
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

        Cliente cliente = this.clienteService.findByEmail(request.email());
        if (cliente == null || !cliente.isUsuarioAtivo()) {
            throw new RegraDeNegocioException("O usuário deve estar ativo para o login ser acessado. Contate o suporte para ativá-lo novamente");
        }

        String token = jwtUtil.generateToken(authentication);
        ClienteOutDto clienteOutDto = ClienteDtoMapper.toDto(cliente);

        return new AuthResponse(token, clienteOutDto, roles);
    }

    @Override
    public AuthResponse cadastrar(ClienteInDto clienteInDto) {
        Cliente cliente = ClienteDtoMapper.toEntity(clienteInDto);
        Cliente novoCliente = this.clienteService.create(cliente);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(clienteInDto.email(), clienteInDto.senha())
        );

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(authentication);
        ClienteOutDto clienteOutDto = ClienteDtoMapper.toDto(novoCliente);

        return new AuthResponse(token, clienteOutDto, roles);
    }
}
