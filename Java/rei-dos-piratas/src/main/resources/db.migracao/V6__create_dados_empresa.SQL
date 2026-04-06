-- V6: Criação da tabela DADOS_EMPRESA (registro único, id fixo = 1)
CREATE TABLE DADOS_EMPRESA (
    id                      NUMBER PRIMARY KEY,
    razao_social            VARCHAR2(255),
    nome_fantasia           VARCHAR2(255),
    cnpj                    VARCHAR2(14),
    email                   VARCHAR2(255),
    telefone                VARCHAR2(255),
    dominio                 VARCHAR2(255),
    state_abbr              VARCHAR2(255),
    state_register          VARCHAR2(255),
    economic_activity_code  VARCHAR2(255)
);

