# Comandos Docker - Rei dos Piratas

## Instalando o Docker

```bash
sudo apt-get update -y

sudo apt-get install docker.io -y
sudo systemctl start docker
sudo systemctl enable docker

sudo usermod -aG docker ${USER}
```

## Fechando conexão para mudança ter efeito

```bash
exit
```

## Verifique se o Docker está funcionando sem sudo

```bash
docker ps
```

## Instala o Docker Compose

```bash
sudo apt-get install docker-compose -y
```

## Verifique a instalação

```bash
docker-compose --version
```

## Clonando o repositório

```bash
git clone https://github.com/FIAP-1TDSPS-2024/rei-dos-piratas.git
```

## Acessando api java

```bash
cd rei-dos-piratas/Java/rei-dos-piratas
```

## Definindo variáveis de ambiente

```bash
echo -e "DB_USER=user\nDB_PASSWORD=pass" > .env
```

## Executando docker

```bash
docker-compose up -d
```

## Verificando se o container foi criado

```bash
docker ps
```

## Verificando os logs do container

```bash
docker logs <container_id>
```

## Acessando API localmente no container

```bash
curl http://localhost:8080/health && echo
```

## Acessando api via IP

```bash
curl http://ippublico:8080/health
```
