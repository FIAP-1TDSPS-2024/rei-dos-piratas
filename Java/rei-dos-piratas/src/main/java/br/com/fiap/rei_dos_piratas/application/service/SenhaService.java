package br.com.fiap.rei_dos_piratas.application.service;

public interface SenhaService {
    String encode(String senha);
    boolean matches(String senha, String hash);
}
