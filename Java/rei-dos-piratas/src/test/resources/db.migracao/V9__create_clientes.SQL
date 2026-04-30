-- V9: Criação da tabela CLIENTES
-- Herança TABLE_PER_CLASS: todos os campos de JpaUsuarioEntity replicados aqui
CREATE TABLE CLIENTES (
    id              NUMBER NOT NULL PRIMARY KEY,
    user_name       VARCHAR2(30)  NOT NULL UNIQUE,
    nome_completo   VARCHAR2(50)  NOT NULL,
    email           VARCHAR2(40)  NOT NULL UNIQUE,
    senha           VARCHAR2(255) NOT NULL,
    usuario_ativo   NUMBER(1)     NOT NULL,
    data_cadastro   DATE          NOT NULL,
    perfil_id       NUMBER,
    data_nascimento DATE          NOT NULL,
    sexo            VARCHAR2(20)  NOT NULL,
    cpf             VARCHAR2(11)  NOT NULL UNIQUE,
    celular         VARCHAR2(11)  NOT NULL,
    carrinho_id     NUMBER        UNIQUE,
    CONSTRAINT fk_cli_perfil   FOREIGN KEY (perfil_id)   REFERENCES PERFIS(id),
    CONSTRAINT fk_cli_carrinho FOREIGN KEY (carrinho_id) REFERENCES CARRINHOS(id)
);

