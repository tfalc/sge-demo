# SGE — Sistema de Gestão Escolar

Documento de especificação técnica para implementação com o Cursor AI.

---

## Visão geral

Sistema web/mobile para gestão completa de escolas de educação básica. O produto substitui processos manuais (carnês, agendas de papel, comunicados físicos) por uma plataforma digital unificada. O MVP foca no módulo financeiro com PIX, expandindo progressivamente para os demais módulos.

---

## Stack tecnológica

| Camada | Tecnologia | Justificativa |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 | Familiaridade do time, ecossistema maduro |
| Frontend | React 18 + TypeScript | PWA — web e mobile unificados |
| Banco principal | PostgreSQL 16 | Dados relacionais complexos |
| Cache / sessões | Redis 7 | Performance e controle de sessão JWT |
| Object storage | MinIO (self-hosted) ou S3 | Boletins PDF, documentos |
| Filas | RabbitMQ | Notificações assíncronas |
| Auth | Spring Security + JWT + OAuth2 | SSO futuro |
| Build frontend | Vite | |
| CSS | Tailwind CSS | |
| Testes backend | JUnit 5 + Testcontainers | |
| Testes frontend | Vitest + React Testing Library | |
| CI/CD | GitHub Actions | |
| Containers | Docker + Docker Compose | |

---

## Arquitetura geral

```
┌─────────────────────────────────────────────────────────┐
│                  Portais (React PWA)                    │
│   Pais | Professor | Coordenação | Secretaria | Direção │
└────────────────────────┬────────────────────────────────┘
                         │ HTTPS
┌────────────────────────▼────────────────────────────────┐
│            API Gateway + Auth (JWT / OAuth2)            │
│                   Spring Boot 3                         │
└──┬──────┬──────┬──────┬──────┬──────┬──────┬───────────┘
   │      │      │      │      │      │      │
   ▼      ▼      ▼      ▼      ▼      ▼      ▼
Acadêm. Financ. Comunic. Saúde Relat. Secr. Notif.
                                  +IA
┌─────────────────────────────────────────────────────────┐
│                   Camada de dados                       │
│   PostgreSQL | Redis | MinIO/S3 | RabbitMQ              │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│               Integrações externas                      │
│   OpenPix (PIX) | Claude API (IA) | SendGrid | FCM     │
└─────────────────────────────────────────────────────────┘
```

---

## Estrutura de pastas

### Backend (Spring Boot)

```
sge-backend/
├── src/main/java/br/com/sge/
│   ├── SgeApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   └── CorsConfig.java
│   ├── shared/
│   │   ├── exception/GlobalExceptionHandler.java
│   │   ├── dto/ApiResponse.java
│   │   └── util/DateUtil.java
│   ├── modules/
│   │   ├── auth/
│   │   │   ├── controller/AuthController.java
│   │   │   ├── service/AuthService.java
│   │   │   └── dto/LoginRequest.java
│   │   ├── academico/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   ├── financeiro/
│   │   ├── comunicacao/
│   │   ├── saude/
│   │   ├── relatorios/
│   │   ├── secretaria/
│   │   └── notificacoes/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
└── src/test/
```

### Frontend (React)

```
sge-frontend/
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── router/
│   │   └── index.tsx          # React Router v6
│   ├── store/                 # Zustand
│   ├── hooks/
│   ├── services/              # API clients (axios)
│   │   ├── api.ts             # instância base axios
│   │   ├── authService.ts
│   │   ├── academicoService.ts
│   │   ├── financeiroService.ts
│   │   └── ...
│   ├── pages/
│   │   ├── auth/
│   │   ├── pais/
│   │   ├── professor/
│   │   ├── coordenacao/
│   │   ├── secretaria/
│   │   └── direcao/
│   ├── components/
│   │   ├── ui/                # componentes base (Button, Input, Modal...)
│   │   ├── layout/
│   │   └── charts/            # Recharts
│   └── types/
│       └── index.ts
├── public/
│   └── manifest.json          # PWA manifest
├── vite.config.ts
└── tailwind.config.ts
```

---

## Modelo de dados

### Entidades principais

