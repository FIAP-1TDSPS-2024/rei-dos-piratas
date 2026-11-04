package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Estorno do pedido para o cliente acompanhar. No boleto, {@code urlEstornoBoleto} é onde ele informa os dados
 * bancários. O motivo de falha não sai da API: é mensagem interna do fornecedor (ex.: saldo da loja).
 */
public record EstornoOutDto(
        Long id,
        OrigemEstornoEnum origem,
        Long devolucaoId,
        BigDecimal valor,
        StatusEstornoEnum status,
        String urlEstornoBoleto,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataConclusao
) {}
