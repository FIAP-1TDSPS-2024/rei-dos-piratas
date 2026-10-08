package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CobrancaService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.*;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CobrancaServiceImpl implements CobrancaService {

    private final CobrancaAppClient apiCobranca;

    public CobrancaServiceImpl(CobrancaAppClient apiCobranca) {
        this.apiCobranca = apiCobranca;
    }

    @Override
    public ClienteCobrancaResponseDto criarnovoClienteCobranca(ClienteCobrancaRequestDto request) {
        log.info("[COBRANCA] Criando novo cliente de cobrança na API de pagamentos Asaas.");
        try {
            ClienteCobrancaResponseDto response = this.apiCobranca.criarNovoCliente(request);
            log.info("[COBRANCA - CLIENTE] Cliente de cobrança criado com sucesso. ID: {}", response);
            return response;
        } catch (ApiExternaException e) {
            log.error("[COBRANCA - CLIENTE] Falha ao criar cliente de cobrança: {}", e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[COBRANCA - CLIENTE] Erro de comunicação ao criar cliente de cobrança: {}", e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de cobrança ao criar o cliente. Tente novamente.");
        }
    }

    @Override
    public PagamentoCobrancaResponseDto criarNovaCobranca(PagamentoCobrancaRequestDto request) {
        log.info("[COBRANCA] Criando nova cobrança na API de pagamentos Asaas.");
        try {
            PagamentoCobrancaResponseDto response = this.apiCobranca.criarNovaCobranca(request);
            log.info("[COBRANCA - PAGAMENTO] Cobrança criada com sucesso. ID: {}", response);
            return response;
        } catch (ApiExternaException e) {
            log.error("[COBRANCA - PAGAMENTO] Falha ao criar cobrança: {}", e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[COBRANCA - PAGAMENTO] Erro de comunicação ao criar cobrança: {}", e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de cobrança ao criar a cobrança. Tente novamente.");
        }
    }

    @Override
    public PixQrCodeResponseDto obterQrCodePix(String id) {
        log.info("[COBRANCA - PIX] Obtendo QR Code Pix da cobrança ID: {}.", id);
        try {
            PixQrCodeResponseDto response = this.apiCobranca.obterQrCodePix(id);
            log.info("[COBRANCA - PIX] QR Code Pix obtido com sucesso para a cobrança ID: {}.", id);
            return response;
        } catch (ApiExternaException e) {
            log.error("[COBRANCA - PIX] Falha ao obter QR Code Pix da cobrança {}: {}", id, e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("[COBRANCA - PIX] Erro de comunicação ao obter QR Code Pix da cobrança {}: {}", id, e.getMessage());
            throw new ApiExternaException("Falha de comunicação com a API de cobrança ao obter o QR Code Pix. Tente novamente.");
        }
    }
}
