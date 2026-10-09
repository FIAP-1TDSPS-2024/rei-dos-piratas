-- V36: Adiciona tipo de pagamento, valor liquido e ID da cobranca (API de pagamentos) em PEDIDOS
ALTER TABLE PEDIDOS ADD tipo_pagamento VARCHAR(50);
ALTER TABLE PEDIDOS ADD valor_liquido  NUMERIC(12,2);
ALTER TABLE PEDIDOS ADD id_cobranca    VARCHAR(50);
