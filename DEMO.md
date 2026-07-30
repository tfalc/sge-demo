# Demo do SGE — ambiente generico

Apresentacao de **prototipo funcional** do SGE. Nao e o sistema final nem prova de producao.

**Tempo sugerido:** 12–14 minutos + perguntas.

> Para demo com dados de escola real (CEM Monnerat), restaurar conforme [`schools/_private/CEM_RESTAURACAO.md`](schools/_private/CEM_RESTAURACAO.md).

---

## Como abrir a reuniao (30 s)

> *"O que voces vao ver e um MVP de gestao escolar digital — financeiro, academico, comunicacao e coordenacao. O que esta pronto hoje ja funciona de ponta a ponta; o que surgir da conversa entra no roadmap."*

## Como fechar (30 s)

> *"A proposta e substituir processos manuais por um unico sistema, comecando pelo que mais impacta familia e secretaria."*

---

## Subir o ambiente

```powershell
.\demo-start.ps1
```

| URL | Uso |
|-----|-----|
| http://localhost:5173 | App |
| http://localhost:8080/actuator/health | Health |

**Senha de todos os perfis:** `admin123`

---

## Roteiro

### 1. Familia — PIX e rematricula (~4 min) · `pai@sge.com`

| # | Tela | O que dizer |
|---|------|-------------|
| 1 | Login (**SGE — Gestao Escolar**) | Portal com identidade da escola |
| 2 | Cobrancas — Joao e Ana Silva | Dois filhos, duas mensalidades |
| 3 | Pagar PIX → simular pagamento | Confirmacao automatica |
| 4 | Rematricula — preencher → Validar → Enviar | Formulario digital |

### 2. Secretaria (~4 min) · `secretaria@sge.com`

| # | Tela | O que dizer |
|---|------|-------------|
| 1 | Matrícula nova | Fluxo operacional do dia a dia |
| 2 | Comunicação | Comunicados e agenda |
| 3 | Cadastro — Alunos / Responsáveis | Manutenção cadastral |
| 4 | Acadêmico / Horários | Turmas e grade |

### 2b. Admin / Direção (opcional, ~2 min) · `admin@sge.com` ou `diretor@sge.com`

| # | Tela | O que dizer |
|---|------|-------------|
| 1 | Financeiro (`/direcao/financeiro`) | Visão estratégica — inadimplência |
| 2 | Matriz / rematrícula | Conformidade e períodos |

### 3. Professor (~2 min) · `prof@sge.com`

Turma **3A** — notas e frequencia.

### 4. Coordenacao (~2 min) · `coord@sge.com`

Analise de **Ana Silva** — alerta de risco pedagogico.

---

## Perfis de teste

| Perfil | E-mail |
|--------|--------|
| Admin (donos) | `admin@sge.com` |
| Secretaria | `secretaria@sge.com` |
| Responsavel | `pai@sge.com` |
| Professor | `prof@sge.com`, `prof.carla@sge.com`, `prof.paulo@sge.com` |
| Coordenacao | `coord@sge.com` |
| Aluno | `aluno@sge.com` |
| Direcao | `diretor@sge.com` |

---

## Checklist antes da demo

- [ ] `.\demo-start.ps1` concluiu
- [ ] Login mostra **SGE — Gestao Escolar** / Escola Modelo Demo
- [ ] Rematricula publicada (admin → Rematrícula → visivel → Salvar)

## Parar

```powershell
.\demo-stop.ps1
```
