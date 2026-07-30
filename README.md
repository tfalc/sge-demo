# SGE (School CRM)

Este repositorio contem o MVP inicial do **SGE — Sistema de Gestao Escolar**, conforme `to-do.md`.

## Demo rápida

```powershell
.\demo-start.ps1    # Docker: Postgres + API + frontend
# Frontend http://localhost:5173 — roteiro em DEMO.md
.\demo-stop.ps1
```

Health: `GET http://localhost:8080/actuator/health`

Plano em duas trilhas: **demo generica** (`DEMO.md`) e **produto pos-demo** (`to-do.md`).

**Registro de desenvolvimento:** [`DESENVOLVIMENTO.md`](DESENVOLVIMENTO.md)

**Restaurar dados CEM (escola real):** [`schools/_private/CEM_RESTAURACAO.md`](schools/_private/CEM_RESTAURACAO.md)

## Estrutura

- `sge-backend/`: API Spring Boot 3 (Java 21)
- `sge-frontend/`: PWA React + Vite + TypeScript + Tailwind
- `sge-launcher/`: janela desktop (iniciar/finalizar + logs) para a versao portatil
- `docker-compose.yml`: Postgres, Redis, RabbitMQ e MinIO para desenvolvimento

## Subir dependencias (Docker)

Na raiz do projeto:

```bash
docker compose up -d
```

## Backend

Requisitos: JDK 21 + Maven.

### Modo dev (sem Postgres local)

```bash
cd sge-backend
set SPRING_PROFILES_ACTIVE=dev
mvn -DskipTests spring-boot:run
```

### Modo com Postgres (Docker Compose)

```bash
docker compose up -d postgres
cd sge-backend
mvn -DskipTests spring-boot:run
```

### Postgres ja instalado (sem Docker)

1. Crie banco e usuario (ajuste senha se quiser; o padrao da API e `sge` / `sge123`):

```sql
CREATE USER sge WITH PASSWORD 'sge123';
CREATE DATABASE sge OWNER sge;
```

2. Suba o **backend sem** o profile `dev` (o `dev` usa H2 em memoria):

```bash
cd sge-backend
mvn -DskipTests spring-boot:run
```

Na primeira subida o **Flyway** aplica `V1__schema.sql` e `V2__seed.sql` (tabelas + dados de exemplo).

### Credenciais MVP (login)

| Perfil | Email | Senha |
|--------|-------|-------|
| Admin | `admin@sge.com` | `admin123` |
| Secretaria | `secretaria@sge.com` | `admin123` |
| Pai (portal cobrancas seed) | `pai@sge.com` | `admin123` |

O usuario `pai@sge.com` esta vinculado ao responsavel de teste no seed; o portal dos pais descobre isso via `GET /api/auth/me`.

## Frontend

Requisitos: Node.js 20+ (recomendado).

```bash
cd sge-frontend
npm install
npm run dev
npm test          # Vitest
```

### PWA

O app pode ser instalado no celular/desktop (menu do navegador → **Instalar app**). Inclui `manifest.json`, icones e service worker (cache offline basico da UI).

### Notificacoes in-app

Com o usuario logado, o sino no header mostra alertas locais (sem push/e-mail): cobrancas, notas, faltas, comunicados e agendamentos de saude. API: `GET /api/notificacoes`.

Em **desenvolvimento** (`npm run dev`), o Vite faz **proxy** de `/api` para `http://localhost:8080` — evita CORS em qualquer porta do Vite. Em producao, a API fica em `http://localhost:8080` (veja `sge-frontend/src/services/api.ts`).

### Login falha no navegador (CORS / rede / 500)

- O backend precisa estar **rodando** na porta **8080** com o **codigo atual** (reinicie apos `git pull`; migration Flyway **V12** cria aluno e horarios).
- Credencial aluno: `aluno@sge.com` / `admin123`.
- Se `admin@sge.com` loga mas `aluno@sge.com` da **500**, o processo na 8080 esta desatualizado — pare e rode `mvn spring-boot:run` de novo.
- DevTools > Network: `POST /api/auth/login` deve retornar **200**. CORS usa padroes `http://localhost:*` no backend.

