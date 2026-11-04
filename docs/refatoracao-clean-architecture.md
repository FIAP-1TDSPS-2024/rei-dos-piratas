# Refatoração Clean Architecture — estado do trabalho

Registro da sessão de 21/10/2026 (análise + itens 1 a 3 implementados). Serve de contexto para continuar o trabalho em uma nova sessão de agente. Atualize este arquivo ao concluir cada etapa.

## Situação em 04/11/2026 — integrações externas e estorno (etapas 0 a 7) concluídos, suíte verde (618 testes)

Etapas 0 e 1 commitadas em `883ff85`. Etapa 2 ainda **não commitada**.

Plano aprovado (ordem 0 → 1 → 2 → 5 → 3 → 4 → 7; a 6, estorno pela API, é entrega separada):

| Etapa | Conteúdo | Estado |
|---|---|---|
| 0 | ArchUnit com violações congeladas + WireMock | **feita** |
| 1 | `GatewayPagamento` + adaptador Asaas; DTOs Asaas em `infrastructure`; remover `CobrancaService` | **feita** |
| 2 | `EventoPagamento` de domínio montado no listener (sai `EventoCobrancaDto`/`EventoAsaasEnum` do `application`) | **feita** |
| 5 | Cobrança do cartão fora da transação: T1 grava pedido → cobrança sem transação → T2 registra resultado, via porta `ExecutorTransacional` (`TransactionTemplate`). Ajustes: `registrarPagamento` preenche `idCobranca` com o `payment.id` do evento se vazio; sem retentativa automática em timeout (pedido fica PENDENTE e o webhook resolve) | **feita** |
| 3 | `GatewayFrete` (Melhor Envio), regras de volume no domínio, token OAuth dentro do adaptador | **feita** |
| 4 | Webhook de rastreio: HMAC e parsing na infraestrutura, `EventoRastreio` de domínio | **feita** |
| 7 | ArchUnit estrito (sem freeze) e docs | **feita** |

### Etapa 0
- `pom.xml`: `archunit-junit5` 1.3.0 e `wiremock-standalone` 3.9.1 (escopo `test`).
- `ArquiteturaTest`: regra congelada "`domain`/`application` não dependem de `infrastructure`/`interfaces`" (store em `src/test/resources/archunit_store`, `allowStoreCreation=false` em `archunit.properties`) + regra estrita "`domain` não depende de `application`". Violação corrigida sai do store sozinha ao rodar o teste (versionar o store atualizado); violação **nova** falha. Store: 239 linhas → 181 após a etapa 1.

### Etapa 1
- Porta `domain/gateway/GatewayPagamento` (`registrarCliente`, `tokenizarCartao`, `criarCobranca`, `obterQrCodePix`, `obterLinhaDigitavel`) e records `SolicitacaoCobranca` (com `para(pedido, regraBoleto, hoje, ipRemoto)`: vencimento, descrição, token e IP só no cartão, cancelamento do registro só no boleto), `CobrancaCriada`, `CartaoTokenizado` (`toString` mascarado), `QrCodePix`, `LinhaDigitavel`.
- Adaptador `infrastructure/external_interface/asaas/GatewayPagamentoAsaas` (bean em `config/pagamento/GatewayPagamentoConfig`). À vista x parcelado (`value` x `installmentCount`+`totalValue`) é decisão do adaptador. `CobrancaAppClient` e todos os DTOs Asaas (inclusive `AsaasPaymentStatus`) foram para `infrastructure/external_interface/asaas[/dto]`.
- `GatewayPagamentoAsaasTest`: 15 testes de contrato com WireMock (cliente Feign real + Jackson do projeto): corpo das requisições PIX/cartão à vista/parcelado/boleto, mapeamento das respostas, recusa → `PagamentoRecusadoException`, 503 → `ApiExternaException`.
- **Bug corrigido (encontrado pelo WireMock):** `FreteFeignConfig` tinha `@Configuration`, o que tornava o interceptor do token do Melhor Envio **global** — o Bearer do Melhor Envio ia para o Asaas e a cobrança falhava se o token de frete estivesse inválido. Agora a classe não tem `@Configuration`; o `FreteTokenClient` usa `FreteTokenFeignConfig` (só o `ErrorDecoder`). Teste de regressão `chamadaAoAsaasNaoEnviaOTokenDoMelhorEnvio`.
- `ClienteServiceImpl`, `CartaoServiceImpl` e `PedidoServiceImpl` usam a porta; testes de serviço mockam `GatewayPagamento` (os detalhes de formato do Asaas foram para o teste do adaptador). `CobrancaService`/`CobrancaServiceImpl`/`CobrancaServiceConfig` removidos.
- QR Code: `PedidoController` devolve `interfaces/dto/pagamento/PixQrCodeOutDto` (via `PixQrCodeDtoMapper`), com o mesmo JSON de antes — protegido pelo teste de caracterização `obterQrCodePix_DeveManterOContratoJsonDoApp`.
- **Mudança de comportamento:** resposta de cobrança sem `status` deixou de ser tratada como cartão aprovado (só `CONFIRMED`/`RECEIVED` confirmam; o resto fica PENDENTE até o evento). O mock do Asaas em `PedidoFluxoIntegrationTest` passou a devolver o status como o Asaas real (`CONFIRMED` para cartão, `PENDING` para PIX/boleto); as asserções não mudaram.

### Etapa 2
- Domínio: `domain/entity/EventoPagamento` (`idEvento`, `tipo`, `tipoOriginal`, `ocorridoEm`, `idCobranca`, `pedidoId`, `motivoRecusa`; `motivoRecusaOuPadrao()`) e `domain/Enum/TipoEventoPagamento` (`isEstorno()`).
- `EventoCobrancaDto` e `EventoAsaasEnum` foram para `infrastructure/external_interface/asaas/dto`. `TradutorEventoAsaas` (mesmo pacote) mapeia o evento do Asaas para o domínio e descarta (vazio + log) evento sem id, sem pedido, de tipo desconhecido ou com `externalReference` que não é número.
- `EventoCobrancaListener` desserializa, traduz e só então chama `RastreioCobrancaService.processarEvento(EventoPagamento, payload)`. `PedidoService.processarEventoCobranca` e `EstornoService.processarEventoEstorno` recebem `EventoPagamento`. A idempotência continua no `EventoExterno`, gravando o nome original do evento (`tipoOriginal`) como antes.
- Testes novos: caracterização do `RastreioCobrancaServiceImpl` (antes sem teste), `EventoPagamentoTest`, `TradutorEventoAsaasTest` (mapeamento de todos os eventos e descartes) e `EventoCobrancaListenerTest` (JSON real → evento de domínio; JSON inválido não lança).
- **Correções de comportamento:**
  - `externalReference` não numérico lançava `NumberFormatException` depois de registrar o evento → marcado como falho e reprocessado sem fim. Agora é descartado no tradutor.
  - `PAYMENT_REFUND_DENIED` sem `additionalInfo` dava `NullPointerException` no `EstornoServiceImpl`; agora loga o motivo nulo.
  - O listener não loga mais o objeto do evento inteiro nem o payload de mensagem inválida (só id e tipo).
