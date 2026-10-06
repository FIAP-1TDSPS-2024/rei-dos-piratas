-- O relacionamento motivo_devolucao_id substituiu o campo legado motivo.
-- Preserva o historico e permite os novos INSERTs, que usam apenas o relacionamento.
ALTER TABLE DEVOLUCOES ALTER COLUMN motivo DROP NOT NULL;

-- V29 utilizava o nome do status de pedidos para devolucoes antigas.
UPDATE DEVOLUCOES SET status = 'PREPARANDO_RETORNO'
WHERE status = 'PREPARANDO_ENVIO';