## Roteiro de teste local (MVP financeiro)

1. Suba o Postgres: `docker compose up -d postgres`
2. Backend (sem profile `dev`): `cd sge-backend && mvn -DskipTests spring-boot:run`
3. Frontend: `cd sge-frontend && npm install && npm run dev`
4. **Admin / Direção** — login `admin@sge.com` ou `diretor@sge.com` / `admin123`:
   - Veja cards do relatorio mensal e tabela de inadimplentes
   - Gere uma nova cobranca pelo formulario
5. **Portal dos Pais** — login `pai@sge.com` / `admin123`:
   - Veja cobrancas do filho (Joao Silva)
   - Clique em **Pagar PIX** → copie o codigo ou use **Simular pagamento (local)**
   - A cobranca deve mudar para **Pago**; volte no financeiro e confira o relatorio
6. **Secretaria** — login `secretaria@sge.com` / `admin123`: matrícula, comunicação e cadastro (sem financeiro estratégico)

Swagger (API): `http://localhost:8080/swagger-ui.html`

### Teste do modulo academico (Fase 2)

| Perfil | Login | O que testar |
|--------|-------|--------------|
| Professor | `prof@sge.com` / `admin123` | Lancar notas e frequencia da turma 3A |
| Pais | `pai@sge.com` / `admin123` | Aba **Desempenho**: boletim e frequencia do Joao Silva |

Fluxo sugerido: professor lanca nota do 2 bimestre → pai atualiza a pagina Desempenho e ve a nova media.

### Estrutura academica e financeiro automatico

| Perfil | Login | O que testar |
|--------|-------|--------------|
| Secretaria | `secretaria@sge.com` / `admin123` | Aba **Academico**: disciplina, professor, turma, vinculo |
| Secretaria | `secretaria@sge.com` / `admin123` | Aba **Cadastro**: novo responsavel vinculado a aluno |
| Admin / Direção | `admin@sge.com` ou `diretor@sge.com` / `admin123` | **Gerar cobrancas do mes** no financeiro |
| Pais | novo responsavel / `admin123` | Portal completo apos vinculo ao filho |

### Fases 4 e 5 (coordenacao, direcao, saude, cadastro)

| Perfil | Login | O que testar |
|--------|-------|--------------|
| Coordenacao | `coord@sge.com` / `admin123` | Dashboard com graficos e analise de alunos |
| Direcao | `diretor@sge.com` / `admin123` | Painel financeiro + indicadores academicos |
| Psicologia | `psico@sge.com` / `admin123` | Agenda e novos agendamentos |
| Secretaria | `secretaria@sge.com` / `admin123` | Aba **Cadastro** — listar/criar alunos |
| Pais | `pai@sge.com` / `admin123` | Desempenho → **Baixar PDF**; aba **Saude** (atendimentos nao privados) |
| Coordenacao | `coord@sge.com` / `admin123` | Botao **Exportar CSV** no dashboard |

> Reinicie o backend para aplicar migrations V7–V9.

### Teste do modulo comunicacao (Fase 3)

| Perfil | Login | O que testar |
|--------|-------|--------------|
| Nutricao | `nutri@sge.com` / `admin123` | Cadastrar itens do cardapio |
| Secretaria | `secretaria@sge.com` / `admin123` | Aba **Comunicacao**: publicar comunicado e evento |
| Pais | `pai@sge.com` / `admin123` | Aba **Comunicacao**: ver comunicados, cardapio e agenda |

> Reinicie o backend apos atualizar o codigo para aplicar as migrations V5/V6 no Postgres.

## Versao portatil (sem instalar Java na maquina destino)

E possivel gerar um **SGE.exe** com Java embutido (via `jpackage` do JDK 21). Na outra maquina basta executar o `.exe` e usar o navegador — sem Docker, sem Node, sem Postgres e **sem instalar Java**.

