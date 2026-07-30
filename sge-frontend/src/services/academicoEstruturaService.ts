import { api } from "./api";
import type { DisciplinaCadastro, ProfessorCadastro, SerieCadastro } from "../types";

export async function listarDisciplinas(): Promise<DisciplinaCadastro[]> {
  const response = await api.get("/api/academico/disciplinas");
  return response.data.data as DisciplinaCadastro[];
}

export async function criarDisciplina(payload: { nome: string; codigo?: string }): Promise<void> {
  await api.post("/api/academico/disciplinas", payload);
}

export async function listarProfessores(): Promise<ProfessorCadastro[]> {
  const response = await api.get("/api/academico/professores");
  return response.data.data as ProfessorCadastro[];
}

export async function criarProfessor(payload: {
  nome: string;
  email: string;
  registroMec?: string;
  senha?: string;
}): Promise<void> {
  await api.post("/api/academico/professores", payload);
}

export async function listarSeries(): Promise<SerieCadastro[]> {
  const response = await api.get("/api/academico/series");
  return response.data.data as SerieCadastro[];
}

export async function criarTurma(payload: {
  nome: string;
  serieId: string;
  capacidadeMax?: number;
}): Promise<void> {
  await api.post("/api/academico/turmas", payload);
}

export async function atualizarDisciplina(id: string, payload: { nome: string; codigo?: string }): Promise<void> {
  await api.put(`/api/academico/disciplinas/${id}`, payload);
}

export async function excluirDisciplina(id: string): Promise<void> {
  await api.delete(`/api/academico/disciplinas/${id}`);
}

export async function atualizarTurma(id: string, payload: { nome: string; serieId: string }): Promise<void> {
  await api.put(`/api/academico/turmas/${id}`, payload);
}

export async function excluirTurma(id: string): Promise<void> {
  await api.delete(`/api/academico/turmas/${id}`);
}

export async function excluirVinculo(id: string): Promise<void> {
  await api.delete(`/api/academico/vinculos/${id}`);
}

export async function vincularDisciplinaTurma(
  turmaId: string,
  payload: { disciplinaId: string; professorId: string },
): Promise<void> {
  await api.post(`/api/academico/turmas/${turmaId}/vinculos`, payload);
}
