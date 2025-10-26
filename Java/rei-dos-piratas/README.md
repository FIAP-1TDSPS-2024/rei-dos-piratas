# Rei Dos Piratas

> API REST para Back-End da aplicação Rei dos Piratas. Um E-commerce de mangás, focando na compra de mangás antigos de pessoas que vendem suas coleções e vendê-los para colecionadores que não querem ter o estresse de pesquisar, negociar e verificar estado de conservação. Também serão vendidos mangás lançamentos a preço competitivo para todo o Brasil.

---

## Vídeo explicativo da solução

> Acesse o Link abaixo para assistir ao vídeo explicativo da solução.

[Vídeo Explicativo no YouTube](https://www.youtube.com/watch?v=wSOUlz6PsAY)

## 👥 Integrantes do Grupo – CATECH

* **RM561144**: Jonas Oliveira - Responsável por Java e banco de dados
* **RM559336**: Wendell Dourado - Responsável por Mobile e Devops
* **RM559622**: Daniel Batista - Responsável por .NET, IoT e QA

---

## Diagrama da Aplicação

> Acesse o Link abaixo para acessar o diagrama UML da aplicação.

[Diagrama da Aplicação no Lucidchart](https://lucid.app/lucidchart/8cb61c77-643b-4269-a160-0be5cca49936/edit?viewport_loc=-2310%2C-669%2C…)

---

## Cronograma de Desenvolvimento - Java - Sprint 1

| Semana | Datas           | Atividade                                         |
| ------ |-----------------| ------------------------------------------------- |
| 1      | 22/set - 28/set | Definir diagrama UML e arquitetura                |
| 2      | 29/set - 05/out | Repositories, entidades JPA e mappers JPA         |
| 2      | 29/set - 05/out | Implementação de Usuario, Cliente e Vendedor      |
| 3      | 06/out - 08/out | Services e validations                            |
| 3      | 06/out - 08/out | Controllers e DTOs                                |
| 3      | 08/out - 10/out | RestControllers                                   |
| 3      | 08/out - 10/out | Conexão com DB                                    |
| 3      | 09/out - 12/out | Testes de requisição, criação de testes unitários |
| 3      | 09/out - 12/out | Finalização de documentação                       |
| 3      | 12/out          | Entrega final - Sprint 1                          |

---

## Como Executar a Aplicação

1. Clone o repositório:

```bash
git clone https://github.com/FIAP-1TDSPS-2024/rei-dos-piratas.git
```

2. Acesse o diretório do projeto:

```bash
cd rei-dos-piratas/Java/rei-dos-piratas
```

3. Compile a aplicação:

```bash
mvn clean install
```

4. Execute a aplicação:

```bash
mvn spring-boot:run
```

## Endpoints Disponíveis

| Método | Endpoint         | Descrição                    | 
| ------ |------------------| ---------------------------- |
| GET    | /clientes        | Retorna lista de clientes    |
| GET    | /clientes/{id}   | Retorna cliente por ID       |
| POST   | /clientes        | Cria um novo clientes        |
| PUT    | /clientes        | Atualiza clientes existente  |
| DELETE | /clientes/{id}   | Remove clientes existente    |
| GET    | /vendedores      | Retorna lista de vendedores  |
| GET    | /vendedores/{id} | Retorna funcionario por ID      |
| POST   | /vendedores      | Cria um novo funcionario        |
| PUT    | /vendedores      | Atualiza funcionario existente  |
| DELETE | /vendedores/{id} | Remove funcionario existente    |
---

## Coleção POSTMAN

[Link do arquivo JSON da coleção POSTMAN para teste dos END-POINTS](API Rei dos Piratas.postman_collection.json)

## Tecnologias Utilizadas

* Java 17 (ou versão utilizada)
* Spring Boot 3.5.6
* Maven
* Banco de dados: H2, ORACLE

## Bibliotecas e dependências (conforme pom.xml)

Dependências principais:
* org.springframework.boot:spring-boot-starter-data-jpa — JPA + Spring Data (persistência)
* org.springframework.boot:spring-boot-starter-web — Spring MVC / REST
* org.springframework.boot:spring-boot-starter-validation — Bean Validation (JSR-380)
* com.oracle.database.jdbc:ojdbc11 (scope: runtime) — Driver JDBC Oracle
* org.projectlombok:lombok (optional) — Reduz boilerplate com anotações

Ferramentas de desenvolvimento / runtime:
* org.springframework.boot:spring-boot-devtools (scope: runtime, optional) — hot reload / dev tools

Dependências de teste:
* org.springframework.boot:spring-boot-starter-test (scope: test) — JUnit, Spring Test, MockMVC, AssertJ, etc.
* com.h2database:h2 (scope: test) — banco em memória para testes

Plugins relevantes configurados no build:
* maven-compiler-plugin — processamento de anotações (Lombok)
* jacoco-maven-plugin — cobertura de testes
* spring-boot-maven-plugin — empacotamento/execução da aplicação

> Observação: versões seguem as definidas pelo Spring Boot parent (3.5.6). O driver Oracle (ojdbc11) é referenciado sem versão explícita no pom (é resolvido em tempo de execução ou via repositório configurado).
...

## Observações

> Espaço para observações adicionais, informações de configuração especial, dicas de uso ou requisitos do sistema.