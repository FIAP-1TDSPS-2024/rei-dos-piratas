-- V39: Cria o pagamento dos pedidos que ja tinham cobranca no Asaas (campos de pagamento em PEDIDOS, V36).
-- Idempotente: so insere para pedidos que ainda nao tem pagamento.
-- Os campos tipo_pagamento/id_cobranca/valor_liquido de PEDIDOS so devem ser removidos em migration futura,
-- depois que o codigo deixar de usa-los.
INSERT INTO PAGAMENTOS (pedido_id, tipo, status, id_cobranca, valor, valor_liquido, parcelas, data_criacao)
SELECT p.id,
       p.tipo_pagamento,
       CASE p.status
           WHEN 'AGUARDANDO_PAGAMENTO' THEN 'PENDENTE'
           WHEN 'CANCELADO' THEN 'CANCELADO'
           ELSE 'RECEBIDO'
       END,
       p.id_cobranca,
       p.valor_total,
       p.valor_liquido,
       1,
       CAST(p.data_pedido AS TIMESTAMP)
FROM PEDIDOS p
WHERE p.id_cobranca IS NOT NULL
  AND p.tipo_pagamento IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM PAGAMENTOS pg WHERE pg.pedido_id = p.id);
