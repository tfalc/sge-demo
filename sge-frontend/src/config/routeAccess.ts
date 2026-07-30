import { defaultAreasForPerfil } from "./navConfig";

/** Prefixos de rota → área do menu superior. */
const PATH_AREA_RULES: { prefix: string; area: string }[] = [
  { prefix: "/pais/", area: "pais" },
  { prefix: "/aluno/", area: "aluno" },
  { prefix: "/professor", area: "professor" },
  { prefix: "/secretaria/", area: "secretaria" },
  { prefix: "/direcao", area: "direcao" },
  { prefix: "/coordenacao", area: "coordenacao" },
  { prefix: "/nutricao", area: "nutricao" },
  { prefix: "/psicologia", area: "psicologia" },
];

const UNIVERSAL_PATHS = ["/perfil"];

/** Rotas estrategicas retiradas do menu operacional da secretaria. */
const SECRETARIA_RESTRICTED_PREFIXES = [
  "/secretaria/financeiro",
  "/secretaria/matriz",
  "/secretaria/rematricula",
  "/secretaria/patrimonio",
  "/secretaria/cadastro/usuarios",
];

/** Página de administração — ADMIN sempre acessa (anti lock-out). */
const ADMIN_ALWAYS_PATHS = ["/admin"];

export function areaForPath(pathname: string): string | null {
  if (pathname === "/admin" || pathname.startsWith("/admin/")) {
    return "admin";
  }
  for (const { prefix, area } of PATH_AREA_RULES) {
    if (pathname === prefix || pathname.startsWith(prefix) || pathname === prefix.replace(/\/$/, "")) {
      return area;
    }
  }
  // /direcao sem barra final
  if (pathname === "/direcao") return "direcao";
  if (pathname === "/coordenacao") return "coordenacao";
  if (pathname === "/nutricao") return "nutricao";
  if (pathname === "/psicologia") return "psicologia";
  if (pathname === "/professor" || pathname.startsWith("/professor/")) return "professor";
  return null;
}

export function canAccessPath(
  pathname: string,
  perfil: string | null,
  areasMenu?: string[] | null,
): boolean {
  if (!perfil) {
    return false;
  }
  if (UNIVERSAL_PATHS.some((p) => pathname === p || pathname.startsWith(`${p}/`))) {
    return true;
  }
  if (perfil === "ADMIN" && ADMIN_ALWAYS_PATHS.some((p) => pathname === p || pathname.startsWith(`${p}/`))) {
    return true;
  }

  if (perfil === "SECRETARIA") {
    if (SECRETARIA_RESTRICTED_PREFIXES.some((p) => pathname === p || pathname.startsWith(`${p}/`))) {
      return false;
    }
  }

  if (perfil === "DIRETOR" && pathname.startsWith("/secretaria/")) {
    return false;
  }

  const area = areaForPath(pathname);
  if (area === "admin") {
    return perfil === "ADMIN";
  }
  if (!area) {
    // Rotas sem área mapeada: só ADMIN (legado)
    return perfil === "ADMIN";
  }

  const areas = areasMenu && areasMenu.length > 0 ? areasMenu : defaultAreasForPerfil(perfil);
  return areas.map((a) => a.toLowerCase()).includes(area);
}

export function defaultHomeForPerfil(perfil: string, areasMenu?: string[] | null): string {
  const areas = areasMenu && areasMenu.length > 0 ? areasMenu : defaultAreasForPerfil(perfil);
  const preferred: Record<string, string> = {
    PAI: "/pais/hoje",
    ALUNO: "/aluno/hoje",
    PROFESSOR: "/professor/hoje",
    NUTRICIONISTA: "/nutricao",
    PSICOLOGA: "/psicologia",
    COORDENADOR: "/coordenacao",
    DIRETOR: "/direcao/hoje",
    ADMIN: "/secretaria/hoje",
    SECRETARIA: "/secretaria/hoje",
  };
  const home = preferred[perfil];
  if (home) {
    const area = areaForPath(home);
    if (area && areas.map((a) => a.toLowerCase()).includes(area)) {
      return home;
    }
  }
  // Primeira área habilitada
  const first = areas[0];
  const byArea: Record<string, string> = {
    pais: "/pais/hoje",
    aluno: "/aluno/hoje",
    professor: "/professor/hoje",
    secretaria: "/secretaria/hoje",
    direcao: "/direcao/hoje",
    coordenacao: "/coordenacao",
    nutricao: "/nutricao",
    psicologia: "/psicologia",
  };
  if (first && byArea[first]) return byArea[first];
  return "/perfil";
}
