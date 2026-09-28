-- Variante H2 para testes, reunida pelo Maven com as demais migrations.
-- A única adaptação funcional
-- é ALTER COLUMN ... SET NOT NULL no lugar de MODIFY ... NOT NULL.
-- Manter alinhada à V29 de produção até a migração do projeto para PostgreSQL.
ALTER TABLE DEVOLUCOES ADD motivo_devolucao_id NUMBER;
ALTER TABLE DEVOLUCOES ADD valor_total NUMBER(12,2);
ALTER TABLE DEVOLUCOES ADD valor_frete NUMBER(12,2);
ALTER TABLE DEVOLUCOES ADD status VARCHAR2(50);
ALTER TABLE DEVOLUCOES ADD servico_entrega NUMBER;
ALTER TABLE DEVOLUCOES ADD pedido_frete VARCHAR2(36);
ALTER TABLE DEVOLUCOES ADD protocolo_envio VARCHAR2(255);
ALTER TABLE DEVOLUCOES ADD status_envio VARCHAR2(255);
ALTER TABLE DEVOLUCOES ADD tracking VARCHAR2(255);
ALTER TABLE DEVOLUCOES ADD tracking_url VARCHAR2(500);

UPDATE DEVOLUCOES d
   SET d.motivo_devolucao_id = (
       SELECT m.id
         FROM MOTIVOS_DEVOLUCAO m
        WHERE m.codigo = d.motivo
   )
 WHERE d.motivo_devolucao_id IS NULL;

UPDATE DEVOLUCOES d
   SET d.status = 'PREPARANDO_ENVIO'
 WHERE d.status IS NULL;

ALTER TABLE DEVOLUCOES ALTER COLUMN motivo_devolucao_id SET NOT NULL;

ALTER TABLE DEVOLUCOES
    ADD CONSTRAINT fk_dev_motivo_devolucao
        FOREIGN KEY (motivo_devolucao_id) REFERENCES MOTIVOS_DEVOLUCAO(id);
