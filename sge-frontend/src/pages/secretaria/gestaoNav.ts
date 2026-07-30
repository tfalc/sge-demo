import type { SectionNavItem } from "../../components/layout/SectionNav";
import type { GestaoBasePath } from "./gestaoArea";

type NavDef = { path: string; label: string; matchPrefix?: string; exact?: boolean };

function buildNav(base: GestaoBasePath, items: NavDef[]): SectionNavItem[] {
  return items.map(({ path, label, matchPrefix, exact }) => {
    const to = path.startsWith("/perfil") ? path : path ? `${base}${path}` : base;
    if (exact) {
      return { to, label };
    }
    return {
      to,
      label,
      matchPrefix: matchPrefix ? `${base}${matchPrefix}` : undefined,
    };
  });
}

const DIRECAO_PRIMARY: NavDef[] = [
  { path: "", label: "Painel", exact: true },
  { path: "/financeiro", label: "Financeiro" },
  { path: "/comunicacao/comunicados", label: "Comunicação", matchPrefix: "/comunicacao" },
  { path: "/academico/disciplinas", label: "Acadêmico", matchPrefix: "/academico" },
  { path: "/rematricula", label: "Rematrícula" },
  { path: "/matricula-nova", label: "Matrícula nova" },
  { path: "/patrimonio", label: "Patrimônio" },
  { path: "/galeria", label: "Galeria" },
  { path: "/matriz", label: "Matriz" },
  { path: "/horarios/grade", label: "Horários", matchPrefix: "/horarios" },
  { path: "/cadastro/escola", label: "Cadastro", matchPrefix: "/cadastro" },
  { path: "/hoje", label: "Hoje" },
  { path: "/perfil", label: "Perfil" },
];

const SECRETARIA_PRIMARY: NavDef[] = [
  { path: "/matricula-nova", label: "Matrícula nova" },
  { path: "/comunicacao/comunicados", label: "Comunicação", matchPrefix: "/comunicacao" },
  { path: "/academico/disciplinas", label: "Acadêmico", matchPrefix: "/academico" },
  { path: "/horarios/grade", label: "Horários", matchPrefix: "/horarios" },
  { path: "/cadastro/escola", label: "Cadastro", matchPrefix: "/cadastro" },
  { path: "/hoje", label: "Hoje" },
  { path: "/perfil", label: "Perfil" },
];

export function primaryGestaoNav(base: GestaoBasePath): SectionNavItem[] {
  return buildNav(base, base === "/direcao" ? DIRECAO_PRIMARY : SECRETARIA_PRIMARY);
}

export function academicoGestaoNav(base: GestaoBasePath): SectionNavItem[] {
  return buildNav(base, [
    { path: "/academico/disciplinas", label: "Disciplinas" },
    { path: "/academico/professores", label: "Professores" },
    { path: "/academico/turmas", label: "Turmas" },
    { path: "/academico/vinculos", label: "Vinculos" },
  ]);
}

export function cadastroGestaoNav(base: GestaoBasePath): SectionNavItem[] {
  const items: NavDef[] = [
    { path: "/cadastro/escola", label: "Escola" },
    { path: "/cadastro/alunos", label: "Alunos" },
    { path: "/cadastro/responsaveis", label: "Responsaveis" },
  ];
  if (base === "/direcao") {
    items.push({ path: "/cadastro/usuarios", label: "Usuarios" });
  }
  return buildNav(base, items);
}

export function comunicacaoGestaoNav(base: GestaoBasePath): SectionNavItem[] {
  return buildNav(base, [
    { path: "/comunicacao/comunicados", label: "Comunicados" },
    { path: "/comunicacao/eventos", label: "Eventos" },
    { path: "/comunicacao/calendario", label: "Calendario" },
  ]);
}

export function horariosGestaoNav(base: GestaoBasePath): SectionNavItem[] {
  return buildNav(base, [
    { path: "/horarios/grade", label: "Grade atual" },
    { path: "/horarios/turma", label: "Grade da turma" },
    { path: "/horarios/disciplina", label: "Por materia" },
    { path: "/horarios/professor", label: "Por professor" },
    { path: "/horarios/calendario", label: "Calendario" },
  ]);
}

export type GestaoModuloCard = { to: string; label: string; desc: string };

/** Atalhos do painel da Direcao (modulos estrategicos). */
export function direcaoModuloCards(): GestaoModuloCard[] {
  const base: GestaoBasePath = "/direcao";
  return [
    { to: `${base}/financeiro`, label: "Financeiro", desc: "Cobranças, inadimplência, planos e contratos" },
    { to: `${base}/comunicacao/comunicados`, label: "Comunicação", desc: "Comunicados, eventos e calendário escolar" },
    { to: `${base}/academico/disciplinas`, label: "Acadêmico", desc: "Disciplinas, professores, turmas e vínculos" },
    { to: `${base}/rematricula`, label: "Rematrícula", desc: "Período, formulário PDF e validação dos pais" },
    { to: `${base}/matricula-nova`, label: "Matrícula nova", desc: "Processos de ingresso com documentos (GED)" },
    { to: `${base}/patrimonio`, label: "Patrimônio", desc: "Inventário de bens e equipamentos" },
    { to: `${base}/galeria`, label: "Galeria", desc: "Álbuns de fotos para a comunidade escolar" },
    { to: `${base}/matriz`, label: "Matriz curricular", desc: "Normativa, matrizes e validação de turmas" },
    { to: `${base}/horarios/grade`, label: "Horários", desc: "Grade horária e calendário de aulas" },
    { to: `${base}/cadastro/escola`, label: "Cadastro", desc: "Escola, alunos, responsáveis e usuários" },
    { to: `${base}/hoje`, label: "Hoje", desc: "Pendências e o que precisa da sua atenção" },
  ];
}
