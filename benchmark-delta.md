# Benchmark — Delta Gestão de Ensino × SGE

Documento de comparação para **entender diferenças**, não para duplicar o que o SGE já faz.

**Fonte Delta:** 40 prints em `evidências/` (5 painéis), extraídos dos vídeos oficiais do [Delta Gestão de Ensino](https://deltagestaodeensino.com.br/) ([Administrador](https://youtu.be/QjCakXis6vM), [Responsável](https://youtu.be/ID4GuKtE7jc), [Aluno](https://youtu.be/yl7Tm4ne1h8), [Coordenador](https://youtu.be/ekHOL0DsQsI), [Professor](https://youtu.be/Kiw2QEuIhdU)).

**Referência SGE:** código em `sge-frontend/` + `sge-backend/` (jun/2026).

> **Nota:** benchmark de mercado (Delta Gestao de Ensino). Referencias a sistema legado de cliente especifico foram removidas da distribuicao generica.

---

## Legenda

| Símbolo | Significado |
|---------|-------------|
| ✅ | SGE já tem equivalente funcional |
| 🟡 | Parcial — existe, mas com escopo menor ou UX diferente |
| ❌ | Gap — não existe no SGE |
| ➕ | Diferencial do SGE — Delta não destaca nos prints/vídeos |
| — | Não evidenciado nos prints deste painel |

**Regra de leitura:** linhas ✅ = **não priorizar** (já coberto). Foque backlog em ❌ e 🟡 relevantes.

---

## Mapa de painéis

| Delta (prints) | Perfil SGE | Rotas principais SGE |
|----------------|------------|----------------------|
| Administrador | `ADMIN` / `SECRETARIA` | `/secretaria/*` |
| Responsável | `PAI` | `/pais/*` |
| Aluno | `ALUNO` | `/aluno/*` |
| Coordenador | `COORDENADOR` | `/coordenacao` |
| Professor | `PROFESSOR` | `/professor` |
| — | `DIRETOR` | `/direcao` |
| — | `NUTRICIONISTA` | `/nutricao` |
| — | `PSICOLOGA` | `/psicologia` |

---

## 1. Administrador (18 prints)

Evidências: `evidências/administrador/` — home, menu, dados do colégio, configurações, coordenadores, professores, alunos, editar aluno, responsável, matrículas, boletim, salas, disciplinas, diário, relatórios.

| Funcionalidade (Delta) | SGE | Onde no SGE | Notas |
|------------------------|-----|-------------|-------|
| Home com atalhos | 🟡 | `/secretaria/financeiro` (entrada admin) | Delta usa tiles coloridos; SGE usa abas no topo |
| Menu lateral por módulo | 🟡 | `secretariaNav`, `App.tsx` | Navegação existe, layout diferente |
| Dados do colégio | ✅ | `/secretaria/cadastro` | Nome, CNPJ, nota/freq mínima |
| Configurações gerais | 🟡 | `/secretaria/cadastro` | SGE: regras acadêmicas; Delta: mais opções de customização |
| Cadastro coordenadores | ❌ | — | SGE tem perfil `COORDENADOR`, sem CRUD dedicado na UI |
| Cadastro professores | ✅ | `/secretaria/academico` | Criar professor + vínculo turma/disciplina |
| Cadastro alunos | ✅ | `/secretaria/cadastro` | Criar aluno, matrícula, turma |
| Editar aluno / responsável | 🟡 | `/secretaria/cadastro` | Vínculo responsável↔aluno; sem tela rica de edição de ficha |
| Matrículas (fluxo formal) | ❌ | — | SGE: cadastro simples; Delta: módulo de matrícula com documentos |
| Boletim (visão admin) | 🟡 | API `GET /api/alunos/{id}/boletim` | Sem tela admin dedicada; pais/professor usam boletim |
| Salas / turmas | ✅ | `/secretaria/academico` | Turmas, séries, vínculos; Delta chama “salas” |
| Disciplinas | ✅ | `/secretaria/academico` | CRUD + edição inline |
| Diário de classe (supervisão) | ❌ | — | Delta: visão admin do diário; SGE: só lançamento nota/freq |
| Relatórios administrativos | 🟡 | `/secretaria/financeiro`, APIs relatório | Inadimplência, mensal; sem biblioteca extensa de PDFs |
| Financeiro / cobranças | ✅ | `/secretaria/financeiro` | Cobranças, inadimplentes, planos, contratos |
| Comunicados / agenda | ✅ | `/secretaria/comunicacao` | CRUD comunicados + eventos + calendário |
| Contratos / documentos PDF | ❌ | — | SGE: contrato financeiro; sem geração de documentos escolares |
| Mala direta / e-mail em massa | ❌ | — | SGE: notificações in-app; FCM/e-mail pendente |
| Rematrícula online | ❌ | — | — |
| Gestão de usuários / permissões (UI) | 🟡 | `SecurityConfig`, `@PreAuthorize` | Regras na API; sem painel para criar/editar usuários |
| ➕ Módulo nutrição | ➕ | `/nutricao` | Cardápio por profissional dedicado |
| ➕ Módulo psicologia | ➕ | `/psicologia`, `/pais/saude` | Agenda + privacidade LGPD |
| ➕ Portal direção executivo | ➕ | `/direcao` | KPIs financeiro + acadêmico |

---

## 2. Responsável (5 prints)

Evidências: `evidências/responsavel/` — home, menu, perfil, boletim (selecionar aluno), boletim.

| Funcionalidade (Delta) | SGE | Onde no SGE | Notas |
|------------------------|-----|-------------|-------|
| Home com widgets (mensagens + calendário) | 🟡 | `/pais/comunicacao` | SGE separa em abas; Delta agrega na home |
| Menu: Perfil | ❌ | — | Sem tela de perfil editável |
| Menu: Boletim | ✅ | `/pais/desempenho` | Notas por bimestre + média |
| Selecionar filho (vários alunos) | ✅ | `/pais/desempenho` | Dropdown quando `me.filhos.length > 1` |
| Frequência | ✅ | `/pais/desempenho` | Percentual + detalhe por disciplina |
| Boletim PDF | ✅ | `downloadBoletimPdf` | `POST /api/relatorios/boletim/{id}/gerar-pdf` |
| Financeiro / PIX | ✅ | `/pais/cobrancas` | QR Code + simulação local |
| Comunicados | ✅ | `/pais/comunicacao` | Segmentado por turma/audiência |
| Calendário / agenda | ✅ | `/pais/comunicacao` | Lista + `AgendaCalendar` |
| Cardápio | ✅ | `/pais/comunicacao` | ➕ recurso que Delta não evidencia no painel responsável |
| Mensagens (caixa de entrada) | 🟡 | Comunicados | Leitura unidirecional; sem responder |
| Horários (grade de aulas) | ✅ | `/pais/horarios`, `/aluno/horarios`, `/secretaria/horarios`, professor | CRUD secretaria + leitura nos portais |
| Fotos / galeria | ❌ | — | — |
| Notificações | ✅ | `NotificationBell` | In-app; push FCM pendente |
| Saúde / atendimentos | ➕ | `/pais/saude` | Histórico filtrado (sem conteúdo privado) |
| Perfil editável (dados, senha) | ✅ | `/perfil` | `PUT /api/auth/perfil` e `/api/auth/senha` |
| Ocorrências disciplinares | ❌ | — | — |
| App mobile | 🟡 | PWA (`manifest.json`) | Delta: app nativo iOS/Android |

---

## 3. Aluno (2 prints)

Evidências: `evidências/aluno/` — home, home + menu.

Tiles na home: **Perfil**, **Boletim**, **Fotos**, **Horários**. Widgets: **Mensagens**, **Calendário**.

| Funcionalidade (Delta) | SGE | Onde no SGE | Notas |
|------------------------|-----|-------------|-------|
| Portal dedicado do aluno | ✅ | `/aluno/desempenho`, `/aluno/comunicacao`, `/aluno/horarios` | Login `aluno@sge.com` |
| Perfil `ALUNO` no auth | ✅ | `PerfilUsuario` | Vínculo `aluno.usuario_id` |
| Rota `/aluno` | ✅ | `router/index.tsx` | — |
| Boletim (próprio) | 🟡 | Via responsável | Dados existem na API; sem UI aluno |
| Frequência (própria) | 🟡 | Via responsável | `GET /api/alunos/{id}/frequencia` |
| Horários | ❌ | — | — |
| Fotos | ❌ | — | — |
| Mensagens | 🟡 | Comunicados (pais) | Sem caixa para aluno |
| Calendário | 🟡 | Agenda (pais) | Eventos acessíveis indiretamente |

**Interpretação:** o SGE trata o aluno como entidade cadastrada, não como usuário do portal. O responsável (e a secretaria) cobrem boletim e frequência.

---

## 4. Coordenador (5 prints)

Evidências: `evidências/coordenador/` — home, menu, perfil, salas, diário (avaliações).

| Funcionalidade (Delta) | SGE | Onde no SGE | Notas |
|------------------------|-----|-------------|-------|
| Home / menu | 🟡 | `/coordenacao` | Uma página; sem menu lateral rico |
| Perfil | ❌ | — | — |
| Salas (turmas) | ✅ | `/coordenacao` | Seletor de turma |
| Diário — avaliações | ❌ | — | Delta: supervisão do diário; SGE: gráficos agregados |
| Desempenho por turma | ✅ | `/coordenacao` | Gráficos Recharts |
| Frequência por turma | ✅ | `/coordenacao` | Barras + lista alunos em risco |
| Análise aluno em risco | ✅ | `/coordenacao` | `GET /api/relatorios/aluno/{id}/analise-ia` |
| Exportar relatório | ✅ | CSV na coordenação | Delta: mais formatos PDF |
| Ocorrências / supervisão docente | ❌ | — | — |
| ➕ IA pedagógica (Claude) | ➕ | `RelatorioController` | Fallback local sem API key |

---

## 5. Professor (10 prints)

Evidências: `evidências/professor/` — home, menu, perfil, disciplinas, diário, mensagens, horários, calendário, visualizar evento, fotos.

| Funcionalidade (Delta) | SGE | Onde no SGE | Notas |
|------------------------|-----|-------------|-------|
| Home (tiles + widgets) | ❌ | `/professor` | SGE: formulário direto; sem dashboard |
| Menu lateral | ❌ | — | Uma página com abas Notas / Frequência |
| Perfil | ❌ | — | — |
| Disciplinas / turmas | ✅ | `/professor` | Filtro por `professorId` |
| Lançar notas | ✅ | `/professor` | PROVA, TRABALHO, PARTICIPACAO |
| Lançar frequência | ✅ | `/professor` | Por data + turma/disciplina |
| Diário de classe | ❌ | — | Delta: conteúdo da aula, atividades, fórmulas de nota |
| Mensagens para pais/colegas | ❌ | — | — |
| Horários (grade) | ❌ | — | — |
| Calendário / eventos | 🟡 | — | Professor não tem tela; eventos na secretaria/pais |
| Fotos | ❌ | — | — |
| Registrar ocorrências | ❌ | — | — |

---

## Matriz resumida — o que **não** precisa ser “absorvido”

Funcionalidades em que o SGE **já está no mesmo patamar** (ou melhor) que o Delta evidenciado:

| Área | Status SGE |
|------|------------|
| Boletim digital + PDF | ✅ |
| Frequência (lançamento + consulta) | ✅ |
| Financeiro + cobranças + PIX (simulado) | ✅ |
| Comunicados segmentados | ✅ |
| Agenda / calendário escolar | ✅ |
| Notificações in-app | ✅ |
| Cadastro estrutural (turma, disciplina, professor, aluno, responsável) | ✅ |
| Configuração nota/freq mínima por escola | ✅ |
| Portal responsável (acadêmico + financeiro + comunicação) | ✅ |
| Coordenação com gráficos + export CSV | ✅ |
| Segurança por perfil na API | ✅ ➕ |
| Nutrição, psicologia, direção | ✅ ➕ |
| PWA | ✅ ➕ |

**Não abrir frentes novas** para reimplementar estes itens “no estilo Delta” — o ganho seria só cosmético (tiles, menu lateral).

---

## Gaps reais (Delta tem, SGE não)

Prioridade sugerida com base na recorrência nos 40 prints e no site Delta.

| P | Gap | Painéis afetados | Esforço estimado |
|---|-----|------------------|------------------|
| **P1** | Portal do aluno (`ALUNO` + `/aluno`) | Aluno | Médio — reutiliza APIs de boletim/freq/comunicados |
| **P1** | Grade de horários | Aluno, Professor, Responsável | Médio — novo modelo `horario_aula` + UI grade |
| **P1** | Diário de classe (conteúdo, avaliações, supervisão) | Professor, Coordenador, Admin | Alto — módulo novo além de nota/freq |
| **P2** | Mensagens bidirecionais | Professor, Responsável, Aluno | Médio — diferente de comunicados |
| **P2** | Galeria de fotos | Aluno, Professor, Responsável | Médio — MinIO já previsto no stack |
| **P2** | Perfil editável (dados + senha) | Todos | Baixo |
| **P2** | Ocorrências disciplinares | Professor, Coordenador, Responsável | Médio |
| **P3** | Matrículas + documentos PDF | Admin | Alto |
| **P3** | Rematrícula online | Admin, Responsável | Alto |
| **P3** | Relatórios administrativos extensos | Admin | Médio |
| **P3** | UI gestão de usuários/permissões | Admin | Médio |
| **P3** | Mala direta / e-mail em massa | Admin | Médio — depende SendGrid (já no roadmap) |

Itens do **roadmap SGE** que o Delta também pressupõe, mas ainda são 🟡 aqui:

| Item | Status SGE |
|------|------------|
| PIX real (OpenPix) | 🟡 simulado |
| Push FCM + e-mail | 🟡 só in-app |
| Análise IA (Claude) | 🟡 fallback local |

---

## Diferenciais do SGE (manter, não descartar)

Recursos que **não aparecem** nos prints Delta, mas já existem no SGE:

1. **Portal da direção** — visão executiva financeira + alertas acadêmicos (`/direcao`).
2. **Psicologia com privacidade** — anotações privadas; pais veem só status (`/psicologia`, `/pais/saude`).
3. **Nutricionista dedicada** — cardápio com calorias (`/nutricao`).
4. **Análise de risco com IA** — relatório por aluno para coordenação.
5. **API com autorização fina** — pai só acessa filhos/cobranças próprias (`SgeAuthorization`).
6. **Stack moderna** — React + Spring Boot + PWA vs PHP legado do Delta Gestão de Ensino.

---

## Índice das evidências (40 prints)

```
evidências/
├── administrador/   01-home … 18-relatórios
├── professor/       01-home … 10-fotos
├── responsavel/     01-home … 05-boletim
├── coordenador/     01-home … 05-diario-avaliacoes
└── aluno/           01-home, 02-home-menu
```

---

## Como usar este documento

1. **Planejamento:** implementar só linhas ❌ (e 🟡 quando o gap for funcional, não visual).
2. **Evitar retrabalho:** antes de cada feature, buscar na tabela “não precisa ser absorvido”.
3. **Validação:** ao fechar um gap, atualizar a coluna SGE (✅/🟡) e a data neste arquivo.

**Última revisão:** jun/2026 — alinhado ao código em `develop` após segurança por perfil, config escola e UI de edição.
