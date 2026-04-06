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
           'Jardim Guaruja',
           '05876030',
           11,
           'Rua Chaia Zingerevitz',
           1,
           42,
           NULL,
           1
       );