# Hospedagem Para TCC

## Melhor opcao gratis

Se a prioridade for gastar zero com a menor quantidade de integracoes, a melhor combinacao para este projeto e:

- backend no Render Free
- banco PostgreSQL no Supabase

Essa combinacao faz sentido porque:

- o Render ainda oferece web service gratis para subir o backend
- voce ja usa Supabase no banco hoje
- o GitHub Actions cobre o CI sem custo para um projeto pequeno

## Contas que voce vai precisar criar

### Obrigatorias

- GitHub: para hospedar o repositorio e conectar o deploy automatico
- Render: para hospedar o backend
- Supabase: para hospedar o PostgreSQL

### Opcional

- dominio proprio: so se quiser URL personalizada para o backend

## Fluxo sugerido

- CI: GitHub Actions executando `./mvnw clean verify`
- CD: autodeploy do Render na branch `main`

Fluxo final:

1. voce faz push ou abre PR
2. o GitHub Actions valida build e testes
3. o Render publica automaticamente a branch principal

## Onde cada parte fica

### 1. Backend

Hospede o backend Spring Boot no Render usando o `Dockerfile` deste repositorio.

Use no Render:

- tipo `Web Service`
- branch `main`
- health check em `GET /health`

Conta:

- criar em `render.com`
- conectar a conta do GitHub durante a criacao do servico

Configuracao:

- Instance Type: `Free`
- Root Directory: deixe vazio
- Runtime: usar `Docker`
- Auto-Deploy: habilitado
- Health Check Path: `/health`

### 2. Banco

Use o projeto do Supabase para o banco PostgreSQL.

Conta:

- usar sua conta em `supabase.com`
- criar um projeto novo se quiser separar do ambiente atual, ou reaproveitar o projeto existente

No Supabase para o banco:

1. abra o projeto
2. clique em `Connect`
3. copie a string de conexao recomendada para backend persistente

Para Java/Spring, configure `DB_URL` com uma URL JDBC do tipo:

`jdbc:postgresql://HOST:5432/DB?sslmode=require`

E preencha tambem:

- `DB_USERNAME`
- `DB_PASSWORD`

## Variaveis para cadastrar no Render

Cadastre estas variaveis no servico do backend:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `AI_API_BASE_URL`
- `AI_API_PREDICT_PATH`
- `APP_CORS_ALLOWED_ORIGINS`

Exemplo de `APP_CORS_ALLOWED_ORIGINS`:

- frontend local: `http://localhost:5173`
- frontend publicado: `https://seu-frontend.onrender.com`

Se precisar aceitar os dois:

- `http://localhost:5173,https://seu-frontend.onrender.com`

## Limitacoes importantes do plano gratis

### Render Free

- a aplicacao entra em idle apos 15 minutos sem trafego
- quando volta, pode levar cerca de 1 minuto para responder
- isso e aceitavel para TCC e demonstracao, mas nao para uso real constante

### Supabase Free

- o banco e gratuito, mas pausa apos inatividade prolongada no plano free

### Servico de IA

- se a IA tambem precisar ficar online 24/7, talvez nao caiba confortavelmente em um stack 100% gratis
- para TCC, uma estrategia valida e deixar a IA em outro ambiente, rodar localmente na apresentacao ou ativar apenas para demos

## Alternativa mais simples, mas nao totalmente gratis

Se voces aceitarem um custo muito baixo para reduzir integracoes, o Railway continua sendo a opcao mais simples para juntar backend e PostgreSQL no mesmo lugar.

Eu manteria a recomendacao assim:

- `gratis e simples`: Render + Supabase
- `mais simples`: Railway
