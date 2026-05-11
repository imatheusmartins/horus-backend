# Hospedagem Para TCC

## Melhor opcao gratis

Se a prioridade for gastar zero com a menor quantidade de integracoes, a melhor combinacao para este projeto e:

- backend no Render Free
- banco PostgreSQL no Supabase
- arquivos no Supabase Storage

Essa combinacao faz sentido porque:

- o Render ainda oferece web service gratis para subir o backend
- voce ja usa Supabase no banco hoje
- o Supabase tambem oferece Storage e reduz uma integracao externa
- o GitHub Actions cobre o CI sem custo para um projeto pequeno

## Contas que voce vai precisar criar

### Obrigatorias

- GitHub: para hospedar o repositorio e conectar o deploy automatico
- Render: para hospedar o backend
- Supabase: para hospedar o PostgreSQL e o Storage

### Opcional

- dominio proprio: so se quiser URL personalizada para o bucket ou para o backend

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

### 2. Banco e arquivos

Use o proprio projeto do Supabase para banco e storage.

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

No Supabase para o storage:

1. abra `Storage`
2. crie um bucket
3. marque o bucket como `Public`
4. se quiser, configure restricao de tipo para `image/*`
5. configure o limite de tamanho de arquivo que fizer sentido para o TCC

Conta:

- a mesma conta do Supabase
- abrir `Project Settings` e procurar a configuracao `S3`

Na configuracao S3 do Supabase:

1. gerar `Access Key ID` e `Secret Access Key`
2. copiar o endpoint S3 do projeto
3. anotar o `project ref`

Como o projeto agora aceita nomes genericos de storage, configure:

- `STORAGE_ENDPOINT`
- `STORAGE_ACCESS_KEY`
- `STORAGE_SECRET_KEY`
- `STORAGE_BUCKET`
- `STORAGE_PUBLIC_BASE_URL`

Valores esperados:

- `STORAGE_ENDPOINT`: `https://<project_ref>.storage.supabase.co/storage/v1/s3`
- `STORAGE_ACCESS_KEY`: access key S3 do Supabase Storage
- `STORAGE_SECRET_KEY`: secret key S3 do Supabase Storage
- `STORAGE_BUCKET`: nome do bucket criado
- `STORAGE_PUBLIC_BASE_URL`: `https://<project_ref>.supabase.co/storage/v1/object/public/<bucket>`

Observacao importante:

- eu adaptei o codigo para, quando `STORAGE_PUBLIC_BASE_URL` estiver preenchida, salvar uma URL publica estavel do arquivo
- isso deixa o uso com bucket publico do Supabase mais simples do que depender de URL assinada salva no banco

## Variaveis para cadastrar no Render

Cadastre estas variaveis no servico do backend:

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
- o storage free tem 1 GB incluido e limite de upload menor que provedores focados em object storage
- para um TCC com volume moderado, costuma ser suficiente

### Servico de IA

- se a IA tambem precisar ficar online 24/7, talvez nao caiba confortavelmente em um stack 100% gratis
- para TCC, uma estrategia valida e deixar a IA em outro ambiente, rodar localmente na apresentacao ou ativar apenas para demos

## Alternativa mais simples, mas nao totalmente gratis

Se voces aceitarem um custo muito baixo para reduzir integracoes, o Railway continua sendo a opcao mais simples para juntar backend, PostgreSQL e talvez MinIO no mesmo lugar.

Eu manteria a recomendacao assim:

- `gratis e simples`: Render + Supabase
- `mais simples`: Railway