- ArchUnit: o store caiu para 132 linhas; não sobra violação de pagamento (o restante é frete e rastreio — etapas 3 e 4).

### Etapa 5 (commit `5afb10f`)
- Porta `domain/gateway/ExecutorTransacional` (`emTransacao`, `emTransacaoSemRetorno`) com adaptador `infrastructure/transacao/ExecutorTransacionalSpring` (`TransactionTemplate`, bean em `config/transacao/TransacaoConfig`).
- `PedidoServiceImpl.fazerPedido(Pedido, PagamentoCartao)` não é mais `@Transactional`: T1 `criarPedido` → tokenização e cobrança sem transação → T2 relê o pedido (`repository.findById`) e registra cartão, id da cobrança (se vazio), valor líquido e `CONFIRMADO` (só se ainda `PENDENTE`). PIX/boleto pelo mesmo método rodam em `executorTransacional.emTransacao(...)` (a autochamada não passava pelo proxy).
- Resultados: recusa → T2 `CANCELADO`/`RECUSADO` + estoque (ignorada se o evento já mudou o pedido) e 422; `ApiExternaException` na tokenização ou na cobrança → `CANCELADO`/`CANCELADO` + estoque e 502; `CobrancaSemRespostaException` (o adaptador traduz `feign.RetryableException`) → pedido pendente, 201, sem nova tentativa.
- Ajuste (a): `registrarPagamento` usa o `idCobranca` do evento quando o pagamento não tem (cobrança sem resposta); não sobrescreve um id já gravado.
- Testes: `ExecutorTransacionalSpringTest` (commit/rollback), contrato WireMock com atraso maior que o read-timeout (1 requisição só), 9 cenários do cartão no `PedidoServiceImplTest` com executor falso que registra se o trecho está em transação, 2 do id pelo evento, e integração `cartaoSemRespostaDoAsaasFicaPendenteGravadoForaDeTransacaoEOEventoCompletaACobranca` (verifica `TransactionSynchronizationManager.isActualTransactionActive()` no mock do Feign).
- Pendência registrada: pedido de cartão sem resposta cuja cobrança não existe fica pendente para sempre com estoque reservado → rotina de expiração (tarefa separada).

### Etapa 6 — estorno pela API do Asaas (commits `dd2f8a5`, `c1c6196`, `e01e85c`, `d1f0930` e o do endpoint)
- Decisões aprovadas: estorno da devolução na conclusão pelo funcionário; valor = itens pelo preço pago, + frete de ida no arrependimento do pedido inteiro; frete de volta nunca cobrado do cliente; pagamento tardio em pedido cancelado estornado automaticamente; link do boleto em `GET /pedidos/{id}/estornos`; cancelamento de pedido pendente cancela a cobrança no Asaas (falha só gera aviso).
- 6a: `Estorno` (filha de `Pagamento`, V42 `ESTORNOS`), `GatewayPagamento.estornar`/`cancelarCobranca` (WireMock), eventos de estorno do webhook no `EstornoServiceImpl` (antes só logava).
- 6b: `EstornoPagamentoService` (T1 registra → Asaas sem transação → T2 relê e grava; `FALHOU` sem lançar; sem resposta mantém `SOLICITADO`); `cancelarPedido` sem `@Transactional`, decide pelo status do pagamento (cartão aprovado em pedido ainda `AGUARDANDO_PAGAMENTO` também é estornado).
- 6c: `Devolucao.valorAReembolsar` e `concluirDevolucao` (antes um esqueleto); `PUT /devolucoes/**` já exigia `PEDIDO_WRITE`.
- 6d: `processarEventoCobranca` sem `@Transactional`: aplica o evento no `ExecutorTransacional` e estorna depois do commit. O teste que esperava o log de "estorno manual" foi reescrito para o novo comportamento (decisão 3).
- Endpoint `GET /pedidos/{id}/estornos` (coberto por `GET /pedidos/** → PEDIDO_READ`; dono ou funcionário via `findById`); o motivo de falha não sai da API.
- Pendências: reprocessar estorno `FALHOU`/`NEGADO` (hoje manual; a `uk_estornos_devolucao` impede uma segunda linha para a mesma devolução); devoluções parciais que somadas devolvem o pedido inteiro não incluem o frete de ida; o envio criado no Melhor Envio para pedido cancelado em `PREPARANDO_ENVIO` continua no carrinho de lá; notificar o cliente do link de estorno do boleto.

### Etapa 7 (commit `272b862`)
- `ArquiteturaTest` sem `freeze`: `archunit_store` e `archunit.properties` removidos. 7 regras estritas: as duas originais, `interfaces` sem `infrastructure`, `domain` sem Spring, `application` só com `org.springframework.transaction`, `domain`/`application` sem Feign e `@Service`/`@Component`/`@Configuration` só na infraestrutura.
- As regras extras foram sondadas antes de entrar: todas já passavam, exceto "sem Jackson no domínio" (5 violações em `domain/entity/Usuario`, dívida conhecida) — ficou fora para não voltar a congelar; entra quando o `Usuario` for limpo.

