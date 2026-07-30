# Registro de desenvolvimento — SGE

Documento para retomar o trabalho em visitas futuras.

**Pacote ativo:** `escola-demo` (generico). Dados CEM arquivados em `schools/_private/` — ver `CEM_RESTAURACAO.md`.

**Última atualização:** 29 jul/2026 (UX audit P0–P3: nav por perfil, a11y, empty states, seletor de filho, LGPD galeria, inbox Hoje).

**Regra operacional:** após alterações que exigem rebuild, o agente/dev deve rodar `.\demo-start.ps1` automaticamente (não pedir ao usuário).

---

## Como rodar

| Modo | Comando | URLs |
|------|---------|------|
| Demo Docker | `.\demo-start.ps1` | Frontend `:5173`, API `:8080` |
| Parar demo | `.\demo-stop.ps1` | — |
| Dev local | Postgres + `mvn spring-boot:run` + `npm run dev` | Ver `README.md` |
| Portable | `.\scripts\build-portable.ps1` | `dist-portable\SGE\` |

Roteiro de apresentação: **`DEMO.md`**. Roadmap geral: **`to-do.md`**.

---

## Migrations Flyway (V12–V30)

| Versão | Arquivo | Conteúdo |
|--------|---------|----------|
| V12 | `V12__aluno_portal_horarios.sql` | Login aluno (`aluno@sge.com`), tabela `horario_aula` |
| V13 | `V13__cem_monnerat_matriz_curricular.sql` | Matriz curricular (historico; dados neutralizados em V21) |
| V14 | `V14__escola_uf_varchar.sql` | Ajuste coluna UF |
| V15 | `V15__matriz_diretrizes_normativa.sql` | Modo DIRETRIZES + matriz `ef-ai-diretrizes-rj` |
| V16 | `V16__cobranca_pix_qr_image.sql` | QR Code PIX em imagem na cobrança |
| V17 | `V17__cem_demo_seed.sql` | Seed demo enriquecido (historico; neutralizado em V21) |
| V18 | `V18__rematricula_schema.sql` | Rematrícula online |
| V19 | `V19__cem_series_professores_seed.sql` | Séries 1º/2º ano, +2 professores |
| V20 | `V20__normativa_consulta_preservacao.sql` | Preservação de matrizes na normativa |
| V21 | `V21__generalizar_dados_demo.sql` | Neutraliza nomes CEM no banco → Escola Modelo Demo |
| V22 | `V22__ata_aula_schema.sql` | Ata de aula (diário do professor) |
| V23 | `V23__ocorrencia_disciplinar_schema.sql` | Ocorrências disciplinares (convivência) |
| V24 | `V24__matricula_nova_schema.sql` | Matrícula nova + documentos (GED) |
| V25 | `V25__restricao_alimentar_patrimonio.sql` | Restrições alimentares + patrimônio |
| V26 | `V26__colegiado_schema.sql` | Reuniões de colegiado pedagógico |
| V27 | `V27__diario_frequencia_meta.sql` | Metadados folha de frequência (aulas previstas, assinatura) |
| V28 | `V28__galeria_schema.sql` | Galeria de fotos (albuns + upload filesystem) |
| V29 | `V29__galeria_lgpd_consentimento.sql` | Consentimento de imagem (aluno + álbum LGPD) |
| V30 | `V30__demo_kpi_recebido_mes.sql` | Seed KPI recebido no mês + flag LGPD em álbum |
| V31 | `V31__usuario_secretaria_demo.sql` | Usuário `secretaria@sge.com` (perfil SECRETARIA) |
| V32 | `V32__perfil_acesso_area.sql` | Matriz de menus superiores por perfil (ADMIN edita) |

**Banco existente:** após nova migration, reinicie o backend (ou `demo-start.ps1 --build`). Volume Postgres antigo pode precisar reset se migration falhar por dados parciais.

---

## Funcionalidades implementadas (sessões recentes)

### 1. Rematrícula online (MVP)

- **Secretaria:** `/secretaria/rematricula` — upload PDF modelo, editor de campos, publicar período, fila de validação.
- **Pais:** `/pais/rematricula` — formulário por filho, validação, envio, notificação à secretaria.
- Backend: `modules/rematricula/` (PDFBox upload, OpenPDF preenchido).
- Migration V18 + `TipoNotificacao.REMATRICULA_ENVIADA`.
- Documentado em `DEMO.md` e `to-do.md`.

### 2. Horários — submenus (Secretaria)

Rotas em `/secretaria/horarios/*`:

| Submenu | Rota | Função |
|---------|------|--------|
| Grade atual | `/grade` | Cadastro de slot + card **Aulas de hoje** |
| Grade da turma | `/turma` | Calendário semanal + lista |
| Por matéria | `/disciplina` | Filtro por disciplina |
| Por professor | `/professor` | Filtro por professor |
| Calendário | `/calendario` | Visão geral |

Arquivos: `SecretariaHorariosLayout.tsx`, `horariosNav.ts`, `HorarioWeekCalendar.tsx`, `HorarioDiaVigente.tsx`, `horarioUtils.ts`.

### 3. Acadêmico — submenus (Secretaria)

Rotas em `/secretaria/academico/*`: Disciplinas, Professores, Turmas, Vínculos.

Arquivos: `SecretariaAcademicoLayout.tsx`, `academicoNav.ts`, páginas `SecretariaAcademico*Page.tsx`.

### 4. Comunicação — submenus (Secretaria)

Rotas em `/secretaria/comunicacao/*`: Comunicados, Eventos, Calendário.

Substituiu a página única `SecretariaCommunicationPage.tsx` (removida).

Arquivos: `SecretariaComunicacaoLayout.tsx`, `comunicacaoNav.ts`.

### 5. Cadastro — refatoração (instância única)

**Problema anterior:** vários cards empilhados + botão “Consultar normativa” no lugar errado + sensação de multi-escola.

**Atual** — `/secretaria/cadastro/*`:

| Submenu | Rota | Função |
|---------|------|--------|
| Escola | `/escola` | Dados da única instalação (nome, CNPJ, nota/freq mínima) |
| Alunos | `/alunos` | Cadastro + lista (vínculos com responsáveis) |
| Responsáveis | `/responsaveis` | Cadastro, vínculo, lista |

- **Normativa** removida do Cadastro (fica só em **Matriz**).
- Contratos de mensalidade: referência em Financeiro (criação saiu do cadastro empilhado).

**Pais cadastram filhos:** `/pais/filhos` — `POST /api/cadastro/meus-filhos` (sem exclusão pelo pai).

Arquivos: `SecretariaCadastroLayout.tsx`, `cadastroNav.ts`, `ParentFilhosPage.tsx`, `CadastroService.criarFilhoResponsavelLogado`.

### 6. Matriz curricular e normativa

- Página: `/secretaria/matriz` — consulta normativa, validação de turma vs matriz.
- **Preservação de dados (V20):**
  - **Consultar normativa** = somente leitura.
  - **Preview (Aplicar ao sistema)** = registra data da pesquisa.
  - **Confirmar** = atualiza escola/planos; **não sobrescreve** matrizes já salvas; só cria matrizes ausentes do pacote.
- Backend: `SchoolNormativaService` (`criarMatrizSeAusente` em vez de `sincronizarMatriz` destrutivo).
- UI: `NormativaEscolaPanel.tsx` com avisos de preservação.

### 7. Seeds demo (V17 + V19 + V21)

**V17/V19:** turma 3A, segunda aluna com desempenho fraco (coordenação), professores extras, séries 1º/2º ano.

**V21:** generaliza nomes da escola, comunicados e pessoas para ambiente demo (`Escola Modelo Demo`, `Ana Silva`, etc.). Restauração CEM: `schools/_private/CEM_RESTAURACAO.md`.

### 8. Correções de UX / bugs

| Item | Correção |
|------|----------|
| Matriz — ordem visual | Título antes do `SectionNav` (igual Rematrícula) |
| Perfil — menu some | `sectionNavResolver.ts` + persistência da última área em `sessionStorage`; `ProfilePage` exibe menu da secretaria/pais/aluno |
| Psicologia — “Falha ao carregar agenda” | `PSICOLOGA` ganhou `GET /api/cadastro/alunos` (lista para agendamento) |
| Demo PWA cache | Build demo desabilita PWA; headers no-cache no nginx; hard refresh após deploy |
| Rematrícula bytea | `@JdbcTypeCode(SqlTypes.VARBINARY)` nas entidades de rematrícula |

---

## Contas de teste (senha `admin123`)

| Perfil | E-mail | Destaque na demo |
|--------|--------|------------------|
| Admin (donos) | `admin@sge.com` | Acesso amplo + console **Admin** (`/admin/acessos`) para liberar menus por perfil — separado da Direção escolar |
| Direção | `diretor@sge.com` | Gestão estratégica em `/direcao/*` (financeiro, matriz, patrimônio, usuários…) |
| Secretaria | `secretaria@sge.com` | Gestão operacional em `/secretaria/*` (matrícula, comunicação, cadastro diário) |
| Pai | `pai@sge.com` | PIX, rematrícula, meus filhos |
| Professor | `prof@sge.com` | Matemática/Português 3A |
| Professor | `prof.carla@sge.com` | Humanas e Artes |
| Professor | `prof.paulo@sge.com` | Ciências e Ed. Física |
| Aluno | `aluno@sge.com` | Portal aluno |
| Coordenação | `coord@sge.com` | Colegiados + supervisão pedagógica |
| Psicologia | `psico@sge.com` | Agenda escolar |
| Nutrição | `nutri@sge.com` | Cardápio |

---

## Estrutura frontend — Gestão escolar (Direção + Secretaria)

Duas áreas reutilizam as mesmas páginas com prefixo de rota e menu distintos (`gestaoArea.ts`, `gestaoNav.ts`, `useGestaoArea.ts`, `router/gestaoRoutes.tsx`).

### Direção (`/direcao/*`) — estratégico / administrativo

Menu principal (`direcaoNav.ts`):

```
Painel | Financeiro | Comunicacao | Academico | Rematricula | Matricula nova | Patrimonio | Matriz | Horarios | Cadastro | Perfil
```

- Cadastro inclui **Usuários** (`/direcao/cadastro/usuarios`).
- Painel executivo (KPIs): `/direcao`.
- Perfil **DIRETOR** no topo: link **Direção** + **Coord.**

### Secretaria (`/secretaria/*`) — operacional

Menu principal (`secretariaNav.ts`):

```
Matricula nova | Comunicacao | Academico | Horarios | Cadastro | Perfil
```

- **Sem** Financeiro, Matriz, Rematrícula, Patrimônio nem Usuários (bloqueio em `routeAccess.ts`).
- Perfil **SECRETARIA** entra em `/secretaria/matricula-nova`.

### Submenus (ambas as áreas)

Layouts com segunda linha de abas (`*Layout.tsx` + `gestaoNav.ts`):

| Área | Submenu | Rotas |
|------|---------|-------|
| Comunicação | Comunicados, Eventos, Calendário | `/comunicacao/*` |
| Acadêmico | Disciplinas, Professores, Turmas, Vínculos | `/academico/*` |
| Horários | Grade, turma, matéria, professor, calendário | `/horarios/*` |
| Cadastro | Escola, Alunos, Responsáveis (+ Usuários só Direção) | `/cadastro/*` |

**Nota UX:** páginas com layout exibem **duas linhas** de navegação; páginas “flat” (ex.: Matrícula nova) exibem só a primeira — o conteúdo começa mais acima.

`SectionNav` suporta `matchPrefix` para rotas aninhadas. `sectionNavResolver.ts` resolve menus para `/direcao` e `/secretaria`.

### ADMIN

Acesso total a ambos os prefixos; menu superior inclui Secretaria e Direção.

---

## Pacote escola (demo)

```
schools/escola-demo/
  escola.yaml
  curriculo/*.yaml
```

Dados de escola real: `schools/_private/` (nao entra no JAR portable).

Backend: `SCHOOL_PACKAGE_ID=escola-demo` (padrao em `application.yml`).

---

## API relevante (novas/alteradas)

| Método | Rota | Quem |
|--------|------|------|
| GET | `/api/school/normativa` | Consulta (leitura) |
| GET | `/api/school/normativa/preview-aplicar` | Preview + registra data |
| POST | `/api/school/normativa/aplicar` | Aplica sem sobrescrever matrizes |
| POST | `/api/cadastro/meus-filhos` | PAI — cadastra filho vinculado |
| GET | `/api/cadastro/alunos` | Secretaria, Coord., Diretor, **PSICOLOGA** |
| * | `/api/rematricula/**` | Fluxo rematrícula |
| * | `/api/academico/matrizes/**` | Matriz e validação turma |
| * | `/api/colegiados/**` | Reuniões pedagógicas (coord., diretor) |
| GET/POST | `/api/presencas/matriz`, `POST .../assinar` | Folha de frequência bimestral |
| * | `/api/patrimonio/itens` | Inventário (DIRETOR + SECRETARIA) |
| PUT | `/api/admin/usuarios/{id}` | Gestão de perfis (DIRETOR + ADMIN) |

---

## Build portable

```powershell
# Requisitos: JAVA_HOME (JDK 21), Node 20+, Maven, jpackage
.\scripts\build-portable.ps1
# Saída: dist-portable\SGE\
# Iniciar: Iniciar SGE.vbs ou Iniciar SGE.bat (preferível a SGE.exe no Windows)
```

O perfil Maven `standalone` embute o frontend no JAR. Dados H2 ficam em `.\data\` na pasta do app. Flyway fica desligado no standalone; seeds e normalizacao rodam via `StandaloneSeedRunner` + `StandaloneDemoDataNormalizer`.

**Após alterações de código:** `.\scripts\build-portable.ps1` (usa `mvn clean package`). Se veio de versao com dados CEM, apague `.\data\` ou reinicie uma vez para normalizar.

---

## Roadmap modular (jun/2026)

Planejamento comercial alinhado aos 7 módulos de gestão escolar. Status após cada fase.

| Módulo | Meta comercial | Status atual | Observação |
|--------|----------------|--------------|------------|
| **Diário de Classe** | Professor registra aula, notas, frequência | Forte, com gaps vs. planilha | Fase 6.2 — folha de frequência |
| **Matrícula documental** | Matrícula + GED | MVP secretaria | Portal pais pendente |
| **Gestão Pedagógica** | Secretaria, matriz, horários, coordenação | Forte | Fase 6.1 — colegiados |
| **Convivência** | Ocorrências, mediação | MVP | — |
| **Finanças** | PIX, cobranças, inadimplência | MVP sólido | OpenPix prod |
| **Nutrição** | Cardápio + restrições | MVP | — |
| **Admin / Patrimonial** | Secretaria, patrimônio, usuários | MVP | — |

### Fase 1 — Diário de Classe completo (professor) — *concluída*

| Item | Descrição | Status |
|------|-----------|--------|
| 1.1 | Fichário de notas (bimestres + complemento) | [x] |
| 1.2 | Histórico de atas por turma/disciplina | [x] |
| 1.3 | Justificativa de falta na chamada | [x] |
| 1.4 | Impressão / exportação do fichário | [x] |
| 1.5 | Página **Diário** integrado (resumo turma) | [x] |

### Fase 2 — Convivência + supervisão pedagógica — *concluída*

| Item | Descrição | Status |
|------|-----------|--------|
| 2.1 | Ocorrências disciplinares (professor registra) | [x] |
| 2.2 | Coordenação visualiza atas e ocorrências | [x] |
| 2.3 | Notificação in-app para coordenação | [x] |

### Fase 3 — Matrícula documental — *concluída (MVP secretaria)*

| Item | Descrição | Status |
|------|-----------|--------|
| 3.1 | Fluxo matrícula nova (secretaria) | [x] |
| 3.2 | Upload de documentos (GED filesystem; MinIO preparado via env) | [x] |
| 3.3 | Status do processo (rascunho → análise → aprovado/rejeitado → concluído) | [x] |

API: `/api/matricula-nova/**`. UI: `/secretaria/matricula-nova`. Migration **V24**.

### Fase 4 — Finanças e admin (refino comercial)

| Item | Descrição | Status |
|------|-----------|--------|
| 4.1 | UI contrato de mensalidade na secretaria | [x] |
| 4.2 | UI gestão de usuários / perfis | [x] |

API admin: `/api/admin/usuarios`. UI: Financeiro (contratos) + Cadastro → Usuarios.

### Fase 5 — Nutrição e patrimônio (add-ons)

| Item | Descrição | Status |
|------|-----------|--------|
| 5.1 | Restrições alimentares por aluno | [x] |
| 5.2 | Inventário patrimonial básico | [x] |

API: `/api/nutricao/restricoes`, `/api/patrimonio/itens`. Migration **V25**.

### Fase 6 — Colegiados + diário eletrônico completo

Referência visual: planilha **DC2017MT.xlsx** (abas CAPA, FREQ, NOTAS, CONT, LISTA).

#### 6.1 — Colegiados pedagógicos (coordenação / direção)

| Item | Descrição | Status |
|------|-----------|--------|
| 6.1.1 | CRUD de reuniões (tipo, série/turma, data, pauta) | [x] |
| 6.1.2 | Participantes por perfil (professor, coord., psicologia…) | [x] |
| 6.1.3 | Painel de dados na pauta (alunos em risco, ocorrências, faltas) | [x] |
| 6.1.4 | Deliberações e encaminhamentos (responsável + prazo) | [x] |
| 6.1.5 | Ata de reunião (texto) + notificação in-app | [x] |
| 6.1.6 | Follow-up (pendências no painel da próxima reunião) | [x] |

UI: Coordenação → aba **Colegiados**. API: `/api/colegiados/**`. Migration **V26**.

#### 6.2 — Folha de frequência estilo diário (professor)

| Item | Descrição | Status |
|------|-----------|--------|
| 6.2.1 | Visão matricial: alunos × datas com P/F editável | [x] |
| 6.2.2 | Totais de faltas por aluno no período | [x] |
| 6.2.3 | Cabeçalho do diário (escola, bimestre, TDP) | [x] |
| 6.2.4 | Aulas dadas vs. previstas no bimestre | [x] |
| 6.2.5 | Impressão da folha de frequência | [x] |
| 6.2.6 | Assinatura do professor no período | [x] |
| 6.2.7 | Ordem numérica dos alunos | [x] |

API: `GET/POST /api/presencas/matriz`, `POST /api/presencas/matriz/assinar`. UI: Professor → Frequência → **Folha do bimestre**. Migration **V27**.

#### 6.3 — Colegiado escolar / Conselho (fase futura)

| Item | Descrição | Status |
|------|-----------|--------|
| 6.3.1 | Mandatos e segmentos (pais, alunos, docentes) | [ ] |
| 6.3.2 | Quórum, votação e atas institucionais | [ ] |
| 6.3.3 | Pautas PPP / financeiro / regimento + GED | [ ] |

### Fase 6.4 — Hierarquia Direção × Secretaria — *concluída (jun/2026)*

| Item | Descrição | Status |
|------|-----------|--------|
| 6.4.1 | Rotas espelhadas `/direcao/*` reutilizando páginas da secretaria | [x] |
| 6.4.2 | Menu estratégico na Direção; menu operacional na Secretaria | [x] |
| 6.4.3 | `routeAccess`: DIRETOR só `/direcao`; SECRETARIA sem rotas estratégicas | [x] |
| 6.4.4 | Títulos e `SectionNav` dinâmicos (`useGestaoArea`) | [x] |
| 6.4.5 | Backend: `DIRETOR` com escrita em financeiro, cadastro, patrimônio, rematrícula, matrícula nova, usuários | [x] |

Arquivos-chave: `gestaoNav.ts`, `gestaoRoutes.tsx`, `routeAccess.ts`, `navConfig.ts`, `SecurityConfig.java`, controllers Patrimônio/Rematrícula/MatrículaNova/UsuarioAdmin.

### 9. UX audit (jul/2026) — P0–P3

| Item | Descrição | Status |
|------|-----------|--------|
| Nav por perfil | ADMIN/SECRETARIA sem portais alheios; overflow “Mais” no mobile | [x] |
| A11y | `focus-visible`, contraste login/CTA | [x] |
| Empty states | `EmptyState` / `PageSkeleton` em painéis-chave | [x] |
| PT-BR | Acentuação e erros com próximo passo | [x] |
| Seed KPIs | Direção prefere turma 3A; V30 cobrança paga no mês | [x] |
| Filho global | `parentFilhoStore` + seletor nas páginas dos pais | [x] |
| LGPD galeria | `autoriza_uso_imagem` / `exigir_consentimento_imagem` | [x] |
| Inbox Hoje | `/pais|aluno|professor/*/hoje` e `/secretaria|direcao/hoje` | [x] |

---

## Próximos passos sugeridos (pós Fase 6)

- Fase 6.3 — conselho de escola (institucional).
- Padronizar UX do menu duplo (sub-abas na mesma faixa ou sticky) nas áreas com layout.
- Portal pais para matricula nova (autoatendimento).
- MinIO em producao (substituir filesystem via `APP_STORAGE_PATH` / driver MinIO).
- PIX real (OpenPix) em produção; assinatura gov.br na rematrícula.
- **Commit pendente** — alterações da sessão 9/jun ainda não commitadas (ver seção abaixo).

---

## Arquivos removidos / substituídos

| Removido | Substituído por |
|----------|-----------------|
| `SecretariaCommunicationPage.tsx` | Layout + 3 páginas Comunicação |
| `SecretariaCadastroPage.tsx` | Layout + Escola/Alunos/Responsáveis |
| `SecretariaAcademicoPage.tsx` (monolítico) | Layout + 4 páginas Acadêmico |

---

## Checklist ao retomar desenvolvimento

1. `git pull` (branch atual: `school/cem-monnerat`)
2. `.\demo-start.ps1` (web) ou `.\scripts\build-portable.ps1` (portable)
3. Confirmar Flyway ate V27 no Postgres; no portable, conferir `escola-demo` no cabecalho
4. Hard refresh no browser (`Ctrl+Shift+R`) apos rebuild frontend
5. Consultar este arquivo + `DEMO.md` + `to-do.md`

---

## Estado atual — encerramento (9 jun/2026)

Sessão encerrada. Demo Docker **parada** (`.\demo-stop.ps1`). Ao retomar: `.\demo-start.ps1`.

### Entregue nesta sessão (após commit `d2f56c6`)

| Área | Status |
|------|--------|
| **6.1** Colegiados pedagógicos (API V26, aba Coordenação) | OK |
| **6.2** Folha de frequência bimestral (API V27, professor → Folha do bimestre) | OK |
| **6.4** Reorganização Direção × Secretaria (rotas, menus, permissões) | OK |
| Fix build backend (`ArrayList` em `AcademicoService`) | OK |
| Testes frontend (`routeAccess`, `navConfig`) | OK |
| Rebuild demo automático pós-alteração | OK |

### Entregue em sessões anteriores (commit `d2f56c6`)

| Área | Status |
|------|--------|
| Roadmap fases 1–5 (diário, convivência, matrícula nova, finanças, nutrição/patrimônio) | OK |
| Rematrícula online (secretaria + pais) | OK |
| Submenus Secretaria (Acadêmico, Horários, Comunicação, Cadastro) | OK |
| Matriz / normativa (preserva matrizes existentes) | OK |
| Seeds demo (turmas 1A/2A/3A, professores extras) | OK |
| Perfil com menu da área visitada | OK |
| Pais cadastram filhos | OK |
| Generalização `escola-demo` (web + portable) | OK |
| Build portable em `dist-portable\SGE` | OK (rebuild quando necessário) |

### Como rodar ao voltar

| Modo | Comando | URL |
|------|---------|-----|
| Web (demo) | `.\demo-start.ps1` | http://localhost:5173 |
| Parar web | `.\demo-stop.ps1` | — |
| Portable | `dist-portable\SGE\Iniciar SGE.vbs` | localhost (janela do launcher) |
| Rebuild portable | `.\scripts\build-portable.ps1` | — |

Senha de todas as contas demo: `admin123`.

**Teste rápido da hierarquia:**

- `diretor@sge.com` → **Direção** → Financeiro, Matriz, Usuários em `/direcao/*`
- `admin@sge.com` → **Admin (donos)** → Secretaria + Direção + Coord.
- `secretaria@sge.com` → **Secretaria** → Matrícula nova, Comunicação em `/secretaria/*` (sem financeiro)

### Git

- Último commit: `d2f56c6` — *feat: roadmap comercial fases 1-5…*
- **Working tree com alterações não commitadas** (Fase 6 + Direção/Secretaria). Ao retomar: revisar `git status`, testar demo, commitar quando desejado.
- Branch: `school/cem-monnerat`

### Restaurar escola real (CEM)

Ver `schools/_private/CEM_RESTAURACAO.md` — pacote e SQL documentados; não entra no JAR portable.
