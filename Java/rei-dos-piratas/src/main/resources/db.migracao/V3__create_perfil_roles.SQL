-- V3: Criação da tabela de associação PERFIL_ROLES (N:N entre PERFIS e ROLES)
CREATE TABLE PERFIL_ROLES (
    perfil_id NUMBER NOT NULL,
    role_id   NUMBER NOT NULL,
    CONSTRAINT pk_perfil_roles   PRIMARY KEY (perfil_id, role_id),
    CONSTRAINT fk_pr_perfil      FOREIGN KEY (perfil_id) REFERENCES PERFIS(id),
    CONSTRAINT fk_pr_role        FOREIGN KEY (role_id)   REFERENCES ROLES(id)
);

