# Autenticação - primeira etapa

Cliente e Funcionário continuam em tabelas separadas. Conta, TipoConta e
IdentidadeConta permanecem no domínio e não criam novas tabelas. Conta reúne
o Usuario encontrado e a identidade composta por tipo e ID.

Usuario não implementa UserDetails nem cria GrantedAuthority. A regra de conta
ativa e a normalização de e-mail ficam em Usuario. Construtores e setEmail
removem espaços nas extremidades e convertem para minúsculas.
SpringUsuarioDetailsService converte o domínio para Spring Security.

AutenticacaoServiceImpl orquestra login e cadastro. SenhaService,
TokenAcessoService e UsuarioAtualService são interfaces em application.service.
ContaRepository fica em domain.repository, seguindo os repositórios existentes.
BCryptSenhaService, JwtUtil, SpringUsuarioAtual e ContaRepositoryImpl são as
implementações na infraestrutura. Não há pacotes application.model ou
application.port. Outros serviços que acessam SecurityContextHolder não foram
refatorados nesta etapa.

Login e cadastro mantêm o formato de AuthResponse. O login valida a senha e a
regra de conta ativa; falhas retornam 401. O cadastro emite um token para a conta
criada na mesma transação, sem autenticar e consultar novamente.
Não há confirmação de e-mail, recuperação de senha ou envio de mensagens.

O JWT contém tipo, uid e subject no formato TIPO:ID. Cada emissão possui jti
aleatório, evitando tokens iguais para dois logins próximos. O filtro carrega a
conta por tipo e ID, usa suas permissões atuais e rejeita contas inativas. Tokens
do formato anterior são rejeitados: usuários devem fazer login novamente.

Cadastro e alteração de e-mail verificam CLIENTES e FUNCIONARIOS, inclusive
contas inativas, excluindo somente a própria identidade na alteração. Não há
tabela auxiliar nem bloqueio de concorrência. As restrições UNIQUE existentes
continuam individuais por tabela. A consulta atende à escala atual do projeto;
não garante unicidade entre tabelas em duas escritas simultâneas.
Ativar ou desativar funcionário não repete a validação de e-mail.

Esta etapa não acrescenta migrações. A V32 de bloqueio de e-mail foi removida,
considerando o banco vazio combinado para o projeto. A sequência permanece até
V31; Flyway pode ser habilitado para migração com FLYWAY_ENABLED=true.

Somente /auth/login e /auth/cadastro são públicos dentro de /auth;
/auth/logout exige autenticação. O logout ainda usa a blocklist existente.
Refresh, sessões persistidas e ajustes de cookies/CSRF pertencem às etapas
seguintes. A validade do access token mantém a configuração atual até a
implantação do refresh, quando será ajustada para 15 minutos e a sessão terá
limite absoluto de sete dias.

Os testes cobrem autenticação real, conta inativa, logout, formato antigo e
expiração do JWT, normalização e duplicidade de e-mail entre tabelas, alteração
do próprio e-mail e cadastro sem autenticação redundante. O teste específico
do mecanismo removido de bloqueio de concorrência também foi retirado.
