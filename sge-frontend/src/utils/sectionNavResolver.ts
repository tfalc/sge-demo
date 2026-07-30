import type { SectionNavItem } from "../components/layout/SectionNav";
import { gestaoBaseFromPathname } from "../pages/secretaria/gestaoArea";
import {
  academicoGestaoNav,
  cadastroGestaoNav,
  comunicacaoGestaoNav,
  horariosGestaoNav,
  primaryGestaoNav,
} from "../pages/secretaria/gestaoNav";
import { alunoNav } from "../pages/aluno/alunoNav";
import { parentNav } from "../pages/pais/parentNav";
import { professorNav } from "../pages/professor/professorNav";

export const LAST_SECTION_PATH_KEY = "sge:lastSectionPath";

const GESTAO_SECONDARY_PREFIXES = ["/academico", "/horarios", "/comunicacao", "/cadastro"] as const;

export function persistSectionPath(pathname: string) {
  if (pathname === "/login" || pathname === "/perfil") return;
  if (
    pathname.startsWith("/secretaria/") ||
    pathname.startsWith("/direcao/") ||
    pathname === "/direcao" ||
    pathname.startsWith("/admin/") ||
    pathname.startsWith("/pais/") ||
    pathname.startsWith("/aluno/") ||
    pathname.startsWith("/professor/")
  ) {
    sessionStorage.setItem(LAST_SECTION_PATH_KEY, pathname);
  }
}

export function readLastSectionPath(): string {
  return sessionStorage.getItem(LAST_SECTION_PATH_KEY) ?? "";
}

function gestaoPrimaryNav(path: string): SectionNavItem[] | null {
  if (path === "/direcao" || path.startsWith("/direcao/")) {
    return primaryGestaoNav("/direcao");
  }
  if (path.startsWith("/secretaria/")) {
    return primaryGestaoNav("/secretaria");
  }
  return null;
}

function gestaoSecondaryNav(path: string): SectionNavItem[] | null {
  const base = gestaoBaseFromPathname(path);
  if (!path.startsWith(`${base}/`)) {
    return null;
  }
  for (const suffix of GESTAO_SECONDARY_PREFIXES) {
    const prefix = `${base}${suffix}`;
    if (path === prefix || path.startsWith(`${prefix}/`)) {
      if (suffix === "/academico") return academicoGestaoNav(base);
      if (suffix === "/horarios") return horariosGestaoNav(base);
      if (suffix === "/comunicacao") return comunicacaoGestaoNav(base);
      if (suffix === "/cadastro") return cadastroGestaoNav(base);
    }
  }
  return null;
}

function primaryNavForContext(lastPath: string, perfil: string | null): SectionNavItem[] | null {
  if (lastPath.startsWith("/admin/")) {
    return [
      { to: "/admin/acessos", label: "Acessos de menu" },
      { to: "/perfil", label: "Perfil" },
    ];
  }

  const gestao = gestaoPrimaryNav(lastPath);
  if (gestao) return gestao;

  if (lastPath.startsWith("/pais/")) return parentNav;
  if (lastPath.startsWith("/aluno/")) return alunoNav;
  if (lastPath.startsWith("/professor/")) return professorNav;

  if (perfil === "PAI") return parentNav;
  if (perfil === "ALUNO") return alunoNav;
  if (perfil === "PROFESSOR") return professorNav;
  if (perfil === "DIRETOR") return primaryGestaoNav("/direcao");
  if (perfil === "ADMIN" || perfil === "SECRETARIA") return primaryGestaoNav("/secretaria");
  return null;
}

function secondaryNavForPath(lastPath: string): SectionNavItem[] | null {
  return gestaoSecondaryNav(lastPath);
}

export function resolveSectionNavs(
  perfil: string | null,
  lastPath?: string,
): { primary: SectionNavItem[] | null; secondary: SectionNavItem[] | null } {
  const contextPath = lastPath || readLastSectionPath();
  return {
    primary: primaryNavForContext(contextPath, perfil),
    secondary: secondaryNavForPath(contextPath),
  };
}
