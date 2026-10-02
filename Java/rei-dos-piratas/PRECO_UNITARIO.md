O preço unitário é copiado do produto já carregado pelo servidor na criação do
item de pedido. O Service de pedido não consulta os produtos novamente.
O checkout converte os itens do carrinho usando o preço do produto carregado,
sem reutilizar o preço registrado anteriormente no carrinho.

Os DTOs de saída expõem `precoUnitario`. Os DTOs de entrada continuam recebendo
somente produto/item e quantidade, pois o cliente não define o preço cobrado.
Totais, subtotais e valores declarados no frete usam o preço registrado no item.
Itens devolvidos copiam o preço do item comprado. A regra existente de somar o
frete ao valor total da devolução foi mantida.

As validações de preço obrigatório, não negativo e com até duas casas decimais
ficam no domínio. As coleções dos agregados possuem validação em cascata.
O banco garante integridade com NOT NULL e CHECK, sem tratamentos de preços
ausentes nos serviços, DTOs ou tela.

Aplicar a migração Flyway V31 antes de executar a aplicação atualizada. A migração
considera o banco vazio, conforme definido para este projeto. O Flyway é
desabilitado por padrão; habilitá-lo com FLYWAY_ENABLED=true no processo que
executa as migrações.
