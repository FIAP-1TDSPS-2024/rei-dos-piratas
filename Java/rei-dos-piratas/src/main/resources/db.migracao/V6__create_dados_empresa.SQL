-- V6: Criação da tabela DADOS_EMPRESA (registro único, id fixo = 1)
CREATE TABLE DADOS_EMPRESA (
    id                      BIGINT PRIMARY KEY,
    razao_social            VARCHAR(255),
    nome_fantasia           VARCHAR(255),
    cnpj                    VARCHAR(14),
    email                   VARCHAR(255),
    telefone                VARCHAR(255),
    dominio                 VARCHAR(255),
    state_abbr              VARCHAR(255),
    state_register          VARCHAR(255),
    economic_activity_code  VARCHAR(255)
);

