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
VALUES (
           21,
           'Jardim Guaruja',
           '05876030',
           11,
           'Rua Chaia Zingerevitz',
           TRUE,
           42,
           NULL,
           1
       );
ALTER TABLE ENDERECO ALTER COLUMN id RESTART WITH 22;
