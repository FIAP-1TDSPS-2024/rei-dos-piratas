-- V19: Dados iniciais - Estado de São Paulo
-- ID fixo usado pelos dados iniciais de cidade.
INSERT INTO ESTADOS (id, estado_nome, estado_sigla)
VALUES (1, 'São Paulo', 'SP');
ALTER TABLE ESTADOS ALTER COLUMN id RESTART WITH 2;
