# Rei Dos Piratas

> API REST para Back-End da aplicacao Rei dos Piratas, um e-commerce de mangas usados e lancamentos.

---

## Video explicativo da solucao

[Video Explicativo no YouTube](https://www.youtube.com/watch?v=wSOUlz6PsAY)

## Integrantes do Grupo - CATECH

- **RM561144**: Jonas Oliveira - Java e banco de dados
- **RM559336**: Wendell Dourado - Mobile e DevOps
- **RM559622**: Daniel Batista - .NET, IoT e QA

---

## Diagrama da Aplicacao

[Diagrama da Aplicacao no Lucidchart](https://lucid.app/lucidchart/8cb61c77-643b-4269-a160-0be5cca49936/edit)

---

## Repositório e vídeo do projeto mobile integrado à API

https://github.com/FIAP-1TDSPS-2024/rei-dos-piratas-mobile

https://youtu.be/12ezMQBmk4s

---

## Cronograma de Desenvolvimento - Sprint 3 (atualizado)

| Sem | Dias          | Foco | Entrega |
| --- |---------------| ---- | ------- |
| 1 | 24/03 - 29/03 | API Melhor Envio | Cotacao de frete, criacao/organizacao de pedidos de frete e geracao de etiquetas |
| 1 | 29/03 - 01/04 | Fluxo de pedidos | Ajustes de status: pagamento, preparacao, postagem, envio e entrega |
| 1 | 01/04 - 03/04 | Perfis e roles | Reforma de autorizacao baseada em permissoes |
| 2 | 03/04 - 06/04 | Front de produtos | Tela e fluxo web para cadastro/edicao/listagem |
| 2 | 06/04 - 08/04 | Front de pedidos | Tela e fluxo web para acompanhamento e impressao de etiquetas |
| 2 | 08/04 - 12/04 | Stabilization | Hardening, testes integrados e ajustes finais de documentacao |

## Cronograma de Desenvolvimento - Sprint 4

> Periodo planejado: **07/05 a 20/05** (duas semanas antes de 21/05).

| Sem | Datas | Foco | Entrega |
| --- | ----- | ---- | ------- |
| 1 | 07/05 - 09/05 | Webhooks de pedidos | Recebimento e processamento de eventos para acompanhamento de pedidos |
| 1 | 10/05 - 12/05 | Login e autenticacao | Aprimoramento do fluxo de login com eventos/webhook e maior rastreabilidade |
| 1 | 13/05 - 14/05 | Seguranca | Melhorias de seguranca no fluxo de autenticacao e autorizacao |
| 2 | 15/05 - 17/05 | Cancelamento | Revisao de regras e fluxo de cancelamento com validacoes de status |
| 2 | 18/05 - 19/05 | Envio reverso | Implementacao de fluxo de logistica reversa e acompanhamento |
| 2 | 20/05 | Fechamento | Testes de ponta a ponta, ajustes finais e preparacao para marco de 21/05 |

---

## Como executar a aplicacao

### 1) Clonar e acessar a pasta

```bash
git clone https://github.com/FIAP-1TDSPS-2024/rei-dos-piratas.git
cd rei-dos-piratas/Java/rei-dos-piratas
```

### 2) Definir variaveis de ambiente

A aplicacao usa variaveis de ambiente para conexao com banco, JWT e integracao com Melhor Envio.

Variaveis obrigatorias para banco:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

Variavel para controle de migration:

- `FLYWAY_ENABLED` (`true` para executar migrations, `false` para nao executar)

Variaveis obrigatorias para API Melhor Envio:

- `ME_URL`
- `ME_CLIENT_ID`
- `ME_SECRET`
- `ME_REDIRECT_URI`

Variaveis de seguranca JWT (recomendado definir em producao):

- `JWT_SECRET`
- `JWT_EXPIRATION`

Exemplo de configuracao no PowerShell (sessao atual):

```powershell
$env:DB_URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1"
$env:DB_USER = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
$env:FLYWAY_ENABLED = "true"

$env:ME_URL = "https://sandbox.melhorenvio.com.br"
$env:ME_CLIENT_ID = "seu_client_id"
$env:ME_SECRET = "seu_client_secret"
$env:ME_REDIRECT_URI = "https://sua-url-de-retorno"

$env:JWT_SECRET = "defina-um-segredo-forte"
$env:JWT_EXPIRATION = "86400000"
```

> Se estiver usando Docker Compose, o `compose.yaml` ja referencia um arquivo `.env` (`env_file`), entao voce pode colocar essas mesmas variaveis nele.

### 3) Executar migrations

As migrations Flyway estao em `src/main/resources/db.migracao` e so rodam quando `FLYWAY_ENABLED=true`.

Fluxo recomendado:

1. Primeira subida do ambiente/base: usar `FLYWAY_ENABLED=true`.
2. Base ja estruturada: pode usar `FLYWAY_ENABLED=false` se nao quiser validar/aplicar scripts no startup.

### 4) Compilar e subir a aplicacao

```bash
mvn clean install
mvn spring-boot:run
```

Aplicacao disponivel em `http://localhost:8080`.

---

## Observacao importante sobre etiquetas (Melhor Envio)

Para gerar e **imprimir etiquetas de envio**, e necessario que exista login/autorizacao valida no Melhor Envio (token/refresh token validos para a conta usada na integracao).

Sem essa autorizacao, as operacoes de frete podem falhar na etapa de organizacao, geracao ou impressao das etiquetas.

---

## Endpoints disponiveis

### Funcionarios

| Metodo | Endpoint | Descricao |
| ------ | -------- | --------- |
| GET | /vendedores | Listar funcionarios |
| GET | /funcionarios/{id} | Buscar funcionario por id |
| POST | /funcionarios | Criar funcionario |
| PUT | /funcionarios | Atualizar funcionario |
| PUT | /funcionarios/{id} | Ativar/Desativar funcionario |

### Clientes

| Metodo | Endpoint | Descricao |
| ------ | -------- | --------- |
| GET | /clientes?pageNumber={n}&pageSize={m} | Listar clientes paginados |
| GET | /clientes | Buscar clientes |
| GET | /clientes/{id} | Buscar cliente por id |
| POST | /clientes | Criar cliente |
| PUT | /clientes | Atualizar cliente |
| DELETE | /info?id={id} | Remover cliente (rota presente na colecao Postman) |

### Produtos

| Metodo | Endpoint | Descricao |
| ------ | -------- | --------- |
| GET | /produtos | Listar produtos |
| GET | /produtos/{id} | Buscar produto por id |
| POST | /produtos | Criar produto |
| PUT | /produtos | Atualizar produto |
| DELETE | /produtos/{id} | Remover produto |

### Pedidos

| Metodo | Endpoint | Descricao |
| ------ | -------- | --------- |
| GET | /pedidos | Listar pedidos |
| GET | /pedidos/{id} | Buscar pedido por id |
| POST | /pedidos | Criar pedido |
| PUT | /pedidos/pagamento/{id} | Marcar pagamento do pedido |
| PUT | /pedidos/envio/{id} | Marcar envio/entrega do pedido |
| PUT | /pedidos/cancelamento/{id} | Cancelar pedido |

### Carrinho

| Metodo | Endpoint | Descricao |
| ------ | -------- | --------- |
| GET | /carrinho | Ver carrinho do usuario |
| PUT | /carrinho/adicionar | Adicionar item ao carrinho |
| PUT | /carrinho/remover | Remover item do carrinho |
| PUT | /carrinho/limpar | Limpar carrinho |
| PUT | /carrinho/finalizar | Finalizar pedido a partir do carrinho |

### Authentication

| Metodo | Endpoint | Descricao |
| ------ | -------- | --------- |
| POST | /auth/login | Autenticacao / login (gera token) |
| POST | /auth/cadastro | Cadastro de usuario (apenas clientes) |

---

## Colecao Postman

[Link do arquivo JSON da colecao Postman para teste dos endpoints](API%20Rei%20dos%20Piratas.postman_collection.json)

## Tecnologias utilizadas

- Java 17
- Spring Boot 3.5.6
- Maven
- Banco de dados: Oracle (principal) e H2 (testes)

## Dependencias principais

- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-validation`
- `com.oracle.database.jdbc:ojdbc11` (runtime)
- `org.projectlombok:lombok` (optional)

Ferramentas adicionais:

- `org.springframework.boot:spring-boot-devtools` (runtime, optional)
- `org.springframework.boot:spring-boot-starter-test` (test)
- `com.h2database:h2` (test)

Plugins do build:

- `maven-compiler-plugin`
- `jacoco-maven-plugin`
- `spring-boot-maven-plugin`

---

## Observacoes

- O usuario administrador deve existir previamente no banco para criacao de novos funcionarios.
- O modulo web (front para gestao de produtos e pedidos) esta no mesmo projeto Spring Boot via templates MVC.