### Etapa 4 (commit `e74f7ef`)
- Domínio: `domain/entity/EventoRastreio` (tipo, nome original, envio, status, protocolo, código e URL de rastreio, data de entrega já no fuso da loja) e `domain/Enum/TipoEventoRastreio`.
- Infraestrutura: `melhorenvio/TradutorEventoRastreio` (lê o corpo, converte `delivered_at` para `America/Sao_Paulo`, descarta sem id/UUID inválido/JSON inválido) e `melhorenvio/WebhookRastreioMelhorEnvio` (verifica o HMAC e entrega ao serviço). `RastreioWebhookDto`/`RastreioDataDto` foram para `melhorenvio/dto`.
- `RastreioRestController` chama o webhook da infraestrutura direto; `RastreioController` (interfaces) foi removido. `RastreioService.processarEvento(EventoRastreio)` só roteia para pedido ou devolução; `rastreioPedidoWebhook`/`rastreioDevolucaoWebhook` recebem `EventoRastreio` (`order.generated` e `order.ready-to-print` viram o mesmo tipo, tratados igual).
- Decisões aprovadas: assinatura comparada com `MessageDigest.isEqual` (tempo constante) e **401** (`AssinaturaInvalidaException`) quando ausente ou inválida — antes era `equals` e 200 silencioso; falha ao processar evento válido **sobe (500)** — antes o `catch (Exception)` engolia e o evento se perdia; o corpo do webhook não é mais logado (tem dados do cliente). `HmacUtil`, `SecurityConfig` e permissões não mudaram.
- Testes: caracterização do roteamento antes da mudança, `TradutorEventoRastreioTest` (19), `WebhookRastreioMelhorEnvioTest` (6), `RastreioRestControllerTest` (200/401/500); integração passa pelo webhook real com corpo assinado.
- ArchUnit: store vazio. Próximo: etapa 7 (regra estrita, sem freeze).

### Etapa 3 (commits `7b6529d` fix, `04ac2a2` 3a–3c, `a6ef9b8` 3d)
- **Bug corrigido antes (`7b6529d`):** `criarPedido` fazia `findFirst()` antes do `filter` e só aceitava o serviço de frete se fosse a primeira opção da cotação.
- Porta `domain/gateway/GatewayFrete`: `cotar`, `criarEnvio`, `criarEnvioReverso`, `comprarEnvios`, `gerarEtiquetas`, `obterLinkImpressao`. Tipos: `OpcaoFrete`, `Transportadora`, `SolicitacaoEnvio` (+ `para(pedido, enderecoLoja, loja)`), `SolicitacaoEnvioReverso` (+ `para(devolucao, servico)`), `ParteEnvio` (sem CPF/e-mail/telefone/endereço no `toString`), `ProdutoEnvio`, `EnvioCriado`, `ResultadoCompraEnvios`, `ResultadoEtiqueta`.
- `domain/entity/Volume.para(itens)`: regra das dimensões da caixa, antes duplicada no pedido e na devolução.
- Adaptador `infrastructure/external_interface/melhorenvio/GatewayFreteMelhorEnvio`; `FreteAppClient`, `FreteTokenClient` e todos os DTOs do Melhor Envio (exceto os do webhook de rastreio) em `melhorenvio[/dto]`. `FeignException` de comunicação vira `ApiExternaException` no adaptador.
- `FreteService` ficou só com a cotação (usada pelo `POST /frete` e pelo pedido); `PedidoServiceImpl` e `DevolucaoServiceImpl` usam a porta. `POST /frete` devolve `OpcaoFreteOutDto` com o mesmo JSON (teste de caracterização `FreteRestControllerTest`).
- 3d: `TokenService`/`TokenServiceImpl` viraram `melhorenvio/ProvedorTokenMelhorEnvio` (usado só pelo interceptor do `FreteFeignConfig`). Credenciais via `melhorenvio.client-id/client-secret/redirect-uri` (`${ME_CLIENT_ID:}`, `${ME_SECRET:}`, `${ME_REDIRECT_URI:}`) em `CredenciaisMelhorEnvio` (toString mascarado), em vez de `System.getenv`. `Token`/`TokenRepository` continuam no domínio (decisão: persistência, sem migration). Os testes do `TokenServiceImplTest` foram para `ProvedorTokenMelhorEnvioTest`.
- Testes: contrato WireMock do adaptador (13), `VolumeTest`, `SolicitacaoEnvioTest`, caracterização de `pagarPedido`/organizar/etiquetas/impressão antes da migração.
- Mudança pequena de comportamento: compra de fretes com `error` sem `message` passa a devolver o `error` (antes o pedido devolvia `null` — parecia sucesso — e não avançava o status). Na devolução já era assim.
- ArchUnit: store de 132 para 45 linhas; resta só o rastreio (etapa 4).
- Registrado para depois (fora do escopo): `pedido.setDataPrevisaoEntrega(LocalDate.now()...)` usa o fuso da JVM, não `America/Sao_Paulo`; `DevolucaoServiceImpl` lança `IllegalStateException` quando falta `ME_SERVICE_DEVOLUCAO` (vira 500); falha ao obter o token do Melhor Envio vira `ResourceNotFoundException` (404) em vez de 502.

## Situação anterior

- Itens 1, 2 e 3 commitados em `88d8799` (junto com o fluxo de cobrança PIX do usuário).
- Sessão de 21/10/2026 (tarde), **não commitada**:
  - As 24 falhas antigas foram corrigidas com aval do usuário (testes desatualizados após o fluxo PIX): `PedidoServiceImplTest` e `PedidoFluxoIntegrationTest` passam `tipoPagamento=PIX` e mockam `criarNovaCobranca`; `DatabaseMigrationIntegrationTest` compara as migrations aplicadas com as encontradas (não usa mais número fixo). Novo teste de caracterização `fazerPedido_PixDeveCriarCobrancaDoPedidoEGuardarIdEValorLiquido`.
  - **4 testes Red intencionais** (fluxo de cartão, para o usuário implementar em TDD) em `PedidoServiceImplTest`: `processarEventoCobranca_PagamentoConfirmadoDoCartaoDeveLiberarPedidoParaEnvio`, `..._PagamentoRecebidoDePedidoJaLiberadoNaoDeveFalharNemCriarOutroFrete`, `..._CartaoRecusadoDeveCancelarPedidoEDevolverEstoque` (2 casos).
  - Segurança/config: `jwt.secret` sem valor padrão (exige `JWT_SECRET`), `spring.jpa.show-sql` via `JPA_SHOW_SQL` (padrão `false`), `data/*.db` removido do índice e ignorado.

## Fluxo de pagamento com cartão de crédito (desenho aprovado; testes Red escritos; implementação do usuário em TDD)

### Decisões (21/10/2026)