### Gerar o pacote (nesta maquina de desenvolvimento)

Requisitos: **JDK 21** (`JAVA_HOME`), Node 20+, Maven.

```powershell
.\scripts\build-portable.ps1
```

Saida: `dist-portable\SGE\` contendo `SGE.exe` e o runtime Java embutido (~150–200 MB). Compacte essa pasta em ZIP e envie para outra maquina.

### Usar na outra maquina

1. Extraia a pasta `SGE`
2. Execute **`Iniciar SGE.bat`** ou **`Iniciar SGE.vbs`** na pasta `dist-portable\SGE\` (recomendado; evite o `SGE.exe` no Windows)
3. Clique em **Iniciar app** (o navegador abre quando o servidor estiver pronto; se a porta 8080 estiver ocupada, usa 8081, 8082… automaticamente)
4. Para encerrar, use **Finalizar app** ou feche a janela (confirme **Sim** para encerrar o servidor)
5. Se algo travar, rode `.\scripts\kill-sge.ps1` para liberar portas e processos

Use qualquer conta de teste abaixo (senha **admin123** para todas):

| Perfil | Email |
|--------|-------|
| Admin (donos) | `admin@sge.com` |
| Secretaria | `secretaria@sge.com` |
| Pai | `pai@sge.com` |
| Professor | `prof@sge.com` |
| Nutricionista | `nutri@sge.com` |
| Psicologa | `psico@sge.com` |
| Coordenacao | `coord@sge.com` |
| Direcao | `diretor@sge.com` |

Os dados ficam em `data\` dentro da pasta do aplicativo (banco H2 em arquivo).

Se so `admin@sge.com` funcionar, apague a pasta `data\` e execute `SGE.exe` de novo (o seed completo roda apenas na primeira subida com banco vazio).

### Aviso do Windows SmartScreen ("aplicativo nao reconhecido")

Isso aparece porque o `SGE.exe` **nao tem assinatura digital** de uma empresa conhecida pela Microsoft. Nao e virus — e o comportamento padrao do Windows para apps distribuidos fora da Microsoft Store.

**Para o colega nao ver o aviso**, e preciso um **certificado de Code Signing** comercial (nao serve certificado autoassinado):

| Tipo | Efeito no SmartScreen | Custo aproximado |
|------|----------------------|------------------|
| **EV Code Signing** | Melhor opcao; reputacao imediata na maioria dos PCs | ~US$ 300–500/ano |
| **OV Code Signing** | Assina o app, mas a reputacao leva semanas ate o aviso sumir | ~US$ 100–250/ano |

Emissores comuns: DigiCert, Sectigo, SSL.com, GlobalSign.

#### Assinar o build (quando tiver o .pfx)

1. Instale o **Windows SDK** (traz o `signtool.exe`)
2. Defina as variaveis (nao commite o certificado no Git):

```powershell
$env:SGE_SIGN_PFX = "C:\caminho\seu-certificado.pfx"
$env:SGE_SIGN_PFX_PASSWORD = "senha-do-certificado"
.\scripts\build-portable.ps1
```

Ou assine um `.exe` ja gerado:

```powershell
.\scripts\sign-portable.ps1 -ExePath "dist-portable\SGE\SGE.exe"
```

#### Alternativas sem certificado (equipe pequena / interna)

- TI da escola distribui a pasta via **Intune/GPO** ou lista de apps permitidos
- Enviar por pen drive / rede interna (menos agressivo que download da internet)
- Documentar o fluxo **Mais informacoes → Executar assim mesmo** (uma vez por maquina)

### Testar o launcher desktop (sem gerar .exe)

Se voce ja tem Java 21 instalado:

```powershell
.\scripts\run-launcher-dev.ps1
```

### Testar o JAR standalone direto (sem interface grafica)

```powershell
.\scripts\build-portable.ps1   # ou apenas npm build + mvn -Pstandalone package
.\scripts\run-standalone-jar.ps1
```
