package br.com.fiap.rei_dos_piratas.interfaces.controller;

public interface RastreioController {
    void rastreioWebhook(String signature, String rawBody);
}

