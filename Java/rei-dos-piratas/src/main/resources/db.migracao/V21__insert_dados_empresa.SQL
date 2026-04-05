-- V21: Dados iniciais - Dados da empresa Rei dos Piratas
-- id fixo = 1 (registro único conforme regra de negócio)
INSERT INTO DADOS_EMPRESA (
    id,
    cnpj,
    dominio,
    email,
    nome_fantasia,
    razao_social,
    telefone,
    economic_activity_code,
    state_abbr,
    state_register
) VALUES (
    1,
    '53042434000110',
    'reidospiratas.com.br',
    'contato@reidospiratas.com.br',
    'Rei dos Piratas',
    'Rei dos Piratas Comércio Ltda',
    '11987654321',
    '4711302',
    'SP',
    '110042490114'
);

