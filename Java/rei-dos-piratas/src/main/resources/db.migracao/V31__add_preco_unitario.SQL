-- Migracao para banco vazio, sem necessidade de preencher registros anteriores.
ALTER TABLE PEDIDO_PRODUTO ADD COLUMN preco_unitario NUMERIC(12,2) NOT NULL;
ALTER TABLE DEVOLUCAO_ITENS ADD COLUMN preco_unitario NUMERIC(12,2) NOT NULL;
ALTER TABLE CARRINHO_PRODUTO ADD COLUMN preco_unitario NUMERIC(12,2) NOT NULL;

ALTER TABLE PEDIDO_PRODUTO ADD CONSTRAINT ck_pedido_preco_unitario CHECK (preco_unitario >= 0);
ALTER TABLE DEVOLUCAO_ITENS ADD CONSTRAINT ck_devolucao_preco_unitario CHECK (preco_unitario >= 0);
ALTER TABLE CARRINHO_PRODUTO ADD CONSTRAINT ck_carrinho_preco_unitario CHECK (preco_unitario >= 0);
