# Rei dos Piratas — API e painel web

> Back-end do e-commerce **Rei dos Piratas**, uma loja de mangás novos e usados com foco em One Piece. A loja hoje vende pela [Shopee](https://shopee.com.br/rei_dos_piratas).

Este projeto Spring Boot tem duas frentes:

- uma **API REST**, consumida pelo [app mobile](https://github.com/FIAP-1TDSPS-2024/rei-dos-piratas-mobile);
- o **painel web Thymeleaf**, com a área do funcionário e a área do cliente.

O objetivo, o escopo e o diagrama de arquitetura estão no [README principal](../../README.md). As convenções de código estão em [AGENTS.md](../../AGENTS.md).

---

## Stack

| Item | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 (Web, Data JPA, Security, Validation, HATEOAS, Thymeleaf) |
| Spring Cloud | 2023.0.5 (OpenFeign) |
| Spring Cloud AWS | 3.2.1 (SQS) |
| Banco | PostgreSQL 18 · H2 em modo PostgreSQL nos testes |
| Migrations | Flyway (`src/main/resources/db.migracao`) |
| Autenticação | JWT (jjwt 0.12.5) + refresh token + sessões persistidas |
| Documentação | springdoc-openapi 2.6 (Swagger UI) |
| Logs | Logback + logstash-logback-encoder 8.0 (JSON) |
| Testes | JUnit 5, Mockito, ArchUnit 1.3, WireMock 3.9, JaCoCo 0.8 |

Organização em Clean Architecture (`domain`, `application`, `interfaces` e `infrastructure`), com as regras de camada verificadas pelo `ArquiteturaTest`.

---

## Como executar

### Pré-requisitos

- JDK 21
- Docker, para o PostgreSQL local

Os comandos abaixo rodam a partir de `Java/rei-dos-piratas`. No PowerShell, use `.\mvnw.cmd`.

```bash
docker compose up -d postgres     # PostgreSQL local na porta 5432
./mvnw spring-boot:run            # sobe a aplicação em http://localhost:8080
```

As variáveis de ambiente podem ficar num arquivo `.env`. O `compose.yaml` já o referencia, e ele **nunca é versionado**.

### Variáveis de ambiente

| Grupo | Variáveis | Observação |
|---|---|---|
| Banco | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `FLYWAY_ENABLED` | `FLYWAY_ENABLED=true` aplica as migrations na subida |
| JWT | `JWT_SECRET`, `JWT_EXPIRATION` | `JWT_SECRET` é obrigatória: use um valor longo e aleatório. Expiração padrão: 900000 ms |
| Asaas | `COBRANCA_URL`, `COBRANCA_API_KEY` | Sandbox: `https://api-sandbox.asaas.com` |
| Melhor Envio | `ME_URL`, `ME_CLIENT_ID`, `ME_SECRET`, `ME_REDIRECT_URI`, `ME_SERVICE_DEVOLUCAO` | `ME_SERVICE_DEVOLUCAO` é o id do serviço de entrega usado no frete reverso |
| Parcelamento | `PAGAMENTO_PARCELAMENTO_VALOR_MINIMO`, `PAGAMENTO_PARCELAMENTO_MAX_PARCELAS` | Padrões: `100.00` (abaixo disso, só à vista) e `3` parcelas |
| Boleto | `PAGAMENTO_BOLETO_DIAS_VENCIMENTO` | Padrão: `3` dias corridos |
| SQS | `SQS_ENABLED`, `SQS_QUEUE_NAME`, `AWS_REGION` | O listener só existe com `SQS_ENABLED=true`. Região padrão: `sa-east-1`. Credenciais pela cadeia padrão da AWS (IAM role em produção). Ajuste fino: `SQS_MAX_CONCURRENT_MESSAGES`, `SQS_MAX_MESSAGES_PER_POLL`, `SQS_POLL_TIMEOUT`, `SQS_VISIBILITY_TIMEOUT` |
| Eventos | `EVENTOS_EXTERNOS_LEASE_SEGUNDOS` | Padrão: `300`. Lease que impede processar o mesmo evento em paralelo |
| Web | `AUTH_COOKIE_SECURE`, `APP_ALLOWED_ORIGINS`, `APP_SIMULADOR_EVENTOS` | Veja [Telas web](#telas-web-thymeleaf) |
| Datadog RUM | `DD_RUM_CLIENT_TOKEN`, `DD_RUM_APPLICATION_ID` | Opcionais |
| Logs | `JPA_SHOW_SQL` | Padrão: `false`. Use `true` só para depuração local |

### Migrations

- O schema é gerenciado só pelo Flyway (`ddl-auto=validate`), e os mesmos scripts rodam em PostgreSQL e no H2 dos testes.
- Nunca edite uma migration já aplicada: crie a próxima `V<n>__descricao.SQL`.
- Para validar as migrations num PostgreSQL real, use uma base **vazia e exclusiva de teste**, nunca a de produção:

```bash
./mvnw test -Dtest=DatabaseMigrationIntegrationTest -Dspring.profiles.active=postgresql-test
# requer TEST_DB_URL, TEST_DB_USER e TEST_DB_PASSWORD
```

### Testes

```bash
./mvnw test                                  # suíte completa (H2; Asaas, Melhor Envio e SQS mockados)
./mvnw clean verify                          # build + testes + relatório JaCoCo em target/site/jacoco
node --test src/test/js/web-auth.test.cjs    # testes do web-auth.js
```

### Docker

O `Dockerfile` faz o build em múltiplos estágios (JRE 21, usuário não root, porta 8080) e **pula os testes**: eles precisam rodar no CI antes. O `buildspec.yml` da raiz gera a imagem no AWS CodeBuild e a publica no ECR.

---

## Documentação da API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- [Coleção do Postman](API%20Rei%20dos%20Piratas.postman_collection.json)

O JSON usa **snake_case**. As listagens paginadas aceitam `pageNumber` (padrão `0`) e `pageSize` (padrão `10`), e respondem `204` quando a página vem vazia.

Os erros seguem o `GlobalExceptionHandler`:

| Status | Quando acontece | Corpo |
|---|---|---|
| `400` | Validação | Mapa `{campo: mensagem}` |
| `400` | Regra de negócio, estoque insuficiente, status inválido, recurso não encontrado e dado duplicado | `{message}` |
| `401` | Não autenticado, credenciais inválidas ou assinatura de webhook inválida | |
| `403` | Sem permissão | |
| `422` | Pagamento com cartão recusado | |
| `502` | Falha do fornecedor externo (Asaas ou Melhor Envio) | |

### Autenticação

- **API e app:** `POST /auth/login` devolve o JWT e o refresh token. Envie `Authorization: Bearer <token>` nas chamadas.
- **Web:** usa cookies `HttpOnly` e CSRF, por `/web/auth/*`.
- As permissões vêm do perfil da conta (roles). O **cliente** tem `CARRINHO_MANAGE`, `ENDERECO_MANAGE`, `PEDIDO_CREATE`, `PEDIDO_READ`, `PEDIDO_PAGAMENTO` e `PEDIDO_CANCEL`. As roles de **funcionário** são `PRODUTO_WRITE`, `PEDIDO_WRITE` e `FUNCIONARIO_WRITE`, conforme o perfil.

## Endpoints

### Autenticação — `/auth`

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/login` | público | Login, que devolve o token e o refresh token |
| POST | `/auth/cadastro` | público | Cadastro de cliente |
| POST | `/auth/refresh` | público | Renova o token pelo refresh token |
| POST | `/auth/logout` | autenticado | Revoga a sessão |

### Produtos — `/produtos`

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/produtos` | público | Lista os produtos (paginado) |
| GET | `/produtos/{id}` | público | Busca um produto |
| POST | `/produtos` | `PRODUTO_WRITE` | Cria um produto |
| PUT | `/produtos` | `PRODUTO_WRITE` | Atualiza um produto |
| DELETE | `/produtos/{id}` | `PRODUTO_WRITE` | Remove um produto |

### Carrinho — `/carrinho` (cliente)

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/carrinho` | Mostra o carrinho do cliente logado |
| PUT | `/carrinho/adicionar` | Adiciona um item (`produto_id`, `quantidade`) |
| PUT | `/carrinho/remover` | Remove um item |
| PUT | `/carrinho/limpar` | Esvazia o carrinho |
| PUT | `/carrinho/finalizar` | Gera o pedido a partir do carrinho (`frete_service_id`, `endereco_entrega_id`, `tipo_pagamento`). Cartão ainda não é aceito aqui: use o `POST /pedidos` |

Todas as rotas exigem `ENDERECO_MANAGE`.

### Endereços e cartões (cliente)

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/enderecos` | Lista os endereços ativos do cliente logado |
| GET | `/enderecos/{id}` | Busca um endereço do cliente |
| POST | `/enderecos` | Cria um endereço |
| PUT | `/enderecos/{id}` | Atualiza um endereço |
| GET | `/cartoes` | Lista os cartões salvos: id, bandeira e 4 últimos dígitos |

- Todas as rotas exigem `ENDERECO_MANAGE`, e os funcionários recebem `403`.
- O token do Asaas nunca sai da API: o app usa o `id` do cartão como `cartao_id` no pedido.
- Não há endpoint para cadastrar cartão: a tokenização acontece dentro do pedido.

### Frete — `/frete`

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/frete` | público | Cota as opções de frete (Melhor Envio) para um CEP e uma lista de produtos |

### Pedidos — `/pedidos`

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/pedidos` | `PEDIDO_READ` | Lista os pedidos do cliente logado (paginado) |
| GET | `/pedidos/status/{status}` | `PEDIDO_WRITE` | Lista os pedidos por status (funcionário, paginado) |
| GET | `/pedidos/{id}` | `PEDIDO_READ` | Busca um pedido (o dono ou um funcionário) |
| GET | `/pedidos/{id}/pix-qrcode` | `PEDIDO_READ` | QR Code e copia-e-cola do PIX |
| GET | `/pedidos/{id}/boleto` | `PEDIDO_READ` | Linha digitável, código de barras, URL e vencimento do boleto |
| GET | `/pedidos/{id}/estornos` | `PEDIDO_READ` | Estornos do pedido |
| POST | `/pedidos` | `PEDIDO_CREATE` | Faz o pedido com PIX, boleto ou cartão (salvo ou novo, parcelado ou à vista) |
| PUT | `/pedidos/cancelamento/{id}` | `PEDIDO_CANCEL` | Cancela o pedido: estorna se já foi pago, ou cancela a cobrança pendente |
| PUT | `/pedidos/organizar-pedidos` | `PEDIDO_WRITE` | Compra o frete dos pedidos pagos (corpo: lista de ids) |
| PUT | `/pedidos/gerar-etiquetas` | `PEDIDO_WRITE` | Gera as etiquetas no Melhor Envio |
| PUT | `/pedidos/imprimir-etiquetas` | `PEDIDO_WRITE` | Devolve o link de impressão das etiquetas |

O status do pedido avança sozinho pelos eventos do Asaas (pagamento) e do Melhor Envio (postagem e entrega). Não há endpoint para marcar pagamento ou envio manualmente.

### Devoluções — `/devolucoes`

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/devolucoes` | `PEDIDO_READ` | O cliente solicita a devolução de itens de um pedido entregue |
| GET | `/devolucoes` | `PEDIDO_READ`/`PEDIDO_WRITE` | Lista as devoluções (paginado) |
| GET | `/devolucoes/{id}` | `PEDIDO_READ`/`PEDIDO_WRITE` | Busca uma devolução |
| GET | `/devolucoes/status/{status}` | `PEDIDO_READ`/`PEDIDO_WRITE` | Lista por status (paginado) |
| GET | `/devolucoes/pedido/{pedidoId}` | `PEDIDO_READ`/`PEDIDO_WRITE` | Devoluções de um pedido (paginado) |
| GET | `/devolucoes/pedido/{pedidoId}/todas` | `PEDIDO_READ`/`PEDIDO_WRITE` | Todas as devoluções de um pedido |
| PUT | `/devolucoes/{id}/aprovar` | `PEDIDO_WRITE` | Aprova e cria o envio reverso (status `PREPARANDO_RETORNO`) |
| PUT | `/devolucoes/{id}/recusar` | `PEDIDO_WRITE` | Recusa a devolução |
| PUT | `/devolucoes/organizar-devolucoes` | `PEDIDO_WRITE` | Paga o frete de retorno das devoluções em `PREPARANDO_RETORNO` (corpo: lista de ids) e as leva para `AGUARDANDO_POSTAGEM_RETORNO` |
| PUT | `/devolucoes/{id}/concluir` | `PEDIDO_WRITE` | Conclui a devolução recebida (`RETORNADO`) e estorna o cliente |

No `organizar-devolucoes`, só os ids no status elegível são processados. Se o Melhor Envio devolver erro, os status não avançam: a resposta é `400` para erro do fornecedor e `502` para falha de comunicação.

### Clientes e funcionários

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/clientes` | autenticado | Lista os clientes (paginado) |
| GET | `/clientes/{id}` | autenticado | Busca um cliente |
| PUT | `/clientes` | cliente | Atualiza os dados do próprio cliente logado |
| DELETE | `/clientes` | cliente | Desativa a própria conta |
| GET | `/funcionarios` | autenticado | Lista os funcionários (paginado) |
| GET | `/funcionarios/{id}` | autenticado | Busca um funcionário |
| POST | `/funcionarios` | `FUNCIONARIO_WRITE` | Cria um funcionário |
| PUT | `/funcionarios` | `FUNCIONARIO_WRITE` | Atualiza um funcionário |
| PUT | `/funcionarios/{id}` | `FUNCIONARIO_WRITE` | Ativa ou desativa um funcionário |

O primeiro administrador vem da migration (`admin@reidospiratas.com.br`). Troque a senha dele antes de usar o sistema em qualquer ambiente compartilhado.

### Webhooks e saúde

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/rastreio/webhook` | Webhook de rastreio do Melhor Envio, com assinatura HMAC no header `x-me-signature` |
| GET | `/health` | Health check |

Os eventos de cobrança do **Asaas** não chegam por HTTP: eles entram pela fila **SQS** (`SQS_QUEUE_NAME`).

---

## Regras de pagamento e estorno (Asaas)

### Cartão de crédito

- O Asaas exige o IP do comprador (`remoteIp`) na tokenização e na cobrança. A API usa o primeiro IP do `X-Forwarded-For`; sem esse header, usa o IP da conexão. O proxy precisa repassar o header.
- O pedido é gravado antes da cobrança, e o resultado é registrado depois, em outra transação. Assim nunca existe cobrança sem pedido.

Respostas do `POST /pedidos` com cartão:

| Resultado | Resposta | Pedido | Pagamento |
|---|---|---|---|
| Aprovado | `201` | | `CONFIRMADO` |
| Em análise de risco | `201` | | `PENDENTE` |
| Recusado | `422` | `CANCELADO` (estoque devolvido) | `RECUSADO` |
| Erro do Asaas que não é recusa | `502` | `CANCELADO` (estoque devolvido) | `CANCELADO` |
| Sem resposta (timeout) | `201` | `AGUARDANDO_PAGAMENTO` | `PENDENTE` |

- No timeout, não há nova tentativa, para evitar cobrança duplicada: o webhook confirma o resultado depois. O timeout de leitura do cliente do Asaas é de 60 s.
- Sandbox: o cartão `4444 4444 4444 4444` (validade futura, CCV `123`) é aprovado. Os cartões `5184019740373151` e `4916561358240741` são recusados de propósito.

### Boleto e PIX

- O boleto vence em `PAGAMENTO_BOLETO_DIAS_VENCIMENTO` dias. O registro é cancelado 1 dia depois do vencimento, então ele não pode mais ser pago. O estoque fica reservado durante esse prazo.
- Cobrança vencida (boleto ou PIX) cancela o pedido e devolve o estoque.
- Pagamento que chega com o pedido já cancelado não reativa o pedido: ele é estornado automaticamente.
- Sandbox: `POST /v3/sandbox/payment/{id}/confirm`, na API do Asaas, simula o pagamento do boleto.

### Estornos

- **Cancelamento de pedido pago:** estorna o valor total, frete incluído.
- **Devolução:** o estorno acontece quando o funcionário conclui a devolução. O valor é o dos itens devolvidos, pelo preço pago; no arrependimento do pedido inteiro, entra também o frete de ida. O frete de volta é sempre da loja.
- **PIX e cartão:** são estornados pela API. No cartão, o estorno pode levar até 10 dias úteis para aparecer na fatura.
- **Boleto:** o Asaas gera um link em que o cliente informa os dados bancários (estorno `AGUARDANDO_CLIENTE`).
- **Status do estorno:** `SOLICITADO`, `EM_ANDAMENTO`, `AGUARDANDO_CLIENTE`, `CONCLUIDO`, `NEGADO` ou `FALHOU`. Os dois últimos precisam de ação manual no painel do Asaas (ex.: saldo insuficiente para estornar um PIX).

## Frete e rastreio (Melhor Envio)

- Para comprar e imprimir etiquetas, a conta da integração precisa estar autorizada no Melhor Envio, com token e refresh token válidos.
- O `POST /rastreio/webhook` valida o `x-me-signature`: um HMAC-SHA256 do corpo com `ME_SECRET`, comparado em tempo constante. As respostas são:

| Status | Quando acontece |
|---|---|
| `200` | Evento processado, ou ignorado quando o corpo não identifica um envio |
| `401` | Assinatura ausente ou inválida |
| `500` | Falha ao processar; o evento não é descartado em silêncio |

- As datas de entrega usam o fuso da loja (`America/Sao_Paulo`).

---

## Telas web (Thymeleaf)

- **Público:**
  - `/web/produtos`, a vitrine;
  - `/web/login`;
  - `/web/cadastro`, a criação de conta de cliente (ao concluir, o cliente já entra logado).
  
  Ainda não há recuperação de senha.
- **Cliente** (`/web/conta/**`):
  - carrinho;
  - checkout: endereço, frete e PIX, boleto ou cartão;
  - pagamento: QR Code do PIX, linha digitável do boleto e status do cartão;
  - meus pedidos e cancelamento;
  - estornos, com o link de dados bancários do boleto;
  - pedido de devolução.
- **Funcionário:**
  - `/web/produtos/novo|editar|excluir`, a gestão de produtos;
  - `/web/pedidos`, o painel de envio (o detalhe mostra pagamento, estornos e cancelamento);
  - `/web/devolucoes`, onde o funcionário aprova, recusa, paga o frete de retorno e conclui com estorno.
- **Simulador de eventos** (`APP_SIMULADOR_EVENTOS=true`, só funcionário). Fica no detalhe do pedido e da devolução, e simula, pelo mesmo caminho dos webhooks reais:
  - pagamento confirmado, recebido ou vencido;
  - cartão recusado;
  - estorno concluído ou negado;
  - rastreio postado, entregue ou cancelado.
  
  É útil sem SQS ou webhook local. **Nunca ligue em produção.**

### Acesso por HTTP fora do `localhost`

Isso vale, por exemplo, para uma VM de teste em `http://<IP>:8080`.

- Defina `AUTH_COOKIE_SECURE=false`. Sem isso, o navegador descarta os cookies de sessão.
- Sem HTTPS, o navegador também não oferece Web Locks. Nesse caso, o `web-auth.js` usa um lease no `localStorage` (que não guarda tokens) para que só uma aba renove a sessão por vez.

Em produção, use sempre HTTPS e `AUTH_COOKIE_SECURE=true`.
