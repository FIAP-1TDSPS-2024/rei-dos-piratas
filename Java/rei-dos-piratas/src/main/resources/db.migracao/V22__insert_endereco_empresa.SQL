-- V22: Dados iniciais - Endereço da empresa (id = 21, vinculado à empresa id = 1)
INSERT INTO ENDERECO (
    id,
    bairro,
    cep,
    numero,
    logradouro,
    endereco_ativo,
    cidade_id,
    cliente_id,
    empresa_id
)
    OVERRIDING SYSTEM VALUE
VALUES (
    21,
    'Jardim Guarujá',
    '05876030',
    11,
    'Rua Chaia Zingerevitz',
    1,
    42,
    NULL,
    1
);

