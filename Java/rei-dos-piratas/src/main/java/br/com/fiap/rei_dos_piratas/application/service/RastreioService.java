package br.com.fiap.rei_dos_piratas.application.service;

public interface RastreioService {
    void rastreioWebhook(String signature, String rawBody);
}
