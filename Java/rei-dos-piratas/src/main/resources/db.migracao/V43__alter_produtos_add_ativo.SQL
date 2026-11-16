-- V43: produto "excluído" passa a ser desativado. Carrinhos, pedidos e devoluções referenciam
-- PRODUTOS por chave estrangeira; a linha fica e o produto só sai das vendas novas.
-- Todos os produtos existentes continuam ativos.
ALTER TABLE PRODUTOS ADD COLUMN ativo BOOLEAN DEFAULT TRUE NOT NULL;
