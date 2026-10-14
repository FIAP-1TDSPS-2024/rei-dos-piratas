package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.messaging;

public interface EventoListener {
    void onMessage(String message);
}
