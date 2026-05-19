# Horus Backend

Backend Spring Boot do projeto Horus.

## Tecnologias

- Java 17
- Spring Boot
- Maven Wrapper
- PostgreSQL
- MinIO

## Estrutura

```text
src/main/java/br/com/horus/horus_backend
├── config
├── controller
├── dto
├── model
├── repository
└── service
```

## Configuracao local

As configuracoes sensiveis sao lidas por variaveis de ambiente. Use o arquivo `.env.example` como referencia para criar seu ambiente local.

Variaveis principais:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `STORAGE_ENDPOINT`
- `STORAGE_ACCESS_KEY`
- `STORAGE_SECRET_KEY`
- `STORAGE_BUCKET`
- `STORAGE_PUBLIC_BASE_URL`
- `AI_API_BASE_URL`
- `AI_API_PREDICT_PATH`
- `SERVER_PORT`
- `APP_CORS_ALLOWED_ORIGINS`

## Executar

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### Executar com banco local H2

Na branch `local-h2-dev`, o perfil `local` e usado por padrao quando nenhum perfil for informado. Entao voce pode subir o backend sem PostgreSQL/Supabase com:

```powershell
.\mvnw.cmd spring-boot:run
```

Se quiser informar o perfil explicitamente:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

Esse perfil usa H2 em arquivo e grava os dados em `data/horus-local`, que fica fora do Git. O console do H2 fica disponivel em:

```text
http://localhost:8080/h2-console
```

Use estes dados no console:

```text
JDBC URL: jdbc:h2:file:./data/horus-local;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE
User Name: sa
Password: deixe em branco
```

Se a porta `8080` ja estiver em uso, rode em outra porta:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

## Testes

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

## CI/CD

O projeto agora possui um workflow de CI em [`.github/workflows/ci.yml`](.github/workflows/ci.yml) que:

- roda em `push` para `main` e `develop`
- roda em `pull_request`
- configura Java 21
- executa `./mvnw clean verify`

Para CD simples, a recomendacao e usar deploy automatico da plataforma de hospedagem apos o CI passar, em vez de criar uma pipeline de release mais complexa.

## Hospedagem recomendada

Se a prioridade for manter o projeto gratis, a melhor rota hoje e:

- Render Free para o backend
- Supabase para o PostgreSQL
- Supabase Storage para os arquivos

Se a prioridade for simplificar a infraestrutura e voces puderem aceitar um custo baixo, o Railway continua sendo a alternativa mais simples.

O projeto ficou pronto para PaaS com:

- suporte a `PORT` em producao
- endpoint `GET /health` para health checks
- `Dockerfile` para deploy consistente
- suporte a URL publica estavel para storage compativel com S3 via `STORAGE_PUBLIC_BASE_URL`

Um guia objetivo de deploy esta em [docs/deploy-tcc.md](docs/deploy-tcc.md).

## Endpoints

- `POST /auth/register`
- `POST /auth/login`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /usuarios/{id}`
- `PUT /usuarios/{id}`
- `DELETE /usuarios/{id}`
- `POST /pacientes`
- `GET /pacientes/usuario/{usuarioId}`
- `GET /pacientes/{id}`
- `PUT /pacientes/{id}`
- `DELETE /pacientes/{id}`
- `POST /exames`
- `GET /exames/paciente/{pacienteId}`
- `GET /exames/{id}`
- `DELETE /exames/{id}`

## Integracao com frontend local

Para a tela de login em React, o backend agora aceita chamadas tanto para `/auth/login` quanto para `/api/auth/login`.

Exemplo de requisicao:

```json
{
  "email": "usuario@exemplo.com",
  "senha": "123456"
}
```

Tambem aceita o campo `password` no lugar de `senha`, caso seu frontend esteja nesse formato.

Resposta de sucesso:

```json
{
  "id": 1,
  "nome": "Usuario",
  "email": "usuario@exemplo.com",
  "token": "token-provisorio"
}
```

Resposta de erro:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Email ou senha inválidos",
  "timestamp": "2026-05-10T19:00:00",
  "details": []
}
```
