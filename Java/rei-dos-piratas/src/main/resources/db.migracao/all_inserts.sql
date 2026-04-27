-- =====================================================================
-- Consolidated INSERT operations from all migrations in db.migracao/
-- Source migrations: V16..V24
-- Order preserved to respect foreign key dependencies.
-- =====================================================================

-- ---------------------------------------------------------------------
-- V16: Roles do sistema
-- ---------------------------------------------------------------------
INSERT INTO ROLES (nome, descricao) VALUES ('FUNCIONARIO_READ',  'Leitura de dados de funcionários');
INSERT INTO ROLES (nome, descricao) VALUES ('FUNCIONARIO_WRITE', 'Criação e edição de funcionários');
INSERT INTO ROLES (nome, descricao) VALUES ('PRODUTO_READ',      'Leitura de produtos');
INSERT INTO ROLES (nome, descricao) VALUES ('PRODUTO_WRITE',     'Criação e edição de produtos');
INSERT INTO ROLES (nome, descricao) VALUES ('CARRINHO_MANAGE',   'Gerenciamento de carrinho');
INSERT INTO ROLES (nome, descricao) VALUES ('PEDIDO_READ',       'Leitura de pedidos');
INSERT INTO ROLES (nome, descricao) VALUES ('PEDIDO_WRITE',      'Criação e edição de pedidos');
INSERT INTO ROLES (nome, descricao) VALUES ('PEDIDO_MANAGE',     'Gerenciamento avançado de pedidos');
INSERT INTO ROLES (nome, descricao) VALUES ('PEDIDO_PAGAMENTO',  'Operações de pagamento de pedidos');
INSERT INTO ROLES (nome, descricao) VALUES ('PEDIDO_CANCEL',     'Cancelamento de pedidos');
INSERT INTO ROLES (nome, descricao) VALUES ('ENDERECO_MANAGE',   'Gerenciamento de endereços');

-- ---------------------------------------------------------------------
-- V17: Perfis do sistema
-- ---------------------------------------------------------------------
INSERT INTO PERFIS (nome, descricao) VALUES ('ADMIN',      'Administrador com acesso total ao sistema');
INSERT INTO PERFIS (nome, descricao) VALUES ('FUNCIONARIO','Funcionário com acesso a operações internas');
INSERT INTO PERFIS (nome, descricao) VALUES ('CLIENT',     'Cliente com acesso a compras e pedidos');

-- ---------------------------------------------------------------------
-- V18: Associação de Roles aos Perfis
-- ---------------------------------------------------------------------

-- ADMIN: acesso total (todas as roles)
INSERT INTO PERFIL_ROLES (perfil_id, role_id)
    SELECT p.id, r.id FROM PERFIS p, ROLES r
    WHERE p.nome = 'ADMIN';

-- FUNCIONARIO: acesso interno operacional
INSERT INTO PERFIL_ROLES (perfil_id, role_id)
    SELECT p.id, r.id FROM PERFIS p, ROLES r
    WHERE p.nome = 'FUNCIONARIO'
      AND r.nome IN (
        'FUNCIONARIO_READ',
        'PRODUTO_READ',
        'PRODUTO_WRITE',
        'PEDIDO_READ',
        'PEDIDO_WRITE',
        'PEDIDO_MANAGE',
        'PEDIDO_PAGAMENTO',
        'PEDIDO_CANCEL'
      );

-- CLIENT: acesso a compras e pedidos próprios
INSERT INTO PERFIL_ROLES (perfil_id, role_id)
    SELECT p.id, r.id FROM PERFIS p, ROLES r
    WHERE p.nome = 'CLIENT'
      AND r.nome IN (
        'CARRINHO_MANAGE',
        'PEDIDO_READ',
        'PEDIDO_PAGAMENTO',
        'PEDIDO_CANCEL',
        'ENDERECO_MANAGE'
      );

-- ---------------------------------------------------------------------
-- V19: Estado de São Paulo
-- ---------------------------------------------------------------------
INSERT INTO ESTADOS (id, estado_nome, estado_sigla)
VALUES (1, 'São Paulo', 'SP');

-- ---------------------------------------------------------------------
-- V20: Cidade de São Paulo
-- ---------------------------------------------------------------------
INSERT INTO CIDADES (id, cidade_nome, estado_id)
VALUES (42, 'São Paulo', 1);

-- ---------------------------------------------------------------------
-- V21: Dados da empresa Rei dos Piratas
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- V22: Endereço da empresa
-- ---------------------------------------------------------------------
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
    1,
    42,
    NULL,
    1
);

-- ---------------------------------------------------------------------
-- V23: Usuário admin inicial
-- Senha: admin123  ->  BCrypt
-- ---------------------------------------------------------------------
INSERT INTO FUNCIONARIOS (
    id,
    user_name,
    nome_completo,
    email,
    senha,
    usuario_ativo,
    data_cadastro,
    perfil_id,
    data_demissao,
    salario
)
VALUES (
    1,
    'admin',
    'Administrador do Sistema',
    'admin@reidospiratas.com.br',
    '$2a$12$.yviqjcXUtK7OJd6/CgBUu43wCCiyH9.PlcDZIXyXigL6dwAbGS/C',
    1,
    now(),
    (SELECT id FROM PERFIS WHERE nome = 'ADMIN'),
    NULL,
    0
);

-- ---------------------------------------------------------------------
-- V24: Primeiro produto
-- ---------------------------------------------------------------------
INSERT INTO produtos (
    ALTURA,
    CATEGORIA,
    CONDICAO,
    ESTOQUE,
    LARGURA,
    PESO,
    PRECO,
    PRECO_ORIGINAL,
    PROFUNDIDADE,
    FUNCIONARIO_ID,
    ID,
    AUTOR,
    NOME,
    DESCRICAO,
    ENDERECO_IMAGEM
)
VALUES (
    20.0,
    1,
    1,
    10,
    15.0,
    0.5,
    29.90,
    39.90,
    5.0,
    1,
    1,
    'Eiichiro Oda',
    'One Piece Vol. 1',
    'A aventura de Luffy começa!',
    'https://m.media-amazon.com/images/I/716EGgqzyOL.jpg'
);

