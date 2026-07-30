import { api } from "./api";

export type SeveridadeRestricao = "LEVE" | "MODERADA" | "GRAVE";

export interface RestricaoAlimentar {
  id: string;
  alunoId: string;
  alunoNome: string;
  descricao: string;
  severidade: SeveridadeRestricao;
  criadoEm: string;
}

export async function listarRestricoes(alunoId?: string): Promise<RestricaoAlimentar[]> {
  const response = await api.get("/api/nutricao/restricoes", {
    params: alunoId ? { alunoId } : undefined,
  });
  return response.data.data as RestricaoAlimentar[];
}

export async function criarRestricao(payload: {
  alunoId: string;
  descricao: string;
  severidade?: SeveridadeRestricao;
}): Promise<void> {
  await api.post("/api/nutricao/restricoes", payload);
}

export async function excluirRestricao(id: string): Promise<void> {
  await api.delete(`/api/nutricao/restricoes/${id}`);
}
