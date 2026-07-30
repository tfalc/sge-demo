# Deploy da demo online (Render)

Ambiente gratuito: Postgres + API (Docker) + frontend estático. Pacote escolar: `escola-demo` apenas.  
`schools/_private/` **não** entra no repositório público.

## Pré-requisitos

1. Conta [Render](https://render.com) (GitHub SSO)
2. Postgres free criado (ex.: `sge-demo-db`, região **Oregon**, Postgres 16)
3. Repositório GitHub **`sge-demo`** (este código, sem `_private`)

## Opção A — Blueprint (`render.yaml`)

1. No Render: **New** → **Blueprint**
2. Conecte o repo `sge-demo` (branch `main`)
3. Confirme os serviços `sge-api` e `sge-demo`
4. Se o Postgres `sge-demo-db` **já existir**, o Blueprint pode reclamar do nome — nesse caso use a Opção B ou remova a seção `databases` do `render.yaml` e vincule o banco manualmente à API
5. Após o deploy da API, confira `VITE_API_BASE_URL` no Static Site (deve ser a URL HTTPS da API, sem barra no final)
6. Redeploy do frontend se a URL da API só ficou conhecida depois do 1º build

## Opção B — Manual (recomendado se o banco já existe)

### 1) Web Service (API)

- **New** → **Web Service**
- Repo: `sge-demo` · branch `main`
- Runtime: **Docker**
- Dockerfile path: `sge-backend/Dockerfile`
- Docker build context: `.` (raiz do repo)
- Região: **Oregon** (igual ao Postgres)
- Plano: **Free**
- Health check: `/actuator/health`

Environment:

| Key | Valor |
|-----|--------|
| `DATABASE_URL` | *Link do Postgres* `sge-demo-db` → Internal Database URL (Render preenche) |
| `JWT_SECRET` | Gere uma string ≥ 32 caracteres |
| `SCHOOL_PACKAGE_ID` | `escola-demo` |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | `https://*.onrender.com,http://localhost:*` |

O entrypoint converte `postgres://...` → `jdbc:postgresql://...` automaticamente.

### 2) Static Site (UI)

- **New** → **Static Site**
- Repo: `sge-demo` · branch `main`
- Root directory: `sge-frontend`
- Build: `npm ci && npm run build`
- Publish: `dist`
- Plano: **Free**

Environment (build):

| Key | Valor |
|-----|--------|
| `VITE_DEMO_BUILD` | `true` |
| `VITE_API_BASE_URL` | URL pública da API, ex. `https://sge-api-xxxx.onrender.com` |

SPA: em Redirects/Rewrites, `/*` → `/index.html` (rewrite).

## Ordem prática

1. API no ar (`/actuator/health` = UP) — 1º boot pode demorar (Flyway + cold start)
2. Anote a URL da API
3. Crie/atualize o Static Site com `VITE_API_BASE_URL`
4. Abra a URL do frontend, escolha perfil e entre (`admin123`)

## Cold start

No free tier a API dorme ~15 min sem uso. O login avisa; se falhar, espere ~1 min e tente de novo.

## O que não sobe

- `schools/_private/` (escolas reais)
- `.cursor/`, `node_modules/`, `target/`, secrets locais
