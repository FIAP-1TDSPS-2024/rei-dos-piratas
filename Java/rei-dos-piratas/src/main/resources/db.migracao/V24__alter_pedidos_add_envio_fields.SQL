-- V24: Adiciona campos de envio/rastreamento na tabela PEDIDOS
ALTER TABLE PEDIDOS ADD protocolo_envio VARCHAR2(255);
ALTER TABLE PEDIDOS ADD status_envio    VARCHAR2(255);
ALTER TABLE PEDIDOS ADD tracking        VARCHAR2(255);
ALTER TABLE PEDIDOS ADD tracking_url    VARCHAR2(500);

