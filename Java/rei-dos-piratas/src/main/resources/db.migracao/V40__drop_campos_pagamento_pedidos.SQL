-- V40: Remove de PEDIDOS os campos de pagamento (V36). Os dados foram copiados para PAGAMENTOS pela V39.
ALTER TABLE PEDIDOS DROP COLUMN tipo_pagamento;
ALTER TABLE PEDIDOS DROP COLUMN valor_liquido;
ALTER TABLE PEDIDOS DROP COLUMN id_cobranca;
