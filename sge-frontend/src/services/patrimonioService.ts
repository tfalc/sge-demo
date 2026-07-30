import { api } from "./api";

export type PatrimonioStatus = "ATIVO" | "MANUTENCAO" | "BAIXADO";

export interface PatrimonioItem {
  id: string;
  nome: string;
  categoria: string | null;
  localizacao: string | null;
  numeroPatrimonio: string | null;
  dataAquisicao: string | null;
  valorAquisicao: number | null;
  status: PatrimonioStatus;
  observacoes: string | null;
  criadoEm: string;
}

export async function listarPatrimonio(): Promise<PatrimonioItem[]> {
  const response = await api.get("/api/patrimonio/itens");
  return response.data.data as PatrimonioItem[];
}

export async function criarPatrimonioItem(payload: {
  nome: string;
  categoria?: string;
  localizacao?: string;
  numeroPatrimonio?: string;
  dataAquisicao?: string;
  valorAquisicao?: number;
  status?: PatrimonioStatus;
  observacoes?: string;
}): Promise<void> {
  await api.post("/api/patrimonio/itens", payload);
}

export async function atualizarPatrimonioItem(
  id: string,
  payload: Partial<{
    nome: string;
    categoria: string;
    localizacao: string;
    numeroPatrimonio: string;
    dataAquisicao: string;
    valorAquisicao: number;
    status: PatrimonioStatus;
    observacoes: string;
  }>,
): Promise<void> {
  await api.put(`/api/patrimonio/itens/${id}`, payload);
}

export async function excluirPatrimonioItem(id: string): Promise<void> {
  await api.delete(`/api/patrimonio/itens/${id}`);
}