```sql
-- Escola e estrutura
CREATE TABLE escola (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(200) NOT NULL,
  cnpj VARCHAR(18) UNIQUE,
  criado_em TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE ano_letivo (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  escola_id UUID REFERENCES escola(id),
  ano INT NOT NULL,
  data_inicio DATE NOT NULL,
  data_fim DATE NOT NULL
);

CREATE TABLE nivel_ensino (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(100) NOT NULL,  -- Ex: "Ensino Fundamental I"
  descricao TEXT
);

CREATE TABLE serie (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nivel_id UUID REFERENCES nivel_ensino(id),
  nome VARCHAR(50) NOT NULL,   -- Ex: "3º Ano"
  ordem INT NOT NULL
);

CREATE TABLE turma (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  serie_id UUID REFERENCES serie(id),
  ano_letivo_id UUID REFERENCES ano_letivo(id),
  nome VARCHAR(20) NOT NULL,   -- Ex: "3A"
  capacidade_max INT DEFAULT 30
);

-- Pessoas
CREATE TABLE pessoa (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(200) NOT NULL,
  cpf VARCHAR(14) UNIQUE,
  email VARCHAR(200),
  telefone VARCHAR(20),
  data_nascimento DATE,
  criado_em TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE usuario (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pessoa_id UUID REFERENCES pessoa(id),
  email VARCHAR(200) UNIQUE NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  perfil VARCHAR(30) NOT NULL,  -- ADMIN, PROFESSOR, COORDENADOR, SECRETARIA, PAI
  ativo BOOLEAN DEFAULT TRUE
);

-- Alunos e vínculos
CREATE TABLE aluno (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pessoa_id UUID REFERENCES pessoa(id),
  matricula VARCHAR(20) UNIQUE NOT NULL,
  turma_id UUID REFERENCES turma(id),
  status VARCHAR(20) DEFAULT 'ATIVO'  -- ATIVO, TRANSFERIDO, FORMADO
);

CREATE TABLE responsavel (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pessoa_id UUID REFERENCES pessoa(id),
  usuario_id UUID REFERENCES usuario(id),
  grau_parentesco VARCHAR(50)
);

CREATE TABLE aluno_responsavel (
  aluno_id UUID REFERENCES aluno(id),
  responsavel_id UUID REFERENCES responsavel(id),
  PRIMARY KEY (aluno_id, responsavel_id)
);

CREATE TABLE professor (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pessoa_id UUID REFERENCES pessoa(id),
  usuario_id UUID REFERENCES usuario(id),
  registro_mec VARCHAR(50)
);

-- Acadêmico
CREATE TABLE disciplina (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(100) NOT NULL,
  codigo VARCHAR(20)
);

CREATE TABLE turma_disciplina_professor (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  turma_id UUID REFERENCES turma(id),
  disciplina_id UUID REFERENCES disciplina(id),
  professor_id UUID REFERENCES professor(id),
  ano_letivo_id UUID REFERENCES ano_letivo(id)
);

CREATE TABLE periodo_avaliacao (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ano_letivo_id UUID REFERENCES ano_letivo(id),
  nome VARCHAR(50) NOT NULL,   -- Ex: "1º Bimestre"
  data_inicio DATE,
  data_fim DATE
);

CREATE TABLE nota (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aluno_id UUID REFERENCES aluno(id),
  turma_disciplina_professor_id UUID REFERENCES turma_disciplina_professor(id),
  periodo_id UUID REFERENCES periodo_avaliacao(id),
  valor NUMERIC(4,2) CHECK (valor >= 0 AND valor <= 10),
  tipo VARCHAR(30),  -- PROVA, TRABALHO, PARTICIPACAO
  observacao TEXT,
  lancado_em TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE presenca (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aluno_id UUID REFERENCES aluno(id),
  turma_disciplina_professor_id UUID REFERENCES turma_disciplina_professor(id),
  data_aula DATE NOT NULL,
  presente BOOLEAN NOT NULL,
  justificativa TEXT
);

-- Financeiro
CREATE TABLE plano_pagamento (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nome VARCHAR(100) NOT NULL,
  valor_mensalidade NUMERIC(10,2) NOT NULL,
  dia_vencimento INT DEFAULT 10
);

CREATE TABLE contrato (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aluno_id UUID REFERENCES aluno(id),
  plano_id UUID REFERENCES plano_pagamento(id),
  ano_letivo_id UUID REFERENCES ano_letivo(id),
  data_inicio DATE,
  status VARCHAR(20) DEFAULT 'ATIVO'
);

CREATE TABLE cobranca (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  contrato_id UUID REFERENCES contrato(id),
  competencia DATE NOT NULL,   -- primeiro dia do mês
  valor NUMERIC(10,2) NOT NULL,
  vencimento DATE NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDENTE',  -- PENDENTE, PAGO, VENCIDO, CANCELADO
  pix_txid VARCHAR(100),       -- ID da transação no PSP
  pix_qrcode TEXT,
  pago_em TIMESTAMPTZ
);

-- Comunicação
CREATE TABLE comunicado (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  titulo VARCHAR(200) NOT NULL,
  conteudo TEXT NOT NULL,
  publicado_por UUID REFERENCES usuario(id),
  publicado_em TIMESTAMPTZ DEFAULT NOW(),
  visivel_para VARCHAR(30)[]   -- PAIS, PROFESSORES, TODOS
);

CREATE TABLE cardapio (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  data_refeicao DATE NOT NULL,
  tipo_refeicao VARCHAR(30) NOT NULL,  -- ALMOCO, LANCHE
  descricao TEXT NOT NULL,
  calorias INT,
  nutricionista_id UUID REFERENCES usuario(id)
);

CREATE TABLE evento_agenda (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  titulo VARCHAR(200) NOT NULL,
  descricao TEXT,
  data_inicio TIMESTAMPTZ NOT NULL,
  data_fim TIMESTAMPTZ,
  tipo VARCHAR(50),  -- REUNIAO, FERIADO, PROVA, EVENTO
  turma_id UUID REFERENCES turma(id)  -- NULL = escola toda
);

-- Saúde / Psicologia
CREATE TABLE profissional_saude (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pessoa_id UUID REFERENCES pessoa(id),
  usuario_id UUID REFERENCES usuario(id),
  especialidade VARCHAR(100),  -- PSICOLOGA, NUTRICIONISTA
  registro_conselho VARCHAR(50)
);

CREATE TABLE agendamento_saude (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aluno_id UUID REFERENCES aluno(id),
  profissional_id UUID REFERENCES profissional_saude(id),
  data_hora TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) DEFAULT 'AGENDADO',  -- AGENDADO, REALIZADO, CANCELADO
  observacoes TEXT,
  privado BOOLEAN DEFAULT TRUE  -- se TRUE, pais não veem o conteúdo
);
```

