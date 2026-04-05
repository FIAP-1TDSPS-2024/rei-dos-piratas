-- V18: Associação de Roles aos Perfis

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
