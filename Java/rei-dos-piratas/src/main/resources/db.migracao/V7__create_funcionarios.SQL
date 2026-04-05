-- V7: Criação da tabela FUNCIONARIOS
-- Herança TABLE_PER_CLASS: todos os campos de JpaUsuarioEntity replicados aqui
CREATE TABLE FUNCIONARIOS (
    id             NUMBER NOT NULL PRIMARY KEY,
    user_name      VARCHAR2(30)  NOT NULL UNIQUE,
    nome_completo  VARCHAR2(50)  NOT NULL,
    email          VARCHAR2(40)  NOT NULL UNIQUE,
    senha          VARCHAR2(255) NOT NULL,
    usuario_ativo  NUMBER(1)     NOT NULL,
    data_cadastro  DATE          NOT NULL,
    perfil_id      NUMBER,
    data_demissao  DATE,
    salario        NUMBER(8,2),
    CONSTRAINT fk_func_perfil FOREIGN KEY (perfil_id) REFERENCES PERFIS(id)
);

-- Sequence para geração de IDs compartilhados (TABLE_PER_CLASS usa sequence única)
CREATE SEQUENCE seq_usuario_id START WITH 1 INCREMENT BY 50;

