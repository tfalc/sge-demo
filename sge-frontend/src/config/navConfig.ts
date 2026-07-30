export interface NavItem {
  to: string;
  label: string;
  matchPrefix?: string;
  /** Chave da área na matriz de acessos (perfil_acesso_area.area). */
  area: string;
}

/** Menus superiores disponíveis no sistema. */
export const ALL_NAV: NavItem[] = [
  { to: "/pais/hoje", label: "Pais", matchPrefix: "/pais/", area: "pais" },
  { to: "/aluno/hoje", label: "Aluno", matchPrefix: "/aluno/", area: "aluno" },
  { to: "/professor/hoje", label: "Professor", matchPrefix: "/professor/", area: "professor" },
  { to: "/secretaria/hoje", label: "Secretaria", matchPrefix: "/secretaria/", area: "secretaria" },
  { to: "/direcao/hoje", label: "Direção", matchPrefix: "/direcao", area: "direcao" },
  { to: "/coordenacao", label: "Coord.", area: "coordenacao" },
  { to: "/nutricao", label: "Nutrição", area: "nutricao" },
  { to: "/psicologia", label: "Psicologia", area: "psicologia" },
];

/** Console dos donos — sempre visível para ADMIN, fora da matriz escolar. */
export const ADMIN_NAV_ITEM: NavItem = {
  to: "/admin/acessos",
  label: "Admin",
  matchPrefix: "/admin",
  area: "admin",
};

export const MENU_AREAS = ALL_NAV.map((i) => i.area);

/** Defaults históricos (fallback se API/seed indisponível). */
export const DEFAULT_AREAS_BY_PERFIL: Record<string, string[]> = {
  PAI: ["pais"],
  ALUNO: ["aluno"],
  PROFESSOR: ["professor"],
  NUTRICIONISTA: ["nutricao"],
  PSICOLOGA: ["psicologia"],
  COORDENADOR: ["coordenacao"],
  DIRETOR: ["direcao", "coordenacao"],
  ADMIN: [...MENU_AREAS],
  SECRETARIA: ["secretaria"],
};

export function defaultAreasForPerfil(perfil: string | null): string[] {
  if (!perfil) return MENU_AREAS;
  return DEFAULT_AREAS_BY_PERFIL[perfil] ?? MENU_AREAS;
}

export function navItemsForPerfil(perfil: string | null, areasMenu?: string[] | null): NavItem[] {
  const areas = areasMenu && areasMenu.length > 0 ? areasMenu : defaultAreasForPerfil(perfil);
  const allowed = new Set(areas.map((a) => a.toLowerCase()));
  const items = ALL_NAV.filter((item) => allowed.has(item.area));
  // Admin (donos) sempre vê o console de administração, separado da Direção escolar
  if (perfil === "ADMIN") {
    return [...items, ADMIN_NAV_ITEM];
  }
  return items;
}
