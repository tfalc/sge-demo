import { describe, expect, it } from "vitest";
import { canAccessPath, defaultHomeForPerfil } from "./routeAccess";

describe("routeAccess", () => {
  it("permite perfil para todos em /perfil", () => {
    expect(canAccessPath("/perfil", "PAI")).toBe(true);
    expect(canAccessPath("/perfil", "ALUNO")).toBe(true);
  });

  it("bloqueia aluno em rotas de secretaria", () => {
    expect(canAccessPath("/secretaria/financeiro", "ALUNO")).toBe(false);
  });

  it("permite admin em areas habilitadas por default", () => {
    expect(canAccessPath("/secretaria/financeiro", "ADMIN")).toBe(true);
    expect(canAccessPath("/pais/cobrancas", "ADMIN")).toBe(true);
  });

  it("bloqueia secretaria em rotas estrategicas da propria area", () => {
    expect(canAccessPath("/secretaria/financeiro", "SECRETARIA")).toBe(false);
    expect(canAccessPath("/secretaria/cadastro/usuarios", "SECRETARIA")).toBe(false);
  });

  it("bloqueia secretaria em direcao se area nao habilitada", () => {
    expect(canAccessPath("/direcao/financeiro", "SECRETARIA")).toBe(false);
  });

  it("permite diretor em gestao estrategica", () => {
    expect(canAccessPath("/direcao/financeiro", "DIRETOR")).toBe(true);
    expect(canAccessPath("/secretaria/financeiro", "DIRETOR")).toBe(false);
  });

  it("ADMIN sempre acessa pagina de Acessos em /admin", () => {
    expect(canAccessPath("/admin/acessos", "ADMIN", ["pais"])).toBe(true);
    expect(canAccessPath("/admin/acessos", "DIRETOR", ["direcao"])).toBe(false);
  });

  it("nao trata acessos como parte da Direcao", () => {
    expect(canAccessPath("/direcao/acessos", "ADMIN", ["direcao"])).toBe(true);
  });

  it("respeita areasMenu ao bloquear rota", () => {
    expect(canAccessPath("/pais/hoje", "ADMIN", ["secretaria"])).toBe(false);
    expect(canAccessPath("/secretaria/hoje", "ADMIN", ["secretaria"])).toBe(true);
  });

  it("redireciona pai para portal correto", () => {
    expect(defaultHomeForPerfil("PAI")).toBe("/pais/hoje");
  });

  it("redireciona professor e aluno para inbox Hoje", () => {
    expect(defaultHomeForPerfil("PROFESSOR")).toBe("/professor/hoje");
    expect(defaultHomeForPerfil("ALUNO")).toBe("/aluno/hoje");
  });
});
