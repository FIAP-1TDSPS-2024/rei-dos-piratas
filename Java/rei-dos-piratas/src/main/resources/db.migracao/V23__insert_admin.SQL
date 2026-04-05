-- V23: Usuário admin inicial
-- Perfil ADMIN (id buscado por subquery para não depender de id fixo)
-- Senha: admin123  ?  BCrypt strength 10
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
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
    OVERRIDING SYSTEM VALUE
VALUES (
    1,
    'admin',
    'Administrador do Sistema',
    'admin@reidospiratas.com.br',
    '$2a$12$.yviqjcXUtK7OJd6/CgBUu43wCCiyH9.PlcDZIXyXigL6dwAbGS/C',
    1,
    SYSDATE,
    (SELECT id FROM PERFIS WHERE nome = 'ADMIN'),
    NULL,
    0
);

