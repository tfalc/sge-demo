import { api } from "./api";
import type { AlunoCadastro, EscolaCadastro, ResponsavelCadastro } from "../types";

export async function getEscola(): Promise<EscolaCadastro> {
  const response = await api.get("/api/cadastro/escola");
  return response.data.data as EscolaCadastro;
}

export async function atualizarEscola(payload: {
  nome: string;
  cnpj?: string;
  notaMinimaAprovacao: number;
  frequenciaMinima: number;
}): Promise<void> {
  await api.put("/api/cadastro/escola", payload);
}

export async function listarAlunos(): Promise<AlunoCadastro[]> {
  const response = await api.get("/api/cadastro/alunos");
  return response.data.data as AlunoCadastro[];
}

export async function listarResponsaveis(): Promise<ResponsavelCadastro[]> {
  const response = await api.get("/api/cadastro/responsaveis");
  return response.data.data as ResponsavelCadastro[];
}

export async function criarResponsavel(payload: {
  nome: string;
  email: string;
  grauParentesco?: string;
  alunoId?: string;
  senha?: string;
}): Promise<void> {
  await api.post("/api/cadastro/responsaveis", payload);
}

export async function vincularResponsavel(alunoId: string, responsavelId: string): Promise<void> {
  await api.post(`/api/cadastro/alunos/${alunoId}/responsaveis`, { responsavelId });
}

export async function desvincularResponsavel(alunoId: string, responsavelId: string): Promise<void> {
  await api.delete(`/api/cadastro/alunos/${alunoId}/responsaveis/${responsavelId}`);
}

export async function criarAluno(payload: {
  nome: string;
  matricula: string;
  turmaId: string;
}): Promise<void> {
  await api.post("/api/cadastro/alunos", payload);
}

export async function cadastrarMeuFilho(payload: {
  nome: string;
  matricula: string;
  turmaId: string;
}): Promise<void> {
  await api.post("/api/cadastro/meus-filhos", payload);
}