---

## Módulos — detalhamento

### 1. Autenticação

- Login com email + senha
- JWT com refresh token (access: 1h, refresh: 7d)
- Perfis: `ADMIN`, `DIRETOR`, `COORDENADOR`, `PROFESSOR`, `SECRETARIA`, `PAI`
- Cada perfil tem acesso a rotas específicas via anotações Spring Security

**Endpoints:**
```
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/esqueci-senha
```

---

### 2. Módulo Acadêmico

**Responsabilidades:** gerenciar estrutura de turmas, disciplinas, notas e frequência.

**Endpoints:**
```
GET    /api/turmas
POST   /api/turmas
GET    /api/turmas/{id}/alunos
GET    /api/turmas/{id}/notas
POST   /api/notas
PUT    /api/notas/{id}
POST   /api/presencas/lancamento   -- lançamento em lote por aula
GET    /api/alunos/{id}/boletim
GET    /api/alunos/{id}/frequencia
```

**Regras de negócio:**
- Nota mínima para aprovação: configurável por escola (padrão 6.0)
- Frequência mínima: 75% das aulas
- Boletim calculado por período e consolidado no final do ano
- Professor só lança notas/presença nas turmas em que está vinculado

---

### 3. Módulo Financeiro + PIX

**Integração PIX via OpenPix (recomendada) ou EFÍ Bank.**

**Fluxo de cobrança:**
1. Sistema gera cobranças automaticamente no início de cada mês
2. PSP cria um QR Code PIX estático por cobrança
3. Responsável acessa o portal, visualiza a cobrança e faz o PIX
4. PSP notifica o sistema via webhook
5. Sistema atualiza o status e notifica o responsável

