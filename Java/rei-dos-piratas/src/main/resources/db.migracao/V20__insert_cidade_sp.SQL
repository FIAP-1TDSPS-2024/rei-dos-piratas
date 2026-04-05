-- V20: Dados iniciais - Cidade de São Paulo (id = 42, vinculada ao estado SP id = 1)
INSERT INTO CIDADES (id, cidade_nome, estado_id)
    OVERRIDING SYSTEM VALUE
    VALUES (42, 'São Paulo', 1);

