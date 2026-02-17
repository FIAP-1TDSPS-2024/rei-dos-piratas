package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.LoginRequest;

public interface AuthController {
    AuthResponse login(LoginRequest loginRequest);

    AuthResponse cadastrar(ClienteInDto clienteInDto);
}