**Integração OpenPix:**
```java
// Dependência: cliente REST via RestTemplate ou WebClient
// Documentação: https://developers.openpix.com.br/

POST https://api.openpix.com.br/api/v1/charge
Authorization: AppID {APP_ID}

{
  "correlationID": "{cobranca_id}",
  "value": 85000,          // centavos
  "comment": "Mensalidade Março 2025 — João Silva",
  "expiresIn": 2592000     // 30 dias em segundos
}
```

**Webhook PIX (endpoint no backend):**
```
POST /api/financeiro/webhook/pix
-- Validar assinatura do PSP antes de processar
-- Atualizar cobranca.status = 'PAGO' e cobranca.pago_em
-- Publicar evento no RabbitMQ para disparo de notificação
```

**Endpoints:**
```
GET  /api/financeiro/cobrancas?responsavelId=
GET  /api/financeiro/cobrancas/{id}/qrcode
POST /api/financeiro/webhook/pix
GET  /api/financeiro/inadimplentes         -- secretaria
GET  /api/financeiro/relatorio-mensal      -- direção
```

---

### 4. Módulo Comunicação

**Funcionalidades:** comunicados, cardápio da nutricionista, calendário escolar, agenda de eventos.

**Endpoints:**
```
GET  /api/comunicados
POST /api/comunicados
GET  /api/cardapio?data=2025-03-01
POST /api/cardapio
GET  /api/agenda?inicio=&fim=
POST /api/agenda
```

**Regras:**
- Cardápio é criado pela nutricionista com login próprio
- Comunicados podem ser segmentados por turma, série ou escola toda
- Agenda inclui provas, feriados, reuniões e eventos culturais

---

### 5. Módulo Saúde / Psicologia

**Funcionalidades:** agenda da psicóloga, histórico de atendimentos, contatos de emergência.

**Regras de privacidade:**
- Anotações clínicas da psicóloga são privadas — pais veem apenas data e status
- Coordenação e direção podem ver um resumo anonimizado por turma
- LGPD: dados sensíveis criptografados em repouso (`pgcrypto`)

**Endpoints:**
```
GET  /api/saude/agenda/{profissionalId}
POST /api/saude/agendamentos
GET  /api/saude/alunos/{id}/historico    -- restrito
POST /api/saude/agendamentos/{id}/nota   -- restrito à profissional
```

---

### 6. Módulo Relatórios + IA

**Funcionalidades:** gráficos de notas e frequência por turma, análise de alunos em risco.

**Análise com IA:**
- Integração com Claude API (Anthropic)
- Input: histórico de notas, frequência e observações dos professores do aluno
- Output: relatório em linguagem natural com sugestões para a coordenação

```java
// Exemplo de prompt para análise
String prompt = """
    Analise o desempenho acadêmico do aluno abaixo e gere um relatório
    para a coordenação pedagógica, identificando possíveis causas de
    dificuldade e sugestões de intervenção. Seja objetivo e empático.

    Dados do aluno:
    Nome: %s | Turma: %s | Período: %s

    Histórico de notas (últimos 2 bimestres):
    %s

    Frequência: %.1f%% (mínimo: 75%%)

    Observações dos professores:
    %s
    """.formatted(nome, turma, periodo, notas, frequencia, observacoes);
```

**Endpoints:**
```
GET  /api/relatorios/turma/{id}/desempenho
GET  /api/relatorios/turma/{id}/frequencia
GET  /api/relatorios/aluno/{id}/analise-ia
GET  /api/relatorios/escola/inadimplencia
POST /api/relatorios/boletim/{alunoId}/gerar-pdf
```

---

### 7. Módulo Notificações

**Canal:** push notification via FCM (Firebase Cloud Messaging) + e-mail via SendGrid.

**Eventos que disparam notificações:**

| Evento | Canal | Destinatário |
|---|---|---|
| Cobrança gerada | Push + Email | Responsável |
| Pagamento confirmado | Push | Responsável |
| Cobrança vencida | Push + Email | Responsável |
| Nova nota lançada | Push | Responsável |
| Falta registrada | Push | Responsável |
| Novo comunicado | Push | Conforme segmentação |
| Agendamento psicóloga | Push + Email | Responsável |

