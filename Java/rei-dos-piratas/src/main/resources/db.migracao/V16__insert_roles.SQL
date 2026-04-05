-- V16: Dados iniciais - Roles do sistema
-- Nomes sem prefixo ROLE_ pois o código adiciona "ROLE_" + nome dinamicamente
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
