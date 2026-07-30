import { api } from "./api";
import type { AnaliseAluno, InadimplenciaEscola, TurmaDesempenho, TurmaFrequencia } from "../types";

export async function getTurmaDesempenho(turmaId: string): Promise<TurmaDesempenho> {
  const response = await api.get(`/api/relatorios/turma/${turmaId}/desempenho`);
  return response.data.data as TurmaDesempenho;
}

export async function getTurmaFrequencia(turmaId: string): Promise<TurmaFrequencia> {
  const response = await api.get(`/api/relatorios/turma/${turmaId}/frequencia`);
  return response.data.data as TurmaFrequencia;
}

export async function getAnaliseAluno(alunoId: string): Promise<AnaliseAluno> {
  const response = await api.get(`/api/relatorios/aluno/${alunoId}/analise-inteligente`);
  return response.data.data as AnaliseAluno;
}

export async function getInadimplenciaEscola(): Promise<InadimplenciaEscola> {
  const response = await api.get("/api/relatorios/escola/inadimplencia");
  return response.data.data as InadimplenciaEscola;
}

export async function downloadBoletimPdf(alunoId: string): Promise<void> {
  const response = await api.post(`/api/relatorios/boletim/${alunoId}/gerar-pdf`, null, {
    responseType: "blob",
  });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = `boletim-${alunoId}.pdf`;
  link.click();
  window.URL.revokeObjectURL(url);
}