| # | Tema | Decisão |
|---|---|---|
| 1 | Ordem das operações | Grava pedido + baixa de estoque e **commita**; depois tokeniza e cobra. Recusa → pedido cancelado e estoque devolvido. |
| 2 | Recusa síncrona do Asaas | Retorna **erro ao cliente** (HTTP 422 com o motivo). O pedido cancelado e o pagamento `RECUSADO` ficam gravados. |
| 3 | Onde fica a tokenização | `CartaoService` próprio (como `ClienteService.obterOuCriarClienteCobranca`). |
| 4 | Cartões salvos | Tabela `CARTOES_CLIENTE`; o cliente pode informar um cartão novo **ou** o id de um salvo. |
| 5 | Titular | Sempre os dados do cliente (nome, e-mail, CPF, celular) e do endereço de entrega (CEP, número). |
| 6 | Parcelamento | Entra agora. Regra configurável: valor mínimo `PAGAMENTO_PARCELAMENTO_VALOR_MINIMO` (padrão 100.00) e máximo `PAGAMENTO_PARCELAMENTO_MAX_PARCELAS` (padrão 3). Abaixo do mínimo, só à vista. |
| 7 | Entrada | Dados do cartão vêm **junto do pedido** (`PedidoInDto`/`PedidoCarrinhoInDto`), opcionais; obrigatórios quando `tipo_pagamento=CREDIT_CARD`. |
| 8 | Tabelas | Conforme abaixo. |

### Fatos do Asaas (confirmar na documentação)
- Tokenização: `POST /v3/creditCard/tokenizeCreditCard` com `customer`, `creditCard`, `creditCardHolderInfo` e `remoteIp` → `creditCardToken`, `creditCardNumber` (4 últimos), `creditCardBrand`. Em produção a tokenização pode precisar ser habilitada pelo Asaas.
- Cobrança: `billingType=CREDIT_CARD` + `creditCardToken` + `remoteIp`. À vista usa `value`; parcelado usa `installmentCount` + `totalValue`. Resposta **síncrona**: aprovada (`CONFIRMED`) ou HTTP 400 se recusada.
- Eventos: `PAYMENT_CONFIRMED` logo após a aprovação; `PAYMENT_RECEIVED` só quando o valor é creditado (~30 dias).

### Fluxo de `fazerPedido` com cartão
1. Valida entrada **antes de gravar**: `CREDIT_CARD` exige cartão (novo ou salvo); parcelas válidas pela `RegraParcelamento` sobre o valor total (já com frete).
2. Transação 1: calcula frete/total, baixa estoque, obtém/cria cliente Asaas, grava pedido (`AGUARDANDO_PAGAMENTO`). Commit.
3. Fora da transação: `CartaoService.obterOuTokenizar(...)` → `CartaoCliente`; cria a cobrança com o token.
4. Transação 2 (via `pedidoRepository.update`, o pagamento vai junto com o pedido):
   - aprovado → `pedido.setPagamento(...)` com `CONFIRMADO`, id da cobrança, valor líquido, parcelas e cartão;
   - recusado → `pedido.setPagamento(...)` com `RECUSADO` e motivo, cancela o pedido e devolve o estoque; lança `PagamentoRecusadoException` (→ 422).
5. O pedido vai para `PREPARANDO_ENVIO` pelo evento `PAYMENT_CONFIRMED` (mesmo caminho do PIX com `RECEIVED`).

Atenção: com `@Transactional` em `fazerPedido`, chamar outro método `@Transactional` da mesma classe não abre transação nova (o proxy do Spring é ignorado). Use `TransactionTemplate` ou separe os passos em outro bean.

### Modelo: `Pedido` 1–1 `Pagamento` (decisão de 21/10/2026)
- **Um pagamento por pedido.** `Pedido` é o agregado e contém `Pagamento pagamento`; `Pagamento` não referencia o pedido (evita ciclo no `toString`/`equals` do Lombok). Não há `PagamentoRepository`: o pagamento é gravado em cascata pelo `PedidoRepository`.
- JPA: `JpaPagamentoEntity` é o lado dono (`@OneToOne` com `pedido_id` único); `JpaPedidoEntity.pagamento` é `mappedBy`, `cascade = ALL`, `orphanRemoval`. O cartão é associado só pelo id (sem cascade) — gravado pelo `CartaoClienteRepository`.
- `CartaoCliente` → `Cliente` (`@ManyToOne`); `CartaoClienteRepositoryImpl` usa `EntityManager.getReference` para o cliente.
- Com recusa, o pedido fica `CANCELADO` com o pagamento `RECUSADO`; uma nova tentativa é um novo pedido.
- **Campos de pagamento saíram de `Pedido`** (`tipoPagamento`, `idCobranca`, `valorLiquido`; colunas removidas na V40). O construtor `Pedido(cliente, endereço, itens, serviço, tipoPagamento)` cria `Pagamento.pendente(tipo)` (status `PENDENTE`, 1 parcela, data de criação). `Pedido.getTipoPagamento()` é derivado do pagamento. O `PedidoOutDto` mantém `tipo_pagamento`/`valor_liquido`/`id_cobranca` (contrato do app mobile inalterado), lidos do pagamento.
- `fazerPedido` preenche `pagamento.valor` junto com o `valorTotal` (antes do `create`, a coluna é NOT NULL) e, no PIX, `idCobranca`/`valorLiquido` após criar a cobrança. `obterQrCodeCobrancaPix` usa o `idCobranca` do pagamento.
- **Ao mudar o pagamento, altere o objeto existente** (`pedido.getPagamento().setStatus(...)`). Trocar por um `new Pagamento(...)` sem id faz o Hibernate inserir o novo antes de apagar o antigo e viola o `pedido_id` único.
- Validação: `Pagamento`, `CartaoCliente`, `DadosCartaoCredito`, `PagamentoCartao` e `RegraParcelamento` têm Bean Validation (mensagens em português; as de `DadosCartaoCredito` não incluem o valor informado). `Pedido.pagamento` e `PagamentoCartao.novoCartao` têm `@Valid`. **Nenhum serviço chama o `Validator` ainda** — ao implementar o fluxo de cartão, injetar o `Validator` (como em `ProdutoServiceImpl`) e validar `PagamentoCartao` antes de gravar.

### Tabelas — **criadas**: `V38__create_pagamentos_e_cartoes_cliente.SQL`, `V39__backfill_pagamentos_pedidos.SQL` e `V40__drop_campos_pagamento_pedidos.SQL`

`CARTOES_CLIENTE`
| coluna | tipo | obs. |
|---|---|---|
| id | BIGINT identity PK | |
| cliente_id | BIGINT NOT NULL FK `CLIENTES` | |
| token | VARCHAR(100) NOT NULL | `creditCardToken` do Asaas; único |
| ultimos_digitos | VARCHAR(4) NOT NULL | |
| bandeira | VARCHAR(30) NOT NULL | |
| ativo | BOOLEAN NOT NULL DEFAULT TRUE | cliente pode remover |
| data_cadastro | TIMESTAMP NOT NULL | |

