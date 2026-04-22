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
- `AI_SERVICE_URL`
- `SERVER_PORT`

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
