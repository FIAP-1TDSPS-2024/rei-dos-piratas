# AGENTS.md — Rei dos Piratas

Guia para agentes de IA (Claude Code e similares) que trabalham neste repositório.
Leia inteiro antes de alterar código. Em caso de conflito com o `README.md`, **este arquivo e o código prevalecem** (o README ainda descreve a fase FIAP: Azure, Oracle e PagBank).

> **Trabalho em andamento:** refatoração de Clean Architecture. Leia `docs/refatoracao-clean-architecture.md` antes de começar e mantenha-o atualizado.

## Contexto do produto

E-commerce de mangás (foco em One Piece, novos e usados), migrando a loja "Rei dos Piratas" da Shopee para infraestrutura própria. Origem acadêmica (Challenge FIAP), agora evoluindo para produção real com **deploy na AWS** (abordagem ainda em definição).

- Idioma do domínio, código, commits e documentação: **português** (sem acentos em identificadores Java).
- Integrações externas: **Melhor Envio** (frete/etiquetas/logística reversa) e **Asaas** (cobrança PIX e cartão de crédito tokenizado, webhooks de pagamento e estorno). PagBank não é mais usado.
- Front web server-side com **Thymeleaf** (painel de funcionários) + API REST consumida pelo app mobile (repo separado: `FIAP-1TDSPS-2024/rei-dos-piratas-mobile`).

## Stack

| Item | Versão / ferramenta |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.x (Web, Data JPA, Security, Validation, HATEOAS, Thymeleaf) |
| Spring Cloud | 2023.0.x — OpenFeign (clientes HTTP externos) |
| Spring Cloud AWS | 3.2.x — SQS (fila de eventos de cobrança) |
| Banco | **PostgreSQL** (produção) / H2 em modo PostgreSQL (testes) |
| Migrations | Flyway, em `src/main/resources/db.migracao` |
| Auth | JWT (jjwt 0.12) + refresh token + sessões persistidas; cookies na web |
| Docs da API | springdoc-openapi (Swagger UI) |
| Logs | Logback + logstash-logback-encoder (JSON, Datadog) |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |
| Cobertura | JaCoCo |

## Layout do repositório

```
/                       raiz do git (CI/CD e docs gerais)
├── AGENTS.md / CLAUDE.md
├── buildspec.yml       AWS CodeBuild -> imagem Docker -> ECR (alvo atual)
├── .github/workflows/  GitHub Actions -> Azure Web App (legado)
├── azure-pipelines.yaml (legado)
└── Java/rei-dos-piratas/   <- PROJETO MAVEN (rode os comandos daqui)
    ├── pom.xml, Dockerfile, compose.yaml, .env (não versionado)
    └── src/main/java/br/com/fiap/rei_dos_piratas/
        ├── domain/          entidades, enums, exceções, interfaces de repositório
        ├── application/     serviços (interfaces + impl) = casos de uso
        ├── interfaces/      controllers de aplicação, DTOs, GlobalExceptionHandler
        └── infrastructure/  Spring: REST, MVC, config de beans, JPA, Feign, segurança, mappers
```

## Arquitetura (Clean Architecture)

Regra de dependência: `infrastructure` → `interfaces` → `application` → `domain`. Camadas internas não conhecem as externas.

**Fluxo de uma requisição** (exemplo: Produto):
1. `infrastructure/api_rest/ProdutoRestController` — `@RestController`, anotações OpenAPI, só traduz HTTP.
2. `interfaces/controller/ProdutoController` (+ `impl/ProdutoControllerImpl`) — converte DTO ↔ entidade de domínio e chama o serviço.
3. `application/service/ProdutoService` (+ `impl/ProdutoServiceImpl`) — regras de negócio, validação (`jakarta.validation.Validator`), transações.
4. `domain/repository/ProdutoRepository` — interface (porta).
5. `infrastructure/repository/Impl/ProdutoRepositoryImpl` — adaptador que usa `JpaProdutoEntityRepository` (Spring Data) + `infrastructure/mapper/jpa/...` para converter `JpaProdutoEntity` ↔ `Produto`.