`PAGAMENTOS`
| coluna | tipo | obs. |
|---|---|---|
| id | BIGINT identity PK | |
| pedido_id | BIGINT NOT NULL FK `PEDIDOS` | **único** (um pagamento por pedido) |
| tipo | VARCHAR(20) NOT NULL | `PIX`, `BOLETO`, `CREDIT_CARD` (CHECK) |
| status | VARCHAR(20) NOT NULL | `PENDENTE`, `CONFIRMADO`, `RECEBIDO`, `RECUSADO`, `CANCELADO`, `ESTORNADO` (CHECK) |
| id_cobranca | VARCHAR(50) | id do pagamento no Asaas; único quando preenchido; nulo se recusado antes de criar |
| valor | NUMERIC(12,2) NOT NULL | |
| valor_liquido | NUMERIC(12,2) | |
| parcelas | INTEGER NOT NULL DEFAULT 1 | |
| cartao_cliente_id | BIGINT FK `CARTOES_CLIENTE` | só para cartão |
| motivo_recusa | VARCHAR(255) | |
| data_criacao | TIMESTAMP NOT NULL | |
| data_confirmacao | TIMESTAMP | |

- Backfill (V39, idempotente): um `PAGAMENTOS` para cada pedido com `id_cobranca` (`AGUARDANDO_PAGAMENTO` → `PENDENTE`, `CANCELADO` → `CANCELADO`, demais → `RECEBIDO`). A V40 remove `tipo_pagamento`/`id_cobranca`/`valor_liquido` de `PEDIDOS` (irreversível; os dados já estão em `PAGAMENTOS`). Os testes que reexecutavam a V39 foram removidos com aval do usuário, pois o script lê colunas que a V40 apaga.
- Webhook: localizar o pagamento por `payment.id` (`id_cobranca`); `externalReference` continua sendo o id do pedido.
- SQL compatível com PostgreSQL e H2 (seguir o estilo da V37: `GENERATED BY DEFAULT AS IDENTITY`, CHECKs nomeados).

### Assinaturas propostas (stubs para o passo Red)
- `domain/entity/DadosCartaoCredito` (record: titular, numero, mesValidade, anoValidade, cvv) — **não persistido**, `toString` mascarado.
- `domain/entity/PagamentoCartao` (record: `Long idCartaoSalvo`, `DadosCartaoCredito novoCartao`, `int parcelas`, `String ipRemoto`) — exatamente um entre `idCartaoSalvo` e `novoCartao`.
- `domain/entity/RegraParcelamento` (record: `BigDecimal valorMinimo`, `int maxParcelas`) com `validar(BigDecimal valorTotal, int parcelas)`; bean montado com as variáveis de ambiente.
- `domain/entity/CartaoCliente`, `domain/entity/Pagamento`, `domain/Enum/StatusPagamentoEnum`; `domain/repository/CartaoClienteRepository` (sem `PagamentoRepository`: o pagamento faz parte do agregado `Pedido`).
- `domain/exceptions/PagamentoRecusadoException extends RegraDeNegocioException` → 422 no `GlobalExceptionHandler`.
- `application/service/CartaoService.obterOuTokenizar(Cliente, Endereco, PagamentoCartao): CartaoCliente`.
- `PedidoService.fazerPedido(Pedido, PagamentoCartao)` (`null` para PIX/boleto) e `CarrinhoService.finalizarCompra(..., PagamentoCartao)`.
- `CobrancaAppClient.tokenizarCartao(TokenizacaoCartaoRequestDto): TokenizacaoCartaoResponseDto`; `PagamentoCobrancaRequestDto` ganha `creditCardToken`, `remoteIp`, `totalValue` e perde `creditCard`.
- `AsaasErrorDecoder`: HTTP 400 em `criarNovaCobranca`/`tokenizarCartao` de cartão vira `PagamentoRecusadoException` com a `description` do Asaas (hoje vira `ApiExternaException` → 502).
- `RestController`: lê o IP (`X-Forwarded-For` atrás do load balancer, senão `remoteAddr`) e repassa no `PagamentoCartao`.

### Consulta de cartões salvos — `GET /cartoes` (implementado e verde)
- `CartaoService.listarCartoesDoClienteAtual()`: usa o `UsuarioAtualService` (sem id vindo de fora), devolve só cartões **ativos** e lança `RegraDeNegocioException` se a conta não for de cliente (ids de cliente e funcionário podem coincidir). `findAllByClienteId` fica para uso interno (corrigido: chamava a si mesmo → `StackOverflowError`).
- `CartaoClienteController`/`Impl`, `CartaoClienteDtoMapper`, `CartaoClienteOutDto` (`id`, `bandeira`, `ultimos_digitos`, `data_cadastro` — **sem token**), `CartaoRestController` e `CartaoClienteControllerConfig`.
- `SecurityConfig`: `/cartoes/**` exige `ENDERECO_MANAGE` (só o perfil CLIENT tem); coberto em `PermissoesIntegrationTest` (cliente 200, admin 403, anônimo 401).
- Logs: nenhum log com token, dígitos ou bandeira; `CartaoCliente.toString` exclui token e últimos dígitos (`@ToString.Exclude`).

### Situação em 30/10/2026 — pagamento por boleto, suíte verde (400 testes)
Decisões aprovadas: vencimento em 3 dias corridos (`PAGAMENTO_BOLETO_DIAS_VENCIMENTO`); `PAYMENT_OVERDUE` cancela pedido + devolve estoque + pagamento `CANCELADO` (boleto **e PIX**, que antes só logava "atualizando para CANCELADO" sem fazer nada); registro do boleto cancelado 1 dia após o vencimento; pagamento que chega com pedido cancelado não reativa e é sinalizado em log `ERROR` para estorno manual; sem juros/multa/desconto.
- Domínio: `Pagamento.urlBoleto` (`@Size(max=500)`) e `dataVencimento`; `RegraBoleto` (dias de vencimento, `DIAS_CANCELAMENTO_REGISTRO = 1`); `DadosBoleto` (linha digitável, código de barras, URL, vencimento). V41 adiciona `url_boleto` e `data_vencimento` em `PAGAMENTOS`.
- Serviço: case `BOLETO` em `definirPagamentoCobrancaRequest` (`value`, vencimento no fuso da loja, `daysAfterDueDateToRegistrationCancellation`); guarda `bankSlipUrl` e vencimento; `obterBoleto` consulta `GET /v3/payments/{id}/identificationField`; `registrarVencimento`; aviso de estorno em `registrarPagamento`.
- Interfaces/REST: `GET /pedidos/{id}/boleto` (`BoletoOutDto`, `BoletoDtoMapper`), `url_boleto` no `PedidoOutDto`. Config: `BoletoConfig`.
- TDD: testes de domínio, persistência, serviço (13), controller, REST e mapper falharam antes da implementação; os 2 de integração (`PedidoFluxoIntegrationTest`) foram escritos depois do serviço e passaram de primeira (confirmam o fluxo com banco). Ajuste de setup: o mock do Asaas na integração passou a devolver `bankSlipUrl`.
- Pendências: estorno pela API (`estornarCobranca` comentado no `CobrancaAppClient`); notificar o cliente do boleto/vencimento; `GatewayPagamento` (o boleto acrescentou mais uma chamada direta ao Feign no serviço).

