# 🏴‍☠️ Rei dos Piratas – DevOps

![Azure](https://img.shields.io/badge/Azure-Cloud-blue)
![Docker](https://img.shields.io/badge/Docker-Containers-blue)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Orchestration-blue)

---

## 📌 Sobre este Diretório

Este diretório contém toda a documentação e recursos relacionados à **infraestrutura e deploy** do projeto **Rei dos Piratas** na nuvem Azure.

O projeto utiliza **Azure Virtual Machines** com **Docker** e **Docker Compose** para containerizar e executar a aplicação Java Spring Boot.

---

## 🗂️ Estrutura do Diretório

```
DevOps/
├── README.md                      # Este arquivo
├── commands_prepare_vm.md         # Comandos para criar e configurar a VM no Azure
├── commands_docker.md             # Comandos para instalação do Docker e deploy da aplicação
└── evidencias/                    # Screenshots do processo de deploy
    ├── 001_criando_grupo_de_recurso.png
    ├── 002_criando_vm.png
    ├── 003_vm_rei_dos_piratas.png
    ├── 004_portas_abertas.png
    ├── 005_criando_alerta.png
    ├── 006_deletando_grupo_de_recurso.png
    └── 007_evidencia_delecao.png
```

---

## 🐳 Docker e Containerização

### Localização dos Arquivos

Os arquivos de containerização estão localizados no diretório da aplicação Java:

- **Dockerfile**: `Java/rei-dos-piratas/Dockerfile`
- **Docker Compose**: `Java/rei-dos-piratas/compose.yaml`

### Dockerfile

O Dockerfile utiliza uma **estratégia multi-stage** para otimizar o tamanho da imagem final:

1. **Build Stage**: Utiliza `eclipse-temurin:17-jdk` para compilar a aplicação

   - Copia o Maven wrapper e dependências
   - Compila o projeto com `./mvnw package -DskipTests`

2. **Runtime Stage**: Utiliza `eclipse-temurin:17-jre` (mais leve)
   - Cria um usuário não-root para segurança
   - Copia apenas o JAR compilado
   - Configura flags JVM otimizadas para containers
   - Expõe a porta 8080

### Docker Compose

O arquivo `compose.yaml` orquestra o container da aplicação:

```yaml
services:
  java-app:
    build:
      context: .
    env_file:
      - .env
    container_name: java-app
    restart: unless-stopped
    init: true
    ports:
      - "8080:8080"
```

**Características**:

- Build automático a partir do Dockerfile local
- Carregamento de variáveis de ambiente via arquivo `.env`
- Restart automático em caso de falhas
- Mapeamento da porta 8080 do container para o host

---

## ☁️ Deploy no Azure

### Arquitetura

A aplicação é executada em uma **Azure Virtual Machine** com as seguintes especificações:

- **Sistema Operacional**: Ubuntu 24.04 LTS
- **Tamanho**: Standard_B2s (2 vCPUs, 4GB RAM)
- **Localização**: Canada Central
- **Grupo de Recursos**: ReiDosPiratas
- **Nome da VM**: VMReiDosPiratas

### Portas Abertas

- **Porta 80**: HTTP (prioridade 1001)
- **Porta 8080**: Aplicação Java Spring Boot (prioridade 1002)

### Monitoramento

Um alerta foi configurado para monitorar o uso de CPU:

- **Nome**: Alert-CPU-High
- **Condição**: CPU acima de 90% (média)
- **Descrição**: Notifica quando a CPU permanece acima de 90% por 5 minutos

---

## 📝 Documentação de Comandos

### `commands_prepare_vm.md`

Este arquivo contém os **comandos Azure CLI** utilizados para provisionar a infraestrutura:

1. **Criação do Grupo de Recursos**: Cria o grupo `ReiDosPiratas` na região Canada Central
2. **Criação da VM**: Provisiona a máquina virtual Ubuntu com autenticação por senha
3. **Abertura de Portas**: Libera as portas 80 e 8080 no Network Security Group
4. **Configuração de Alertas**: Cria alerta de monitoramento de CPU
5. **Exclusão de Recursos**: Comando para deletar todo o grupo de recursos

### `commands_docker.md`

Este arquivo documenta o **processo de deploy da aplicação**:

1. **Instalação do Docker**: Comandos para instalar Docker e Docker Compose na VM
2. **Configuração de Permissões**: Adiciona o usuário ao grupo docker
3. **Clone do Repositório**: Baixa o código-fonte do GitHub
4. **Configuração de Ambiente**: Cria o arquivo `.env` com credenciais do banco de dados
5. **Execução**: Inicia os containers com `docker-compose up -d`
6. **Verificação**: Comandos para verificar logs e testar a API

---

## 🚀 Como Fazer o Deploy

### Pré-requisitos

- Azure CLI instalado e configurado
- Conta Azure ativa
- Acesso SSH à VM

### Passo a Passo

1. **Preparar a infraestrutura Azure**:

   ```bash
   # Siga os comandos em commands_prepare_vm.md
   az group create --name "ReiDosPiratas" --location canadacentral
   az vm create [...]
   ```

2. **Conectar à VM via SSH**:

   ```bash
   ssh admlnx@<IP_PUBLICO_DA_VM>
   ```

3. **Configurar Docker e Deploy**:

   ```bash
   # Siga os comandos em commands_docker.md
   sudo apt-get update -y
   sudo apt-get install docker.io docker-compose -y
   [...]
   ```

4. **Verificar a aplicação**:
   ```bash
   curl http://localhost:8080/health
   # ou
   curl http://<IP_PUBLICO>:8080/health
   ```

---

## 📸 Evidências

A pasta `evidencias/` contém screenshots de todas as etapas do processo de deploy, desde a criação do grupo de recursos até a exclusão dos recursos após os testes.

---

## 🔐 Segurança

- Container executa com **usuário não-root**
- Variáveis sensíveis (DB_USER, DB_PASSWORD) são carregadas via arquivo `.env`
- Network Security Group configurado para permitir apenas portas específicas
- Alertas de monitoramento para detecção de anomalias

---

## 🛠️ Manutenção

### Ver logs da aplicação:

```bash
docker logs java-app -f
```

### Reiniciar a aplicação:

```bash
docker-compose restart
```

### Atualizar a aplicação:

```bash
git pull
docker-compose down
docker-compose up -d --build
```

---

## 📚 Referências

- [Azure Virtual Machines](https://learn.microsoft.com/pt-br/azure/virtual-machines/)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Azure CLI](https://learn.microsoft.com/pt-br/cli/azure/)

---

## 👥 Equipe Responsável

- **Wendell Dourado (RM559336)** - Responsável por Mobile e DevOps
- **Jonas Oliveira (RM561144)** - Responsável por Java e Banco de Dados
- **Daniel Batista (RM559622)** - Responsável por .NET, IoT e QA
