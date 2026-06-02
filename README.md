# Horus Backend

[![Backend CI](https://github.com/imatheusmartins/horus-backend/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/imatheusmartins/horus-backend/actions/workflows/ci.yml)

API backend do Horus, sistema para gerenciamento de usuarios, pacientes e exames, com integracao a um servico externo de IA para analise de imagens.

## Tecnologias

- Java 21
- Spring Boot
- Maven Wrapper
- Spring Data JPA
- PostgreSQL
- H2 para testes
- Supabase Storage
- Docker
- GitHub Actions

## Estrutura

```text
src/main/java/br/com/horus/horus_backend
|-- config
|-- controller
|-- dto
|-- model
|-- repository
`-- service
```

## Configuracao local

As configuracoes sensiveis sao lidas por variaveis de ambiente. Use o arquivo `.env.example` como referencia para criar o arquivo `.env` local.

Variaveis principais:

| Variavel | Descricao |
| --- | --- |
| `DB_URL` | URL JDBC do banco PostgreSQL |
| `DB_USERNAME` | Usuario do banco |
| `DB_PASSWORD` | Senha do banco |
| `AI_API_BASE_URL` | URL base do servico de IA |
| `AI_API_PREDICT_PATH` | Caminho do endpoint de predicao |
| `APP_STORAGE_PROVIDER` | Provedor de armazenamento (`local` ou `supabase`) |
| `SUPABASE_URL` | URL do projeto Supabase |
| `SUPABASE_SERVICE_ROLE_KEY` | Chave de acesso ao Supabase Storage |
| `APP_CORS_ALLOWED_ORIGINS` | Origens permitidas para chamadas do frontend |
| `SERVER_PORT` | Porta local da aplicacao |

## Executar localmente

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrao, a API sobe em:

```text
http://localhost:8080
```

Health check:

```text
GET /health
```

Resposta esperada:

```json
{
  "status": "ok"
}
```

## Testes

O projeto possui testes automatizados para validar a subida do contexto Spring e fluxos de controller. Durante os testes, o profile `test` usa H2 em memoria, evitando dependencia do PostgreSQL de producao ou desenvolvimento.

No Linux/macOS:

```bash
SPRING_PROFILES_ACTIVE=test ./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd "-Dspring.profiles.active=test" test
```

## CI/CD

O workflow de CI fica em `.github/workflows/ci.yml` e roda automaticamente em:

- `push` para `dev` e `main`
- `pull_request` para `dev` e `main`
- execucao manual pela aba Actions do GitHub

Etapas executadas:

- checkout do repositorio
- configuracao do Java 21
- cache de dependencias Maven
- execucao dos testes com profile `test`
- empacotamento da aplicacao
- publicacao do `.jar` como artefato em `push` para `main`

Fluxo recomendado:

1. Desenvolver e enviar alteracoes para `dev`.
2. Validar o resultado do GitHub Actions.
3. Abrir pull request de `dev` para `main`.
4. Fazer merge na `main` apos o CI passar.
5. Deixar a plataforma de hospedagem publicar a nova versao automaticamente.

## Deploy

O backend pode ser publicado como aplicacao Docker usando o `Dockerfile` do repositorio. O deploy recomendado para o prototipo e manter o autodeploy da plataforma de hospedagem apontando para a branch `main`.

O projeto ja considera:

- porta dinamica via `PORT` ou `SERVER_PORT`
- endpoint `/health` para health check
- variaveis de ambiente para banco, IA, CORS e armazenamento
- build Docker multi-stage

Um guia de deploy esta disponivel em `docs/deploy-tcc.md`.

## Endpoints

### Autenticacao

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `POST` | `/auth/register` | Cadastro de usuario |
| `POST` | `/auth/login` | Login de usuario |
| `POST` | `/api/auth/register` | Cadastro de usuario com prefixo `/api` |
| `POST` | `/api/auth/login` | Login de usuario com prefixo `/api` |

### Usuarios

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `GET` | `/usuarios/{id}` | Busca usuario por ID |
| `PUT` | `/usuarios/{id}` | Atualiza usuario |
| `DELETE` | `/usuarios/{id}` | Remove usuario |

### Pacientes

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `POST` | `/pacientes` | Cria paciente |
| `GET` | `/pacientes/usuario/{usuarioId}` | Lista pacientes de um usuario |
| `GET` | `/pacientes/{id}` | Busca paciente por ID |
| `PUT` | `/pacientes/{id}` | Atualiza paciente |
| `DELETE` | `/pacientes/{id}` | Remove paciente |

### Exames

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `POST` | `/exames` | Cria exame com imagem para analise |
| `GET` | `/exames/paciente/{pacienteId}` | Lista exames de um paciente |
| `GET` | `/exames/{id}` | Busca exame por ID |
| `DELETE` | `/exames/{id}` | Remove exame |

O endpoint `POST /exames` recebe uma requisicao `multipart/form-data` com:

- `dados`: objeto JSON com os dados do exame
- `imagem`: arquivo de imagem do exame

## Integracao com frontend

O backend aceita chamadas de autenticacao tanto com o prefixo `/auth` quanto com `/api/auth`, facilitando a integracao com frontends que usam prefixo de API.

Exemplo de login:

```json
{
  "email": "usuario@exemplo.com",
  "senha": "123456"
}
```

Tambem e aceito o campo `password` no lugar de `senha`.

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
  "message": "Email ou senha invalidos",
  "timestamp": "2026-05-10T19:00:00",
  "details": []
}
```