**Injeção de dependência:** classes de `domain`, `application` e `interfaces` **não** recebem `@Service`/`@Component`. Os beans são montados manualmente em `infrastructure/config/<contexto>/` (`XxxServiceConfig`, `XxxRepositoryConfig`, `XxxControllerConfig`) via `@Bean` e construtor. Ao criar uma nova feature, siga este padrão.

**Checklist para uma nova feature/entidade** (cada item é implementado pelo ciclo TDD descrito em [Desenvolvimento orientado a testes](#desenvolvimento-orientado-a-testes-tdd)):
- [ ] Entidade em `domain/entity`, enums em `domain/Enum`, exceções em `domain/exceptions`
- [ ] Interface de repositório em `domain/repository`
- [ ] `JpaXxxEntity` em `infrastructure/entity/<contexto>`, `JpaXxxEntityRepository`, `XxxRepositoryImpl`, mapper JPA
- [ ] Serviço (interface + impl) em `application/service`
- [ ] DTOs `XxxInDto`/`XxxOutDto` em `interfaces/dto/<contexto>`, controller (interface + impl), mapper DTO
- [ ] `XxxRestController` em `infrastructure/api_rest` com `@Tag`/`@Operation`/`@ApiResponses`
- [ ] Classes de config `@Configuration` em `infrastructure/config/<contexto>`
- [ ] Migration Flyway nova (se mexer no banco)
- [ ] Testes escritos **antes** do código de produção: unitários do serviço, RestController e, se for fluxo crítico, integração

**Dívida técnica conhecida (não piorar; corrigir quando tocar no arquivo):**
- Pagamento já usa a porta `domain/gateway/GatewayPagamento` (adaptador `infrastructure/external_interface/asaas/GatewayPagamentoAsaas`). Eventos de cobrança chegam ao `application` como `EventoPagamento` de domínio (traduzidos no `TradutorEventoAsaas`, chamado pelo listener). Frete usa a porta `domain/gateway/GatewayFrete` (adaptador `infrastructure/external_interface/melhorenvio/GatewayFreteMelhorEnvio`; o token OAuth fica no `ProvedorTokenMelhorEnvio`). O webhook de rastreio entra por `melhorenvio/WebhookRastreioMelhorEnvio` (HMAC + tradução) e chega ao `application` como `EventoRastreio`.
- Evento externo novo: o adaptador traduz o DTO do fornecedor para um evento de domínio e **descarta** (vazio + log `WARN`) o que não dá para processar, sem lançar exceção; o serviço só recebe o evento de domínio.
- Integração externa nova: porta em `domain/gateway` com tipos de domínio, adaptador + DTOs do fornecedor em `infrastructure/external_interface/<fornecedor>[/dto]`, teste de contrato do adaptador com **WireMock** (ex.: `GatewayPagamentoAsaasTest`) e serviço testado com mock da porta.
- `ArquiteturaTest` (ArchUnit) verifica as regras de camada de forma **estrita** (sem congelamento): `domain`/`application` não dependem de `infrastructure`/`interfaces`; `domain` não depende de `application` nem do Spring; `application` só usa o Spring para `org.springframework.transaction`; `interfaces` não depende de `infrastructure`; `domain`/`application` não usam Feign; `@Service`/`@Component`/`@Configuration` só em `infrastructure`. Se uma regra falhar, corrija o código; não afrouxe a regra nem volte a congelar violações. Exceção conhecida, fora das regras: Jackson em `domain/entity/Usuario`.
- Classe de configuração específica de um cliente Feign (`configuration = ...`) **não** leva `@Configuration`: com ela os beans (interceptor, `ErrorDecoder`) viram globais e vazam para os outros clientes (já aconteceu: o token do Melhor Envio ia para o Asaas).
- `domain/entity/Usuario` usa anotações do Jackson. O domínio deveria ser Java puro (Lombok e Bean Validation são tolerados).
- Usuário logado: use sempre `UsuarioAtualService` (`identidade()`, `identidadeSeAutenticado()`, `possuiPermissao()`); nunca `SecurityContextHolder`/`CustomUserDetails` fora de `infrastructure`.
- Mappers DTO ↔ domínio ficam em `interfaces/mapper`; mappers JPA e `PageMapper` em `infrastructure/mapper/jpa`.
- O pacote `domain/Enum` tem nome fora do padrão (maiúsculo); não renomeie sem pedido explícito, é uma mudança grande.
- Pedido de cartão que ficou sem resposta do Asaas e cuja cobrança de fato não foi criada fica `AGUARDANDO_PAGAMENTO` para sempre, com o estoque reservado (nenhum webhook chega). Falta uma rotina que expire esses pedidos.

## Domínio e validação

- **Regras e validações de entidade ficam no domínio**, nunca espalhadas em controllers ou serviços:
  - formato e obrigatoriedade de campos: Bean Validation (Jakarta) na própria entidade/record, com `message` em português (padrão de `Produto`, `Pedido`, `Pagamento`);
  - invariantes que não cabem em anotação: no construtor (record compacto) ou em método do domínio, lançando `RegraDeNegocioException` (ex.: `PagamentoCartao` exige cartão salvo **ou** novo; `RegraParcelamento.validar`);
  - associações validadas em cascata com `@Valid` (ex.: `Pedido.pagamento`, `PagamentoCartao.novoCartao`).
- O serviço chama o `jakarta.validation.Validator` antes de gravar (como `ProdutoServiceImpl`) e traduz violações em `ValidacaoException`. DTOs de entrada validam só o formato da requisição (`@NotNull`, `@Valid`); a regra de negócio não é duplicada neles.
- Regras configuráveis (limites, valores mínimos) viram objetos de domínio montados no `@Configuration` a partir de variáveis de ambiente (ex.: `RegraParcelamento`); nada de `@Value` em `domain`/`application`.
- **Agregados:** a entidade raiz contém as filhas e o repositório é só da raiz (ex.: `Pedido` contém `Pagamento`, um por pedido; não existe `PagamentoRepository`). A filha não referencia a raiz, para evitar ciclo no `toString`/`equals` do Lombok.
- Para mudar uma entidade filha, **altere o objeto existente** (`pedido.getPagamento().setStatus(...)`). Substituir por um `new` sem id faz o Hibernate inserir antes de apagar e viola a chave única.
- Valor obrigatório no banco precisa estar preenchido no domínio antes do primeiro `create` (ex.: `pagamento.valor` junto com `pedido.valorTotal`).
- Repositórios lançam `NoSuchElementException` quando o id não existe; o serviço traduz para `ResourceNotFoundException` (404). Nunca deixe a `NoSuchElementException` chegar ao controller (vira 500).
- Erro de negócio nunca é `IllegalArgumentException`/`IllegalStateException` genérica: use as exceções de `domain/exceptions`, senão a resposta vira 500.

## Comandos

Execute a partir de `Java/rei-dos-piratas`. No Windows (PowerShell) use `.\mvnw.cmd` no lugar de `./mvnw`.

Ambiente local: o `JAVA_HOME` padrão da máquina pode não ser o 21 (ex.: sdkman em Java 7) — aponte para um JDK 21 antes do `mvnw`. Se o mirror Maven corporativo do `~/.m2/settings.xml` estiver inacessível, rode com `-s <settings sem mirror>`.

```bash
./mvnw clean verify                      # build completo + testes + relatório JaCoCo (target/site/jacoco)
./mvnw test                              # todos os testes (H2, sem dependências externas)
./mvnw test -Dtest=ProdutoServiceImplTest          # uma classe
./mvnw test -Dtest=ProdutoServiceImplTest#metodo   # um método
./mvnw spring-boot:run                   # sobe a app (precisa das variáveis de ambiente)
node --test src/test/js/web-auth.test.cjs           # teste JS do web-auth.js
docker compose up -d postgres            # Postgres local na porta 5432
```

Teste de migrations contra Postgres real (base **vazia e exclusiva** de teste, nunca produção):
```bash
./mvnw test -Dtest=DatabaseMigrationIntegrationTest -Dspring.profiles.active=postgresql-test
# requer TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD
```

**Antes de considerar uma tarefa concluída:** `./mvnw test` precisa passar. Se algum teste já falhava antes da sua mudança, informe em vez de "consertar" o teste para passar.

## Configuração e variáveis de ambiente

Tudo vem de variáveis de ambiente referenciadas em `application.properties` (localmente via `.env`, que **não** é versionado):

- Banco: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `FLYWAY_ENABLED`
- JWT: `JWT_SECRET` (obrigatória, sem padrão), `JWT_EXPIRATION`
- Logs de SQL: `JPA_SHOW_SQL` (padrão `false`)
- Melhor Envio: `ME_URL`, `ME_CLIENT_ID`, `ME_SECRET`, `ME_REDIRECT_URI`, `ME_SERVICE_DEVOLUCAO` (serviço usado no frete reverso)
- Asaas: `COBRANCA_URL` + token de acesso
- Parcelamento no cartão: `PAGAMENTO_PARCELAMENTO_VALOR_MINIMO` (padrão `100.00`), `PAGAMENTO_PARCELAMENTO_MAX_PARCELAS` (padrão `3`)
- Boleto: `PAGAMENTO_BOLETO_DIAS_VENCIMENTO` (padrão `3`, dias corridos)
- AWS SQS: `SQS_ENABLED` (listener só existe se `true`), `SQS_QUEUE_NAME`, `AWS_REGION` (padrão `sa-east-1`), credenciais pela cadeia padrão da AWS (IAM role em produção)
- Web: `AUTH_COOKIE_SECURE` (`false` só em HTTP local), `APP_ALLOWED_ORIGINS`, `APP_SIMULADOR_EVENTOS` (padrão `false`; liga o simulador de eventos do Asaas/Melhor Envio no painel — **nunca** em produção)
- Datadog RUM: `DD_RUM_*`

Ao adicionar configuração nova: use `${VARIAVEL:padrao-seguro}` no properties, documente aqui e no README, e nunca coloque valores reais.

## Banco de dados e migrations

- `spring.jpa.hibernate.ddl-auto=validate`: o schema é **somente** Flyway. Toda mudança em entidade JPA precisa de migration correspondente.
- Nome: `V<próximo número>__descricao_em_snake_case.SQL` (a última é `V41`; confira antes de criar).
- **Nunca edite uma migration já existente** — crie uma nova. Migrations aplicadas são imutáveis.
- SQL deve rodar em PostgreSQL **e** em H2 modo PostgreSQL (os testes usam os mesmos scripts).
- Enums são persistidos como texto (ver `V33__normaliza_enums_textuais`).
- Migration de dados (backfill) fica separada da de estrutura e é **idempotente** (`... WHERE NOT EXISTS (...)`), como a `V39`.
- Remover coluna ou tabela é irreversível: só depois de copiar os dados e de o código parar de usá-la, em migration própria (ex.: `V38` cria → `V39` copia → `V40` apaga).
- Nomeie constraints (`fk_`, `uk_`, `ck_`) e use `CHECK` para enums textuais, como na `V37`/`V38`.

## Convenções de código

- Nomes em português: `ProdutoServiceImpl`, `findById`, `listAll` (verbos CRUD em inglês são aceitos, como já existe).
- DTOs de entrada/saída: `XxxInDto` / `XxxOutDto`. JSON em `snake_case` (configuração global do Jackson).
- Injeção por construtor; Lombok liberado (`@Data`, `@Slf4j` etc.).
- Logs com prefixo de contexto: `log.info("[REPO-PRODUTO] ...")`. Nunca logar senha, token, CPF completo ou dados de cartão (ver [Dados de cartão](#dados-de-cartão-e-dados-sensíveis)). Logue ids e quantidades, não objetos inteiros.
- Erros de negócio lançam exceções de `domain/exceptions` (`RegraDeNegocioException`, `ResourceNotFoundException`, `ValidacaoException`...); a resposta HTTP é montada no `GlobalExceptionHandler`. Não retorne `ResponseEntity` de erro manualmente nos controllers.
- Mensagens para o usuário em `messages.properties` (pt-BR) e `messages_en.properties`.
- Paginação usa `domain/entity/Page`, não o `Page` do Spring, fora da infraestrutura.

### Transações

- `@Transactional` só vale quando o método é chamado **de fora** do bean (pelo proxy do Spring). Chamar `this.outroMetodoTransacional()` não abre transação: o método público de entrada precisa ter a anotação (ex.: `fazerPedido(Pedido, PagamentoCartao)`). Sintoma típico: `LazyInitializationException` ou "no Session".
- **Chamada externa que move dinheiro não fica dentro de transação de banco.** Padrão (ver `PedidoServiceImpl.fazerPedido(Pedido, PagamentoCartao)`): T1 grava e confirma → chamada ao fornecedor sem transação → T2 **relê** o agregado (o webhook pode ter chegado antes) e registra o resultado sem rebaixar status. As transações explícitas usam a porta `domain/gateway/ExecutorTransacional`. Sem resposta do fornecedor (`CobrancaSemRespostaException`) não se repete a chamada.
- Quando um erro de negócio precisa **gravar estado antes de subir** dentro de uma única transação (ex.: `AutenticacaoServiceImpl` registra a tentativa e lança `CredenciaisInvalidasException`), use `@Transactional(noRollbackFor = ...)` para essa exceção. No fluxo com transações explícitas (cartão), grave o estado num `emTransacao` próprio e lance a exceção depois.
- Mappers JPA acessam relacionamentos `LAZY`: chame-os dentro da transação do repositório (as implementações já são `@Transactional(readOnly = true)` na classe).

### Datas e fuso horário

- Datas de negócio (entrega, conclusão de devolução etc.) são no fuso da loja, `America/Sao_Paulo`.
- `OffsetDateTime` vindo de API externa chega em UTC (o Jackson ajusta). Converta antes de extrair a data: `deliveredAt().atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).toLocalDate()`. Nunca `toLocalDate()` direto: depois das 21h de Brasília a data vira o dia seguinte.
- Testes não dependem do fuso da JVM: use datas fixas ou `LocalDate.now(ZoneId.of("America/Sao_Paulo"))`.

### Pagamentos (Asaas)

- O pagamento faz parte do `Pedido` (1–1). Status do `Pagamento`: `PENDENTE` → `CONFIRMADO` → `RECEBIDO`, ou `RECUSADO`/`CANCELADO`/`ESTORNADO`.
- Cartão: o Asaas autoriza na própria criação da cobrança. Aprovado (`CONFIRMED`) → `CONFIRMADO`; recusado → HTTP 400, traduzido pelo `AsaasErrorDecoder` em `PagamentoRecusadoException` (422); em análise de risco → continua `PENDENTE` até o webhook.
- Cobrança à vista envia `value`; parcelada envia `installmentCount` + `totalValue`. O limite de parcelas vem da `RegraParcelamento`.
- `remoteIp` (IP do **comprador**, nunca o do servidor) é obrigatório na tokenização e na cobrança com cartão. O `PedidoRestController` lê o primeiro IP do `X-Forwarded-For` (atrás do load balancer) ou, sem o header, o endereço da conexão, e o repassa até o `PagamentoCartao`.
- A recusa é identificada pelo **código** do erro (`invalid_creditCard`), não pela descrição, e só nas chamadas de cartão (`criarNovaCobranca`/`tokenizarCartao`). Outros 400 (campo inválido) são `ApiExternaException`. O log do `AsaasErrorDecoder` traz código e descrição.
- O cliente Feign do Asaas usa `read-timeout` de 60s (recomendação do Asaas para cartão; timeout curto arrisca cobrança duplicada em nova tentativa).
- Sandbox: cartão `4444 4444 4444 4444` aprova; `5184019740373151` e `4916561358240741` são recusados de propósito.
- Boleto: vence em `RegraBoleto.diasVencimento` (configurável) e é enviado com `daysAfterDueDateToRegistrationCancellation=1`, para deixar de ser pagável logo após o vencimento (sem isso o Asaas aceita pagamento por até 99 dias). Guardamos `urlBoleto` e `dataVencimento` no `Pagamento`; linha digitável e código de barras são consultados na hora (`GET /pedidos/{id}/boleto`). No sandbox, `POST /v3/sandbox/payment/{id}/confirm` simula o pagamento.
- `PAYMENT_OVERDUE` (boleto/PIX) com pedido `AGUARDANDO_PAGAMENTO`: cancela o pedido, devolve o estoque e marca o pagamento `CANCELADO`. Pagamento que chega com o pedido já `CANCELADO` **não reativa** o pedido: o pagamento vira `RECEBIDO`/`CONFIRMADO` e é **estornado automaticamente** (uma vez só; pagamento `ESTORNADO` não volta a `RECEBIDO`).
- **Estornos** (`Estorno`, filha de `Pagamento`, 1–N; tabela `ESTORNOS`): sempre pelo `EstornoPagamentoService` — T1 registra o estorno `SOLICITADO` junto com o motivo (cancelamento, devolução), a chamada ao Asaas roda fora de transação e a T2 relê o pedido e grava o resultado sem desfazer o que um evento já concluiu. Regras no domínio: só pagamento confirmado/recebido, total não passa do valor pago, uma devolução estorna uma vez, conclusão do valor todo marca o pagamento `ESTORNADO`.
  - PIX e cartão: `POST /v3/payments/{id}/refund` (parcial com `value`). Boleto: `POST /v3/payments/{id}/bankSlip/refund` devolve um link onde o cliente informa os dados bancários (estorno `AGUARDANDO_CLIENTE`, exposto em `GET /pedidos/{id}/estornos`).
  - Recusa do Asaas (ex.: saldo insuficiente no PIX, as taxas não voltam) marca `FALHOU` sem desfazer o cancelamento/devolução: ação manual. Sem resposta: fica `SOLICITADO`, sem nova tentativa; o evento resolve.
  - Eventos `PAYMENT_REFUNDED`/`PARTIALLY_REFUNDED`/`REFUND_IN_PROGRESS`/`REFUND_DENIED` atualizam o estorno em aberto mais antigo; chargeback só gera log `ERROR`.
  - Cancelamento de pedido pago estorna o valor total (frete incluído); cobrança pendente é cancelada no Asaas (`DELETE`) depois do commit. Devolução: estorno ao concluir (funcionário, status `RETORNADO`), pelo valor dos itens; arrependimento do pedido inteiro inclui o frete de ida; o frete de volta nunca é cobrado do cliente.
- `PAYMENT_CONFIRMED` libera o envio. No cartão, `PAYMENT_RECEIVED` só chega quando o valor é creditado (~30 dias), com o pedido possivelmente já entregue.
- **Eventos externos são idempotentes e tolerantes a ordem:** evento repetido ou fora de ordem não lança exceção (a exceção faz o evento ser reprocessado sem fim) e nunca repete efeitos como devolver estoque.
- Erros HTTP do fornecedor são traduzidos no `ErrorDecoder` do Feign em exceções de `domain/exceptions`; `FeignException` crua não chega ao serviço.

## Testes

- JUnit 5 + Mockito + Spring Boot Test + `spring-security-test`.
- Espelhe o pacote da classe testada em `src/test/java/...`; nome `XxxTest`.
- Unitário de serviço: mock dos repositórios (interfaces de domínio).
- `infrastructure/api_rest`: testes com MockMvc.
- `integration/`: sobem contexto completo com H2 + Flyway; use para fluxos de pedido, autenticação e permissões.
- Integrações externas (Asaas, Melhor Envio, SQS) **sempre mockadas** nos testes. Nunca chame APIs reais.
- Mocks de fornecedor devem respeitar as restrições do banco: ids externos únicos (ex.: `id_cobranca`) são gerados por chamada, não fixos.
- Validações do domínio são testadas com o `Validator` em classes `XxxValidacaoTest` (um comportamento por teste: objeto válido sem violações; cada regra quebrada gera violação).
- Dados sensíveis têm teste de que **não aparecem** no `toString`, em mensagens de validação e no JSON de resposta.
- Sem dependência do relógio ou do fuso da máquina: datas fixas nos cenários de borda (ex.: entrega às 23h30 de Brasília).

## Desenvolvimento orientado a testes (TDD)

**Todo código de produção novo é escrito com TDD.** Isso vale para features, regras de negócio e correções de bug.

### O ciclo

1. **Red** — escreva um teste que descreve *um* comportamento esperado. Rode e **confirme que falha pelo motivo certo** (asserção falhando, não erro de compilação ou de configuração). Mostre a falha.
2. **Green** — escreva o mínimo de código de produção para esse teste passar. Nada além do que o teste exige.
3. **Refactor** — melhore nomes, remova duplicação e ajuste o design com todos os testes verdes. Rode a suíte de novo.
4. Repita com o próximo comportamento.

Para compilar no passo Red, é permitido criar a assinatura vazia (interface, método lançando `UnsupportedOperationException`, DTO sem lógica).

### Por onde começar (de dentro para fora)

1. **Domínio** (`domain/entity`) — regras puras, sem mocks. É onde ficam os testes mais rápidos e mais valiosos.
2. **Serviço** (`application/service/impl`) — mockando as interfaces de `domain/repository` e os clientes externos.
3. **Controller e REST** (`interfaces/controller/impl`, `infrastructure/api_rest`) — mapeamento, status HTTP e permissões com MockMvc.
4. **Repositório e integração** (`integration/`) — persistência real em H2 + Flyway para fluxos críticos (pedido, pagamento, devolução, autenticação).

### Correção de bugs

Antes de corrigir, escreva um teste que **reproduz o bug e falha**. Só então corrija. O teste fica no repositório como proteção contra regressão.

### Regras para o agente

- Não escreva código de produção sem um teste falhando que o justifique. Se a tarefa não tem como ser testada (ex.: ajuste de `Dockerfile`, CSS de template), diga isso explicitamente.
- Ao reportar uma tarefa, mostre a sequência: teste criado → falha observada → implementação → testes passando.
- Para tarefas maiores, liste antes os casos de teste planejados (caminho feliz, validações, erros, limites) e espere aprovação.
- Nunca altere ou apague um teste existente só para fazê-lo passar. Se o teste estiver errado, explique por quê e peça confirmação.
- Testes descrevem comportamento, não implementação: verifique resultados e efeitos observáveis, evite `verify` excessivo de chamadas internas.
- Nomeie os testes pelo comportamento, em português: `deveLancarEstoqueInsuficienteQuandoQuantidadeMaiorQueEstoque()`. Use `@DisplayName` quando ajudar na leitura.
- Estruture cada teste em **Arrange / Act / Assert** (dado / quando / então), um comportamento por teste.
- Código legado sem teste: ao modificá-lo, primeiro cubra o comportamento atual com testes de caracterização e depois faça a mudança.

## Segurança — limites para o agente

- **Nunca** leia, imprima, edite ou faça commit de `.env`, `.env.production` ou de qualquer credencial. Se precisar saber se uma variável existe, pergunte.
- Webhooks devem validar a origem (o rastreio do Melhor Envio usa assinatura HMAC via `HmacUtil`, comparada em tempo constante com `MessageDigest.isEqual`, nunca `equals`; assinatura ausente ou inválida responde 401 via `AssinaturaInvalidaException`), não logar o corpo recebido, deixar falha de processamento subir (não engolir com `catch (Exception)`) e ser idempotentes (eventos de cobrança Asaas passam por `EventoExterno` com lease). Não remova essas proteções; novos webhooks devem seguir o mesmo padrão.
- Mudanças em `infrastructure/security/*`, `SecurityConfig` ou regras de permissão (roles/perfis) exigem aprovação explícita e testes em `PermissoesIntegrationTest`.
- Não altere valores monetários com `double`/`float`; use `BigDecimal`.

### Dados de cartão e dados sensíveis

- **Número e CVV do cartão nunca são persistidos**: servem só para tokenizar no Asaas (`DadosCartaoCredito`, que não tem entidade JPA). Persistimos apenas o token, os 4 últimos dígitos e a bandeira (`CartaoCliente`).
- **Nenhuma informação de cartão em log**: nem número, CVV, token, últimos dígitos ou bandeira. Logue só ids e quantidades (ex.: "2 cartões ativos do cliente ID=5").
- Classes com dado sensível não podem expor esses dados no `toString` (o `@Data` e o `record` imprimem tudo): use `@ToString.Exclude` ou sobrescreva o `toString` do record. Vale para domínio e DTOs.
- Mensagens de validação de dado sensível não incluem o valor informado (sem `${validatedValue}`).
- O token do Asaas não sai da API: DTOs de saída expõem só id, bandeira e últimos dígitos; o app usa o id (`cartao_id`).
- Feign com `logger-level=basic` (nunca `full`, que loga o corpo com o cartão). Não logar corpo de requisição/resposta HTTP.
- Dados pessoais do cliente (cartões, endereços, pedidos) são sempre do **usuário logado**, via `UsuarioAtualService`; o endpoint não recebe id de cliente de fora. Confira também o `TipoConta`: ids de cliente e funcionário vêm de tabelas diferentes e podem coincidir.
- Endpoints exclusivos de cliente usam uma role só do perfil CLIENT no `SecurityConfig` (hoje `ENDERECO_MANAGE`, em `/carrinho`, `/enderecos`, `/cartoes`), além da checagem no serviço.

## Deploy e DevOps

- Imagem: `Java/rei-dos-piratas/Dockerfile` (multi-stage, JRE 21, usuário não-root, porta 8080). O build do Docker **pula os testes** — eles precisam rodar no CI antes.
- AWS (alvo): `buildspec.yml` (CodeBuild) gera a imagem, publica no ECR e emite `imagedefinitions.json` (formato ECS). Fila SQS para eventos de cobrança. Serviço de execução ainda não definido (ECS Fargate / App Runner / Elastic Beanstalk em avaliação).
- Azure (legado): `.github/workflows/main_rei-dos-piratas-app.yml` e `azure-pipelines.yaml`. Não remova sem pedido explícito.
- Health check: `HealthRestController`.
- Infraestrutura como código ainda não existe; ao propor, prefira Terraform ou AWS CDK em uma pasta `infra/` na raiz.

## Front-end web (Thymeleaf)

- Controllers MVC em `infrastructure/mvc` (só delegam aos controllers de `interfaces`), templates em `src/main/resources/templates` (área do cliente em `conta/`). Rotas: `/web/conta/**` só cliente (`ENDERECO_MANAGE`); `/web/pedidos/**` e `/web/devolucoes/**` funcionário (`PEDIDO_WRITE`); `/web/simulador/**` só com `APP_SIMULADOR_EVENTOS=true`.
- Testes com `@WebMvcTest` renderizando o template real; permissões no `PermissoesIntegrationTest`.
- O login web exige contexto seguro (HTTPS ou `http://localhost`): cookies `Secure` e `navigator.locks` do `web-auth.js` não funcionam em `http://<IP>` (VM sem TLS).
- Detalhes nos agentes `frontend-analista` e `frontend-desenvolvedor`.

## Agentes do projeto (`.claude/agents/`)

- `backend-analista` / `backend-desenvolvedor`: análise (sem editar) e implementação TDD do back-end.
- `frontend-analista` / `frontend-desenvolvedor`: análise (sem editar) e implementação TDD das telas Thymeleaf.
- Cada agente traz o recorte deste arquivo de que precisa; **este arquivo prevalece**. Ao mudar uma regra aqui, atualize o agente correspondente (e vice-versa).

## Como trabalhar comigo (agente)

1. Para tarefas não triviais, apresente um plano curto (incluindo os casos de teste) antes de editar e espere aprovação.
2. Implemente em ciclos TDD (Red → Green → Refactor); veja a seção de TDD acima.
3. Faça mudanças pequenas e focadas; não refatore código fora do escopo pedido.
4. Rode `./mvnw test` ao final e informe o resultado.
5. Não faça `git commit`/`push` sem pedido. Quando pedirem, use o padrão do repo: `(feat)`, `(ref)`, `(fix)`, `(chore)`, `(test)`, `(docs)` + descrição em português. Ex.: `(feat) Implementação de estorno parcial via Asaas`.
6. Ao mudar comportamento, endpoint ou variável de ambiente, atualize o `README.md` do projeto Java e, se for convenção nova, este arquivo.
7. Mantenha `docs/refatoracao-clean-architecture.md` atualizado ao fim de cada etapa (estado da suíte, decisões, pendências).
8. Não altere arquivos que o usuário está editando no momento (código incompleto que não compila). Avise e, se precisar rodar testes, use uma cópia isolada do projeto.
9. Ao encontrar um bug fora do escopo da tarefa, registre e proponha a correção em vez de corrigir sem pedido.
