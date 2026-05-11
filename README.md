# Horus Backend

Backend Spring Boot do projeto Horus.

## Tecnologias

- Java 21
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
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`
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

## Testes

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

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
