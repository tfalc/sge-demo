import { api } from "./api";
import type { OcorrenciaDisciplinar, TipoOcorrencia } from "../types";

export async function registrarOcorrencia(payload: {
  alunoId: string;
  turmaDisciplinaProfessorId: string;
  dataOcorrencia: string;
  tipo: TipoOcorrencia;
  descricao: string;
}): Promise<OcorrenciaDisciplinar> {
  const response = await api.post("/api/ocorrencias", payload);
  return response.data.data as OcorrenciaDisciplinar;
}

export async function listarOcorrencias(turmaDisciplinaProfessorId: string): Promise<OcorrenciaDisciplinar[]> {
  const response = await api.get("/api/ocorrencias", {
    params: { turmaDisciplinaProfessorId },
  });
  return response.data.data as OcorrenciaDisciplinar[];
}

export async function listarOcorrenciasTurma(turmaId: string): Promise<OcorrenciaDisciplinar[]> {
  const response = await api.get(`/api/ocorrencias/turma/${turmaId}`);
  return response.data.data as OcorrenciaDisciplinar[];
}

export async function marcarOcorrenciaVista(id: string): Promise<void> {
  await api.put(`/api/ocorrencias/${id}/vista`);
}
