-- Converte os ordinais gravados pelo mapeamento JPA anterior em nomes de enum.
-- Valores que ja usam nomes permanecem inalterados.
UPDATE PEDIDOS SET status = CASE status
    WHEN '0' THEN 'AGUARDANDO_PAGAMENTO'
    WHEN '1' THEN 'AGUARDANDO_NF'
    WHEN '2' THEN 'PREPARANDO_ENVIO'
    WHEN '3' THEN 'AGUARDANDO_GERACAO_ETIQUETA'
    WHEN '4' THEN 'AGUARDANDO_POSTAGEM'
    WHEN '5' THEN 'EM_TRANSITO'
    WHEN '6' THEN 'ENTREGUE'
    WHEN '7' THEN 'CANCELADO'
    ELSE status END
WHERE status IN ('0', '1', '2', '3', '4', '5', '6', '7');

UPDATE PRODUTOS SET categoria = CASE categoria
    WHEN '0' THEN 'ACAO'
    WHEN '1' THEN 'AVENTURA'
    WHEN '2' THEN 'COMEDIA'
    WHEN '3' THEN 'DRAMA'
    WHEN '4' THEN 'FICCAO_CIENTIFICA'
    WHEN '5' THEN 'FANTASIA'
    WHEN '6' THEN 'TERROR'
    ELSE categoria END
WHERE categoria IN ('0', '1', '2', '3', '4', '5', '6');

UPDATE PRODUTOS SET condicao = CASE condicao
    WHEN '0' THEN 'NOVO' WHEN '1' THEN 'USADO' ELSE condicao END
WHERE condicao IN ('0', '1');

UPDATE CLIENTES SET sexo = CASE sexo
    WHEN '0' THEN 'M' WHEN '1' THEN 'F' ELSE sexo END
WHERE sexo IN ('0', '1');