---

## Segurança

```java
// Mapeamento de permissões por perfil
ADMIN        -> tudo
DIRETOR      -> leitura geral + relatórios + gestão de usuários
COORDENADOR  -> acadêmico (leitura/escrita) + relatórios + saúde (resumo)
PROFESSOR    -> notas/presença das próprias turmas + comunicados
SECRETARIA   -> financeiro + cadastros + comunicados + secretaria
PAI          -> leitura dos próprios filhos
NUTRICIONISTA -> cardápio
PSICOLOGA    -> agenda e anotações de saúde
```

- Senhas: BCrypt (strength 12)
- Dados sensíveis: criptografia em repouso com `pgcrypto`
- Rate limiting: Bucket4j (login: 5 tentativas / 15min)
- CORS: apenas origens configuradas em `application.yml`
- Logs de auditoria para acessos a dados sensíveis (saúde)

---

## Docker Compose (desenvolvimento)

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: sge
      POSTGRES_USER: sge
      POSTGRES_PASSWORD: sge123
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"    # UI de gerenciamento

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - miniodata:/data

volumes:
  pgdata:
  miniodata:
```

---

## Variáveis de ambiente

```env
# application-dev.yml equivalente como .env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sge
SPRING_DATASOURCE_USERNAME=sge
SPRING_DATASOURCE_PASSWORD=sge123

SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

JWT_SECRET=seu-segredo-aqui-minimo-256-bits
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=604800000

OPENPIX_APP_ID=seu-app-id-openpix
OPENPIX_WEBHOOK_SECRET=seu-segredo-webhook

ANTHROPIC_API_KEY=sua-chave-anthropic
ANTHROPIC_MODEL=claude-sonnet-4-20250514

SENDGRID_API_KEY=sua-chave-sendgrid
SENDGRID_FROM_EMAIL=noreply@suaescola.com.br

FCM_SERVER_KEY=sua-chave-fcm

MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=sge-docs
```

---

## Ordem de implementação (MVP → completo)

> **Última atualização:** jun/2026 — status do código em `school_crm`.

### Fase 1 — MVP (4–6 semanas)
- [x] Setup do projeto (Spring Boot + React + Docker Compose)
- [x] Módulo Auth (login, JWT, `/me`, esqueci-senha simulado) — *restrição de rotas API por perfil*
- [x] Cadastro de escola, alunos, responsáveis — *escola (nome, CNPJ, nota/freq mínima), vínculos, contratos e planos via UI*
- [~] Módulo Financeiro: cobranças + PIX — *PIX simulado + OpenPix dual-mode; webhook preparado*
- [x] Portal do responsável: cobranças e pagar via PIX (simulado)
- [x] Portal da secretaria: cobranças e inadimplentes

### Fase 2 — Acadêmico (4–6 semanas)
- [x] Cadastro de turmas, séries, disciplinas, professores — *API + UI com edição/exclusão*
- [x] Portal do professor: lançamento de notas e frequência
- [x] Boletim digital (PDF)
- [x] Portal dos pais: notas e frequência dos filhos

### Fase 3 — Comunicação (2–3 semanas)
- [x] Comunicados por turma/escola
- [x] Cardápio da nutricionista
- [x] Agenda de eventos — *calendário + edição/exclusão (secretaria)*
- [x] Notificações in-app (sino no header, sem FCM/e-mail)
- [ ] Notificações push (FCM) e e-mail

### Fase 4 — Coordenação + IA (3–4 semanas)
- [x] Dashboard da coordenação com gráficos (Recharts)
- [x] Análise de alunos em risco — *motor nativo `AnaliseInteligenteService` (sem API externa)*
- [x] Relatórios exportáveis — *PDF boletim + APIs de resumo + export CSV na coordenação*

### Fase 5 — Saúde e refinamentos (2–3 semanas)
- [x] Agenda da psicóloga
- [x] Histórico de atendimentos (dados privados) — *API com filtro privado + portal pais (aba Saúde)*
- [x] Portal da direção
- [x] Testes automatizados (Vitest frontend, JUnit backend, Testcontainers Postgres)
- [x] PWA (manifest, icones, service worker)
- [ ] Testes de carga e otimizações

### Legenda
| Símbolo | Significado |
|---------|-------------|
| `[x]` | Implementado e testável localmente |
| `[~]` | Parcial / em progresso |
| `[ ]` | Não iniciado |

### Credenciais de teste (seed)
| Perfil | E-mail | Senha |
|--------|--------|-------|
| Admin / Secretaria | `admin@sge.com` | `admin123` |
| Pais | `pai@sge.com` | `admin123` |
| Aluno | `aluno@sge.com` | `admin123` |
| Professor | `prof@sge.com` | `admin123` |
| Nutricionista | `nutri@sge.com` | `admin123` |
| Psicóloga | `psico@sge.com` | `admin123` |
| Coordenação | `coord@sge.com` | `admin123` |
| Direção | `diretor@sge.com` | `admin123` |

---

## Qualidade de arquitetura (SOLID + 12-Factor)

Avaliação de referência (jun/2026). Detalhes e evidências no histórico do projeto.

### SOLID — nota B−

| Princípio | Status | Notas |
|-----------|--------|-------|
| SRP | 🟡 | Módulos por domínio ok; serviços grandes (`FinanceiroService`, `AcademicoService`) |
| OCP | 🟡 | `SchoolPackageLoader`, PIX dual-mode; `SecurityConfig` monolítico |
| LSP | ✅ | Pouca herança; sem violações graves |
| ISP | 🟡 | Sem interfaces de serviço; só repositórios |
| DIP | 🟡 | DI Spring ok; dependência de classes concretas |

**Melhorias planejadas:** DTOs tipados (substituir `Map<String,Object>`), interfaces de serviço onde houver swap (PIX, notificações), quebrar `FinanceiroService`.

### 12-Factor — nota C+

| Fator | Status |
|-------|--------|
| I Codebase | ✅ Mono-repo |
| II Dependencies | 🟡 Maven/npm; Docker app em progresso |
| III Config | 🟡 Env vars; validator de secrets em `prod` |
| IV Backing services | 🟡 Postgres ativo; Redis/Rabbit no compose, não no código |
| V Build/release/run | 🟡 CI build; compose `demo` profile |
| VI Processes | ✅ JWT stateless |
| VII Port binding | ✅ 8080 / 5173 |
| VIII Concurrency | 🟡 Jobs no processo web |
| IX Disposability | 🟡 Actuator health |
| X Dev/prod parity | 🟡 H2 dev vs Postgres prod |
| XI Logs | 🟡 SLF4J; structured logging pendente |
| XII Admin | 🟡 Flyway ok; jobs in-app |

---

## Trilha 1 — Demo generica (escola-demo)

**Objetivo:** apresentar app funcional com dados genericos (sem identificacao de escola real).

Roteiro: **`DEMO.md`**. Restaurar CEM: **`schools/_private/CEM_RESTAURACAO.md`**.

| Item | Status | Notas |
|------|--------|-------|
| Docker demo (`demo` profile) | [x] | Postgres + API + frontend |
| Scripts start/stop | [x] | `demo-start.ps1` / `demo-stop.ps1` |
| Roteiro 10 min (4 blocos) | [x] | Família → Secretaria → Professor → Coordenação |
| Frases abertura/fechamento | [x] | Em `DEMO.md` |
| Branding escola no login | [x] | `schools/escola-demo/escola.yaml` |
| **Seed CEM enriquecido** | [x] | Migration `V17__cem_demo_seed.sql` |
| Login vazio em build demo | [x] | `VITE_DEMO_BUILD` no Docker |
| Ensaiar com dados reais do colégio | [ ] | Ajustar valores/nomes após feedback da direção |
| **Rematrícula online (MVP)** | [x] | Upload PDF → editor → portal pais → Validar → confirmar → PDF + alerta secretaria |

### Rematrícula — fluxo acordado (MVP)

1. **Secretaria** (`/secretaria/rematricula`): envia PDF modelo (processado no servidor); revisa sugestões de campos extraídas; edita seções/campos; publica o período.
2. **Pais** (`/pais/rematricula`): preenche formulário digital por filho; **Validar** → tela de revisão → **Confirmar e enviar**.
3. **Sistema**: gera PDF preenchido (OpenPDF); notifica secretaria/admin (`REMATRICULA_ENVIADA`); secretaria valida na mesma tela.
4. **Fase futura**: parsing AcroForm avançado; preenchimento sobre o PDF original; assinatura **gov.br**.

Migration: `V18__rematricula_schema.sql`. API: `/api/rematricula/**`.

### Conteúdo do seed CEM (V17)

- Escola: CNPJ fictício, nota/freq mínima; plano “Mensalidade CEM”
- Turma **3A**: vínculos professor × 7 componentes da matriz + grade horária ampliada
- Família **Oliveira**: João Silva + **Ana Beatriz** (2ª filha) — cobranças pendentes para `pai@sge.com`
- **Ana Beatriz**: notas baixas e faltas para demo da análise na coordenação
- Comunicados e agenda com texto generico (V21 neutraliza seeds historicos)
- Financeiro: mês anterior pago, janeiro vencido, **mês atual pendente** (PIX na demo)

### Fora do escopo desta demo

Não prometer nem abrir na reunião: matrícula online completa, deploy público, OWASP hardening, rate limit, assinatura gov.br — ver Trilha 2. Rematrícula MVP pode ser demonstrada em ambiente local.

---

## Trilha 2 — Produto pós-demo

Evolução técnica e comercial **após** validação com a direção do CEM.

### Segurança (OWASP ASVS Nível 1)

| Fase | Itens | Status |
|------|-------|--------|
| Acesso | `canAccessCobranca`, guards de rota | [x] |
| Acesso | Professor só nas suas turmas; testes financeiro | [ ] |
| Auth | Rate limit login, esqueci-senha sem enumeração, refresh token | [ ] |
| Config | Secrets prod, Swagger off prod | [x] parcial |
| Config | Security headers; webhook PIX obrigatório em prod | [ ] |
| CI | Dependency-Check | [x] |
| CI | ZAP baseline; mais testes `ApiSecurityIntegrationTest` | [ ] |

### Infra e qualidade

| Item | Status |
|------|--------|
| Deploy staging (HTTPS) | [ ] |
| DTOs tipados (menos `Map`) | [ ] |
| Redis/Rabbit em uso ou removidos do compose | [ ] |
| Testes de carga | [ ] |
| Gaps `benchmark-delta.md` (matrícula, etc.) | [ ] conforme prioridade pós-reunião |

### Mapeamento OWASP Top 10 (pós-demo)

| Risco | Mitigação |
|-------|-----------|
| A01 Broken Access Control | Trilha 2 — acesso por turma, testes |
| A02 Cryptographic Failures | Secrets obrigatórios em prod |
| A05 Security Misconfiguration | Headers, Swagger/H2 off prod |
| A06 Vulnerable Components | Dependency-Check CI |
| A07 Auth Failures | Rate limit, tokens |
| A09 Logging Failures | Audit log em erros |

---

## Convenções de código

- Idioma do código: inglês (variáveis, métodos, classes)
- Idioma de comentários e mensagens de usuário: português
- Padrão de commits: Conventional Commits (`feat:`, `fix:`, `docs:`)
- Branches: `main` (prod), `develop`, `feature/nome-da-feature`
- Endpoints REST em português (reflete o domínio): `/api/turmas`, `/api/cobrancas`
- DTOs separados de entidades (nunca expor entidade JPA diretamente na API)
- Use `record` do Java 21 para DTOs imutáveis
- Tratamento de erros centralizado via `@ControllerAdvice`

---

## Referências e documentação

- [OpenPix API](https://developers.openpix.com.br/)
- [EFÍ Bank PIX](https://dev.efipay.com.br/docs/api-pix/credenciais)
- [Anthropic Claude API](https://docs.anthropic.com/)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/)
- [FCM HTTP v1 API](https://firebase.google.com/docs/cloud-messaging)
- [SendGrid API](https://docs.sendgrid.com/)
- [Bucket4j rate limiting](https://bucket4j.com/)