### Situação em 26/10/2026 — `remoteIp` de volta, suíte verde (378 testes)
- Teste manual no sandbox: a cobrança com cartão voltava "Transação não autorizada" (recusa genérica, código `invalid_creditCard`). Suspeitas: cartão de teste de recusa do sandbox (`5184019740373151`/`4916561358240741`; o de aprovação é `4444 4444 4444 4444`) e ausência do `remoteIp`, que o Asaas documenta como obrigatório.
- `remoteIp` voltou: `PagamentoCartao.ipRemoto` (`@NotBlank`), `PedidoRestController` extrai o IP (`X-Forwarded-For` → primeiro IP; senão `remoteAddr`), `PedidoController.fazerPedido(PedidoInDto, String ipRemoto)`, `PagamentoCartaoCreditoMapper.toEntity(dto, ip)`, e o campo `remoteIp` em `TokenizacaoCartaoRequestDto` e `PagamentoCobrancaRequestDto` (nulo no PIX).
- `AsaasErrorDecoder`: loga `código(s)` + descrição; recusa decidida pelo código `invalid_creditCard` só em `criarNovaCobranca`/`tokenizarCartao` (antes procurava o texto "Transação não autorizada" no corpo).
- `spring.cloud.openfeign.client.config.cobranca-app.read-timeout=60000` (sem teste automatizado: os testes usam outro `application.properties`).
- TDD: 12 testes novos (domínio, mapper, `CartaoService`, `PedidoService`, controller, REST e decoder); 11 falharam por asserção antes da implementação. Testes existentes só ganharam o argumento de IP nas construções de `PagamentoCartao` e nas chamadas do controller/mapper.

### Situação em 23/10/2026 — fluxo de cartão completo, suíte verde
**Suíte: 363 testes, 0 falhas** (`./mvnw test`). Pronto para teste manual da aplicação.

- **IP remoto removido por enquanto** (decisão do usuário): saiu de `PagamentoCartao`, `TokenizacaoCartaoRequestDto`, `PagamentoCobrancaRequestDto` e dos testes; o stub `PedidoController.fazerPedido(PedidoInDto, String ipRemoto)` foi removido. As menções a `remoteIp`/`ipRemoto` nas seções acima são históricas.
- **`PedidoServiceImpl.fazerPedido(Pedido, PagamentoCartao)`** (`@Transactional(noRollbackFor = PagamentoRecusadoException.class)`): valida o cartão (`RegraDeNegocioException` se faltar), grava o pedido (`criarPedido`: frete, total, parcelamento, estoque, cliente Asaas), tokeniza **depois** de o cliente Asaas existir e cria a cobrança. Recusa (na tokenização ou na cobrança) → pedido `CANCELADO`, estoque devolvido, pagamento `RECUSADO` com motivo, tudo gravado; depois o erro sobe (422). PIX/boleto delegam para `fazerPedido(Pedido)`.
- Cobrança de cartão: à vista envia `value`; parcelado envia `installmentCount` + `totalValue`. Aprovado na resposta síncrona (status `CONFIRMED`/`RECEIVED` ou ausente) → pagamento `CONFIRMADO` com `dataConfirmacao`; `AWAITING_RISK_ANALYSIS` → continua `PENDENTE` até o webhook. O pedido só vai para `PREPARANDO_ENVIO` pelo evento.
- **Eventos Asaas** (`processarEventoCobranca`, agora `@Transactional`): `PAYMENT_CONFIRMED` → pagamento `CONFIRMADO` e libera o envio; `PAYMENT_RECEIVED` → pagamento `RECEBIDO` (e libera, se ainda aguardando); `PAYMENT_CREDIT_CARD_CAPTURE_REFUSED`/`PAYMENT_REPROVED_BY_RISK_ANALYSIS` → cancela, devolve estoque, `RECUSADO` com `denialReason`. Tudo idempotente: evento repetido ou fora de ordem não falha nem devolve estoque duas vezes.
- `devolverEstoque` extraído de `cancelarPedido` e reaproveitado na recusa.
- `PedidoControllerImpl`: cartão ausente lança `RegraDeNegocioException` (400) em vez de `IllegalArgumentException` (500).
- `CartaoServiceImpl`: cartão salvo inexistente → `ResourceNotFoundException` (antes vazava `NoSuchElementException` → 500).
- **Bug corrigido — data de entrega/conclusão em UTC**: `PedidoServiceImpl` e `DevolucaoServiceImpl` convertem `deliveredAt` para `America/Sao_Paulo` antes de `toLocalDate()`. Antes, entregas após 21h de Brasília ficavam com a data do dia seguinte. `PedidoFluxoIntegrationTest` passou a comparar com `LocalDate.now(America/Sao_Paulo)` (antes dependia do fuso da JVM).
- Testes ajustados com aval: nomes sem "Ip"; removida do teste à vista a asserção `creditCardToken()).isNull()` que contradizia `isEqualTo("tok_abc123")`.

Pendências para a próxima rodada (arquitetura e regras):
- **Cobrança dentro da transação**: o cartão é cobrado antes do commit. Se o commit falhar depois da aprovação, há cobrança sem pedido. Plano: gravar o pedido numa transação, cobrar fora, registrar o resultado em outra (`TransactionTemplate` ou bean separado) — entra junto com o item 4 (`GatewayPagamento`).
- Na recusa, o cartão tokenizado fica salvo em `CARTOES_CLIENTE` (o cartão é válido; a cobrança é que foi negada). Decidir se mantém.
- Fuso `America/Sao_Paulo` fixo no código (duas classes): avaliar configuração.
- `PAYMENT_OVERDUE`/`PAYMENT_DELETED` ainda só logam (PIX vencido não cancela o pedido).
- Item 4 (`GatewayPagamento`/`GatewayFrete`), item 5 (`Usuario` com Jackson), preço do carrinho vs. preço atual, README raiz.

