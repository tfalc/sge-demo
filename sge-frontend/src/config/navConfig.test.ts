import { describe, expect, it } from "vitest";
import { navItemsForPerfil, DEFAULT_AREAS_BY_PERFIL } from "./navConfig";

describe("navItemsForPerfil", () => {
  it("retorna apenas portal dos pais para perfil PAI", () => {
    const items = navItemsForPerfil("PAI");
    expect(items).toHaveLength(1);
    expect(items[0]?.to).toBe("/pais/hoje");
  });

  it("retorna todas as areas escolares para ADMIN com defaults + aba Admin", () => {
    const items = navItemsForPerfil("ADMIN");
    expect(items.map((i) => i.area)).toEqual([...DEFAULT_AREAS_BY_PERFIL.ADMIN, "admin"]);
  });

  it("respeita areasMenu customizado e mantem Admin", () => {
    const items = navItemsForPerfil("ADMIN", ["secretaria", "pais"]);
    expect(items.map((i) => i.area)).toEqual(["pais", "secretaria", "admin"]);
  });

  it("retorna apenas secretaria para SECRETARIA", () => {
    const items = navItemsForPerfil("SECRETARIA");
    expect(items).toHaveLength(1);
    expect(items[0]?.to).toBe("/secretaria/hoje");
  });

  it("retorna portal do aluno para perfil ALUNO", () => {
    const items = navItemsForPerfil("ALUNO");
    expect(items).toHaveLength(1);
    expect(items[0]?.to).toBe("/aluno/hoje");
  });

  it("retorna menu completo quando perfil e desconhecido sem areas", () => {
    const items = navItemsForPerfil("DESCONHECIDO");
    expect(items.length).toBeGreaterThanOrEqual(5);
  });
});
