-- V41: Dados do boleto no pagamento: URL do PDF (Asaas bankSlipUrl) e vencimento da cobrança (boleto/PIX)
ALTER TABLE PAGAMENTOS ADD url_boleto VARCHAR(500);
ALTER TABLE PAGAMENTOS ADD data_vencimento DATE;
