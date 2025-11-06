# Comandos Azure VM - Rei dos Piratas

## Criando o grupo de recurso

```bash
az group create --name "ReiDosPiratas" --location canadacentral
```

## Criando a máquina virtual

```bash
az vm create \
--resource-group "ReiDosPiratas" \
--name "VMReiDosPiratas" \
--image "Ubuntu2404" \
--size Standard_B2s \
--authentication-type password \
--admin-username admlnx \
--admin-password Fiap@2tdspsvm \
--vnet-name nnet-Linux \
--nsg nsgr-linux \
--public-ip-address pip-linux \
--tags "owner=CaTech"
```

## Abrindo portas 80 e 8080

```bash
az vm open-port --resource-group "ReiDosPiratas" --name VMReiDosPiratas --port 80 --priority 1001
az vm open-port --resource-group "ReiDosPiratas" --name VMReiDosPiratas --port 8080 --priority 1002
```

## Alerta para monitoramento de CPU

```bash
az monitor metrics alert create \
-n Alert-CPU-High \
-g ReiDosPiratas \
--scopes $(az vm show -g ReiDosPiratas -n VMReiDosPiratas --query id -o tsv) \
--description "CPU acima de 90% por 5min" \
--condition "avg Percentage CPU > 90"
```

## Deletando grupo de recursos

```bash
az group ativarDesativar --name ReiDosPiratas --yes --no-wait
```
