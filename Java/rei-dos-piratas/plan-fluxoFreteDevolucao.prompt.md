## Plan: Fluxo de Frete de Devolução

Evoluir o domínio de devoluções para acionar frete reverso ponta a ponta: criar pedido de frete de devolução na aprovação, gerar etiqueta e expor acesso ao cliente sem autenticação via link anônimo com expiração e fallback de entrega da etiqueta (redirect para URL da transportadora e stream/PDF local). O plano minimiza regressão separando contrato, persistência, segurança, telemetria, testes e rollout gradual por feature flag.

### Steps
- [ ] Ajustar contratos em [`FreteService`](src/main/java/br/com/fiap/rei_dos_piratas/application/service/FreteService.java), [`FreteAppClient`](src/main/java/br/com/fiap/rei_dos_piratas/infrastructure/external_interface/feign/FreteAppClient.java) e DTOs de etiqueta para suportar devolução (`reverse`) e retorno binário/URL.
- [ ] Estender modelo de devolução em [`Devolucao`](src/main/java/br/com/fiap/rei_dos_piratas/domain/entity/Devolucao.java), [`JpaDevolucaoEntity`](src/main/java/br/com/fiap/rei_dos_piratas/infrastructure/entity/negocio/JpaDevolucaoEntity.java) e migrations `V27+` para persistir `pedidoFreteRetorno`, `labelUrl`, `anonTokenHash`, `expiresAt` e status da etiqueta.
- [ ] Implementar orquestração no [`DevolucaoServiceImpl`](src/main/java/br/com/fiap/rei_dos_piratas/application/service/impl/DevolucaoServiceImpl.java): aprovar devolução, criar pedido de frete reverso, gerar etiqueta, tratar idempotência/reprocesso e atualizar estado interno.
- [ ] Expor disponibilização pública em [`DevolucaoRestController`](src/main/java/br/com/fiap/rei_dos_piratas/infrastructure/api_rest/DevolucaoRestController.java): endpoint autenticado para gerar link anônimo e endpoint público para consumir etiqueta com fallback `redirect -> PDF stream`.
- [ ] Endurecer segurança em [`SecurityConfig`](src/main/java/br/com/fiap/rei_dos_piratas/infrastructure/config/security/SecurityConfig.java): permitir apenas rota pública específica, validar token assinado+expiração+uso único/rate limit e reforçar autorização por dono do pedido.
- [ ] Cobrir observabilidade, testes e rollout: logs estruturados/correlação em [`FreteServiceImpl`](src/main/java/br/com/fiap/rei_dos_piratas/application/service/impl/FreteServiceImpl.java), testes unitários/web em `src/test/java/**`, e ativação progressiva por flag em [`application.properties`](src/main/resources/application.properties).

### Further Considerations
1. Token anônimo: usar JWT assinado curto (A) vs token randômico hash em banco (B) vs híbrido (C, recomendado).
2. Fallback de etiqueta: priorizar URL externa (A) e usar stream interno apenas em falha/expiração de URL (B).
3. Rollout: habilitar primeiro para devoluções aprovadas novas (A) ou incluir backfill de devoluções antigas (B); qual prioridade?  

Rascunho para revisão: se quiser, eu refino este plano já com milestones por sprint (MVP + hardening).

