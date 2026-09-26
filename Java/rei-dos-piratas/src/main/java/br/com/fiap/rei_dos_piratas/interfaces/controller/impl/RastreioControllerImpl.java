package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.RastreioController;

public class RastreioControllerImpl implements RastreioController {

    private final RastreioService rastreioService;

    public RastreioControllerImpl(RastreioService rastreioService) {
        this.rastreioService = rastreioService;
    }

    @Override
    public void rastreioWebhook(String signature, String rawBody) {
        this.rastreioService.rastreioWebhook(signature, rawBody);
    }
}

