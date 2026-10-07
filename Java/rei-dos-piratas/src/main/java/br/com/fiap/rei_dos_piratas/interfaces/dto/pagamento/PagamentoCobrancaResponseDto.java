package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import br.com.fiap.rei_dos_piratas.domain.Enum.AsaasPaymentStatus;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoCobrancaResponseDto(
        String object,
        String id,
        LocalDate dateCreated,
        String customer,
        String checkoutSession,
        String paymentLink,
        BigDecimal value,
        BigDecimal netValue,
        BigDecimal originalValue,
        BigDecimal interestValue,
        String description,
        TipoPagamentoEnum billingType,
        String pixTransaction,
        AsaasPaymentStatus status,
        LocalDate dueDate,
        LocalDate originalDueDate,
        LocalDate paymentDate,
        LocalDate clientPaymentDate,
        Integer installmentNumber,
        String invoiceUrl,
        String invoiceNumber,
        String externalReference,
        Boolean deleted,
        Boolean anticipated,
        Boolean anticipable,
        LocalDate creditDate,
        LocalDate estimatedCreditDate,
        String transactionReceiptUrl,
        String nossoNumero,
        String bankSlipUrl,
        LocalDate lastInvoiceViewedDate,
        LocalDate lastBankSlipViewedDate
)
{}
