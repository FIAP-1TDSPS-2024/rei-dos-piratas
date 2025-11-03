package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.LoginRequest;
import jakarta.validation.Valid;

public interface AuthController {
    AuthResponse login(LoginRequest loginRequest);

    ClienteOutDto cadastrar(ClienteInDto clienteInDto);
}
