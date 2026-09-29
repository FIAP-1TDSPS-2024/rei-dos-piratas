-- V9: Criação da tabela CLIENTES
-- Herança TABLE_PER_CLASS: todos os campos de JpaUsuarioEntity replicados aqui
CREATE TABLE CLIENTES (
    id              BIGINT NOT NULL PRIMARY KEY,
    user_name       VARCHAR(30)  NOT NULL UNIQUE,
    nome_completo   VARCHAR(50)  NOT NULL,
    email           VARCHAR(40)  NOT NULL UNIQUE,
    senha           VARCHAR(255) NOT NULL,
    usuario_ativo   BOOLEAN     NOT NULL,
    data_cadastro   DATE          NOT NULL,
    perfil_id       BIGINT,
    data_nascimento DATE          NOT NULL,
    sexo            VARCHAR(20)  NOT NULL,
    cpf             VARCHAR(11)  NOT NULL UNIQUE,
    celular         VARCHAR(11)  NOT NULL,
    carrinho_id     BIGINT        UNIQUE,
    CONSTRAINT fk_cli_perfil   FOREIGN KEY (perfil_id)   REFERENCES PERFIS(id),
    CONSTRAINT fk_cli_carrinho FOREIGN KEY (carrinho_id) REFERENCES CARRINHOS(id)
);

