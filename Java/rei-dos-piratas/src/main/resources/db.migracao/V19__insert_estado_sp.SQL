-- V19: Dados iniciais - Estado de São Paulo
-- OVERRIDING SYSTEM VALUE permite forçar o id na sequence do Oracle
INSERT INTO ESTADOS (id, estado_nome, estado_sigla)
    OVERRIDING SYSTEM VALUE
    VALUES (1, 'São Paulo', 'SP');
