package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface CobrancaService {
    ClienteCobrancaResponseDto criarnovoClienteCobranca(ClienteCobrancaRequestDto request);
    PagamentoCobrancaResponseDto criarNovaCobranca(PagamentoCobrancaRequestDto request);
    PixQrCodeResponseDto obterQrCodePix(String id);
}