### Estado dos testes Red (21/10/2026)
Suíte: **362 testes, 35 falhando — todos testes Red intencionais** (31 do cartão + 4 de eventos de cobrança); nenhum teste antigo quebrou. Todos falham por asserção ou por `UnsupportedOperationException` dos stubs.

Verdes nesta etapa: `PedidoTest`, `PedidoDtoMapperTest`, QR Code PIX no `PedidoServiceImplTest`, `*ValidacaoTest` (5 classes, 59 testes) e `PagamentoPersistenciaIntegrationTest` (8 testes). No `PedidoFluxoIntegrationTest`, o mock do Asaas passou a gerar ids de cobrança diferentes por chamada (o `id_cobranca` é único em `PAGAMENTOS`).

Persistência **implementada e verde**: V38/V39/V40, `JpaPagamentoEntity`, `JpaCartaoClienteEntity`, `JpaPagamentoMapper`, `JpaCartaoClienteMapper`, `JpaPedidoMapper` com pagamento, `CartaoClienteRepositoryImpl`, `CartaoClienteRepositoryConfig`, `CartaoServiceConfig` (inclui o bean `RegraParcelamento` com `PAGAMENTO_PARCELAMENTO_VALOR_MINIMO`/`PAGAMENTO_PARCELAMENTO_MAX_PARCELAS`) e `PedidoServiceConfig` com o construtor completo (o construtor temporário foi removido).

Stubs criados (só para compilar): `DadosCartaoCredito`, `PagamentoCartao`, `RegraParcelamento.validar`, `CartaoCliente`, `Pagamento`, `StatusPagamentoEnum`, `CartaoClienteRepository`, `PagamentoRecusadoException`, `CartaoService`/`CartaoServiceImpl`, `PedidoService.fazerPedido(Pedido, PagamentoCartao)`, `PedidoController.fazerPedido(PedidoInDto, String)`, `CartaoPedidoInDto` + campo `cartao` em `PedidoInDto` (construtor antigo mantido), `CobrancaAppClient.tokenizarCartao` + DTOs, campos `creditCardToken`/`remoteIp`/`totalValue` em `PagamentoCobrancaRequestDto`.

Pontos para o usuário na implementação:
- Eventos do Asaas (`PAYMENT_CONFIRMED`/`RECEIVED`/recusas) devem atualizar também o status do `Pagamento` (`CONFIRMADO`/`RECEBIDO`/`RECUSADO`) e a `dataConfirmacao`.
- REST "`CREDIT_CARD` sem cartão → 400 sem chamar o controller": sugestão de `@AssertTrue` em `PedidoInDto`.
- Fora da lista aprovada (sem testes ainda): `PedidoControllerImpl.fazerPedido(PedidoInDto, String)` (mapear `CartaoPedidoInDto` → `PagamentoCartao`), `CarrinhoService.finalizarCompra` com cartão, remoção de `creditCard` do `PagamentoCobrancaRequestDto`, e `toString` mascarado também em `CartaoPedidoInDto` (o `record` expõe o cartão se o DTO for logado).

### Testes Red escritos
Domínio
- `RegraParcelamentoTest`: permite até o máximo de parcelas quando o total ≥ mínimo; recusa acima do máximo; recusa parcelar (>1) abaixo do mínimo; à vista sempre permitido; recusa parcelas < 1.
- `DadosCartaoCreditoTest`: `toString` não contém número completo nem CVV.
- `PagamentoCartaoTest`: exige exatamente um entre cartão salvo e cartão novo.

`CartaoServiceImplTest`
- cartão novo: tokeniza com o id do cliente Asaas, dados do cliente/endereço como titular e o IP;
- cartão novo: grava `CartaoCliente` com token, 4 últimos dígitos e bandeira (nada de número/CVV);
- cartão salvo: usa o token gravado sem chamar o Asaas;
- cartão salvo de outro cliente → `ResourceNotFoundException`;
- cartão salvo inativo → `RegraDeNegocioException`.

`PedidoServiceImplTest`
- `CREDIT_CARD` sem cartão → `RegraDeNegocioException`, sem gravar pedido nem chamar o Asaas;
- parcelamento inválido → `RegraDeNegocioException`, sem gravar pedido;
- à vista: cobrança com `creditCardToken`, `remoteIp` e `value`, sem dados do cartão;
- parcelado: cobrança com `installmentCount` e `totalValue`;
- aprovado: grava `Pagamento` `CONFIRMADO` com id da cobrança, valor líquido, parcelas e cartão;
- recusado: lança `PagamentoRecusadoException`, grava `Pagamento` `RECUSADO` com motivo, cancela o pedido e devolve o estoque;
- PIX continua funcionando com `PagamentoCartao` nulo.

`AsaasErrorDecoderTest`
- 400 na criação de cobrança → `PagamentoRecusadoException` com a descrição do Asaas; demais status inalterados.

REST / integração
- `PedidoRestControllerTest`: `CREDIT_CARD` sem cartão → 400; recusa → 422; IP vindo de `X-Forwarded-For`.
- `PedidoFluxoIntegrationTest`: cartão aprovado chega a `PREPARANDO_ENVIO` via `PAYMENT_CONFIRMED`; recusa deixa pedido `CANCELADO`, estoque restaurado e `PAGAMENTOS` com `RECUSADO` (exige a V38).

## Violações encontradas (análise inicial)

| # | Violação | Status |
|---|---|---|
| 1 | Mappers de DTO em `infrastructure/mapper/dto` usados por `interfaces`; `JpaItemProdutoMapper::toPedido` usado como regra de negócio | **Feito** |
| 2 | `SecurityContextHolder`/`CustomUserDetails` em `CarrinhoServiceImpl`, `EnderecoServiceImpl`, `PedidoServiceImpl`, `PedidoControllerImpl` | **Feito** |
| 3 | `@Value("${ME_SERVICE_DEVOLUCAO}")` na entidade `Devolucao` (nunca aplicado → NPE ao aprovar devolução) | **Feito (era bug real)** |
| 4 | Serviços chamam clientes Feign diretamente e trafegam DTOs de fornecedor (Asaas/Melhor Envio) | **Pendente — plano abaixo** |
| 5 | `domain/entity/Usuario` com anotações Jackson | Pendente |
| — | `@Transactional` em 9 serviços; `ObjectMapper`/`HmacUtil` em `RastreioServiceImpl`; domínio anêmico (`PedidoServiceImpl` ~730 linhas) | Aceito por ora |

