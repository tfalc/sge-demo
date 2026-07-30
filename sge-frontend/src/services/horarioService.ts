import { api } from "./api";
import type { HorarioAula } from "../types";

export async function getHorariosTurma(turmaId: string): Promise<HorarioAula[]> {
  const response = await api.get("/api/horarios", { params: { turmaId } });
  return response.data.data as HorarioAula[];
}

export async function getHorariosProfessor(professorId: string): Promise<HorarioAula[]> {
  const response = await api.get("/api/horarios/professor", { params: { professorId } });
  return response.data.data as HorarioAula[];
}

export async function criarHorario(payload: {
  turmaId: string;
  diaSemana: number;
  horaInicio: string;
  horaFim: string;
  disciplinaId: string;
  professorId?: string | null;
}): Promise<HorarioAula> {
  const body: Record<string, unknown> = { ...payload };
  if (!body.professorId) {
    delete body.professorId;
  }
  const response = await api.post("/api/horarios", body);
  return response.data.data as HorarioAula;
}

export async function atualizarHorario(
  id: string,
  payload: {
    diaSemana: number;
    horaInicio: string;
    horaFim: string;
    disciplinaId: string;
    professorId?: string | null;
  },
): Promise<HorarioAula> {
  const response = await api.put(`/api/horarios/${id}`, payload);
  return response.data.data as HorarioAula;
}

export async function excluirHorario(id: string): Promise<void> {
  await api.delete(`/api/horarios/${id}`);
}

export async function listarHorariosTurmas(turmaIds: string[]): Promise<HorarioAula[]> {
  if (turmaIds.length === 0) return [];
  const lists = await Promise.all(turmaIds.map((id) => getHorariosTurma(id)));
  return lists.flat();
}
