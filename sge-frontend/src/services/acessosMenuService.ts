import { api } from "./api";

export type AcessosMenuMatriz = {
  areas: string[];
  perfis: string[];
  acessos: Record<string, string[]>;
  defaults: Record<string, string[]>;
};

export async function getAcessosMenu(): Promise<AcessosMenuMatriz> {
  const response = await api.get("/api/admin/acessos-menu");
  return response.data.data as AcessosMenuMatriz;
}

export async function putAcessosMenu(
  acessos: Record<string, string[]>,
): Promise<AcessosMenuMatriz> {
  const response = await api.put("/api/admin/acessos-menu", { acessos });
  return response.data.data as AcessosMenuMatriz;
}

export async function restaurarAcessosMenuDefaults(): Promise<AcessosMenuMatriz> {
  const response = await api.post("/api/admin/acessos-menu/defaults");
  return response.data.data as AcessosMenuMatriz;
}