### O que mudou nos itens 1–3
- Mappers DTO → `interfaces/mapper/{endereco,frete,negocio,usuarios}`; `PageMapper` → `infrastructure/mapper/jpa`.
- `ItemProdutoCarrinho.paraItemPedido()` no domínio (substitui `JpaItemProdutoMapper.toPedido`).
- `UsuarioAtualService` ganhou `possuiPermissao(String)` e `identidadeSeAutenticado()` (vazio = chamada interna/webhook, que tem acesso liberado em `validarAcessoAoPedido`).
- `DevolucaoServiceImpl` recebe `servicoEntregaDevolucao` (config `ME_SERVICE_DEVOLUCAO`); grava na solicitação e usa como fallback na aprovação; falha com `IllegalStateException` clara se não configurado.
- Testes novos: `ItemProdutoCarrinhoTest`, `SpringUsuarioAtualTest`, `EnderecoServiceImplTest`, casos novos em `PedidoServiceImplTest`, `CarrinhoServiceImplTest`, `DevolucaoServiceImplTest`.

## Plano do item 4 — portas e adaptadores para integrações

Interfaces em `domain/gateway` (mesma lógica de `domain/repository`), adaptadores em `infrastructure/external_interface/<fornecedor>`.

```java
public interface GatewayPagamento {
    String registrarCliente(Cliente cliente, Endereco endereco);      // id externo
    CobrancaCriada criarCobranca(SolicitacaoCobranca solicitacao);
    QrCodePix obterQrCodePix(String idCobranca);
}
public record SolicitacaoCobranca(String idClienteExterno, TipoPagamentoEnum tipo, BigDecimal valor,
        LocalDate vencimento, String descricao, String referenciaPedido, Integer parcelas) {}
public record CobrancaCriada(String idCobranca, BigDecimal valorLiquido) {}
public record QrCodePix(String imagemBase64, String copiaECola, LocalDateTime expiraEm) {}

public interface GatewayFrete {
    List<OpcaoFrete> cotar(String cepDestino, List<ItemProdutoPedido> itens);
    EnvioCriado criarEnvio(SolicitacaoEnvio solicitacao);
    EnvioCriado criarEnvioReverso(SolicitacaoEnvio solicitacao);
    void comprarEnvios(List<String> idsEnvio);
    void gerarEtiquetas(List<String> idsEnvio);
    String obterLinkImpressao(List<String> idsEnvio);
}
```

- Regra de negócio (vencimento, descrição, parcelas) fica no domínio/serviço (`SolicitacaoCobranca.para(pedido)`); tradução para o fornecedor e `FeignException → ApiExternaException` ficam no adaptador.
- DTOs do Asaas/Melhor Envio saem de `interfaces/dto` para `infrastructure/external_interface/<fornecedor>/dto`.
- `CobrancaService`/`CobrancaServiceImpl` hoje são código morto (bean criado, ninguém injeta) e são substituídos pelo adaptador. **Problema atual:** `PedidoServiceImpl`/`ClienteServiceImpl` chamam `CobrancaAppClient` direto e falhas de rede escapam como `FeignException` crua.
- `TokenServiceImpl` (OAuth Melhor Envio) vai para dentro do adaptador de frete.
- Webhooks de entrada: listener/controller converte `EventoCobrancaDto`/`RastreioWebhookDto` em eventos de domínio (ex.: `EventoPagamento(idCobranca, TipoEventoPagamento, ocorridoEm)`); mapeamento de `EventoAsaasEnum` fica no adaptador.
- Dados de cartão (`CartaoCreditoRequestDto`) não viram entidade nem são persistidos; passam da borda ao adaptador ou usam tokenização do Asaas — decidir com o usuário.
- Testes: services mockam a porta; adaptadores com testes de contrato via **WireMock** (adicionar dependência); testes de caracterização antes de mexer no `PedidoServiceImpl`.

Ordem (cada etapa com suíte verde e commit próprio):
1. Pagamento: porta + records → adaptador Asaas (WireMock) → migrar `ClienteServiceImpl` → migrar `PedidoServiceImpl` → remover `CobrancaService` e mover DTOs Asaas.
2. Webhook de cobrança → `EventoPagamento`.
3. Frete: porta, adaptador Melhor Envio, token dentro do adaptador, migrar `PedidoServiceImpl`/`DevolucaoServiceImpl`.
4. Webhook de rastreio.
5. ArchUnit: `application` não importa `infrastructure` nem `interfaces.dto`.

## Pendências e decisões em aberto

- **Senhas/segredos (usuário vai tratar):** o segredo JWT antigo e o conteúdo de `novo 6.txt` continuam no histórico do git — rotacionar. `V23__insert_admin.SQL` cria o admin com senha conhecida (está no comentário da migration) — trocar em produção.
- Mensagem de `pagarPedido` sem espaço: "AGUARDANDO_PAGAMENTOpara".
- **Preço no pedido:** `ItemProdutoCarrinho.paraItemPedido()` preserva o comportamento antigo — o pedido usa o **preço atual do produto**, não o preço gravado no carrinho (`precoUnitario`). Confirmar com o usuário se é o desejado (teste `deveUsarPrecoAtualDoProdutoAoConverterParaPedido` documenta isso).
- **`ME_SERVICE_DEVOLUCAO`** precisa estar definida em produção.
- `README.md` raiz ainda descreve a fase FIAP (Azure/Oracle/PagBank).
- Plano de integrações e estorno concluído. Pendências registradas: as da etapa 6 (acima); rotina de expiração de pedido de cartão sem resposta do Asaas; Jackson em `Usuario`; fuso da previsão de entrega; `IllegalStateException` sem `ME_SERVICE_DEVOLUCAO`; 404 na falha do token do Melhor Envio.

## Ambiente
- Build: Java 21 + Maven Wrapper em `Java/rei-dos-piratas` (`.\mvnw.cmd test` no Windows). Nesta máquina o `JAVA_HOME` padrão é Java 7 (sdkman) e o PowerShell bloqueia o `mvnw.cmd` (modo de linguagem restrito): rodar pelo Git Bash com `JAVA_HOME` no JDK 21 do Adoptium e `-s` com um settings sem o mirror Nexus corporativo.
- Arquivos `.java` usam CRLF; preservar ao editar.
