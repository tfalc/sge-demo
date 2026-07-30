import { api } from "./api";
import type {
  AtaAulaResumo,
  Boletim,
  DiarioNotas,
  DisciplinaVinculo,
  Frequencia,
  PeriodoAvaliacao,
  TipoNota,
  Turma,
  TurmaAluno,
} from "../types";

export async function getTurmas(professorId?: string): Promise<Turma[]> {
  const response = await api.get("/api/turmas", {
    params: professorId ? { professorId } : undefined,
  });
  return response.data.data as Turma[];
}

export async function getTurmaAlunos(turmaId: string): Promise<TurmaAluno[]> {
  const response = await api.get(`/api/turmas/${turmaId}/alunos`);
  return response.data.data as TurmaAluno[];
}

export async function getTurmaDisciplinas(
  turmaId: string,
  professorId?: string,
): Promise<DisciplinaVinculo[]> {
  const response = await api.get(`/api/turmas/${turmaId}/disciplinas`, {
    params: professorId ? { professorId } : undefined,
  });
  return response.data.data as DisciplinaVinculo[];
}

export async function getPeriodos(): Promise<PeriodoAvaliacao[]> {
  const response = await api.get("/api/periodos-avaliacao");
  return response.data.data as PeriodoAvaliacao[];
}

export async function lancarNota(payload: {
  alunoId: string;
  turmaDisciplinaProfessorId: string;
  periodoId: string;
  valor: number;
  tipo: TipoNota;
  observacao?: string;
}): Promise<void> {
  await api.post("/api/notas", payload);
}

export async function getDiarioNotas(turmaDisciplinaProfessorId: string): Promise<DiarioNotas> {
  const response = await api.get("/api/notas/diario", {
    params: { turmaDisciplinaProfessorId },
  });
  return response.data.data as DiarioNotas;
}

export async function lancarPresencas(payload: {
  turmaDisciplinaProfessorId: string;
  dataAula: string;
  presencas: { alunoId: string; presente: boolean; justificativa?: string }[];
}): Promise<void> {
  await api.post("/api/presencas/lancamento", payload);
}

export async function getPresencasDaAula(
  turmaDisciplinaProfessorId: string,
  dataAula: string,
): Promise<{ alunoId: string; alunoNome: string; presente: boolean; justificativa: string | null }[]> {
  const response = await api.get("/api/presencas", {
    params: { turmaDisciplinaProfessorId, dataAula },
  });
  return response.data.data;
}

export async function getBoletim(alunoId: string): Promise<Boletim> {
  const response = await api.get(`/api/alunos/${alunoId}/boletim`);
  return response.data.data as Boletim;
}

export async function getFrequencia(alunoId: string): Promise<Frequencia> {
  const response = await api.get(`/api/alunos/${alunoId}/frequencia`);
  return response.data.data as Frequencia;
}

export type AtaAula = {
  id: string;
  turmaDisciplinaProfessorId: string;
  dataAula: string;
  conteudo: string | null;
  tarefaCasa: string | null;
  observacoes: string | null;
  atualizadoEm?: string;
};

export async function getAtaAula(
  turmaDisciplinaProfessorId: string,
  dataAula: string,
): Promise<AtaAula | null> {
  const response = await api.get("/api/atas", {
    params: { turmaDisciplinaProfessorId, dataAula },
  });
  return (response.data.data as AtaAula | null) ?? null;
}

export async function salvarAtaAula(payload: {
  turmaDisciplinaProfessorId: string;
  dataAula: string;
  conteudo?: string;
  tarefaCasa?: string;
  observacoes?: string;
}): Promise<AtaAula> {
  const response = await api.post("/api/atas", payload);
  return response.data.data as AtaAula;
}

export async function getHistoricoAtas(
  turmaDisciplinaProfessorId: string,
  inicio?: string,
  fim?: string,
): Promise<AtaAulaResumo[]> {
  const response = await api.get("/api/atas/historico", {
    params: { turmaDisciplinaProfessorId, inicio, fim },
  });
  return response.data.data as AtaAulaResumo[];
}

export interface MatrizFrequenciaCabecalho {
  escolaNome: string;
  disciplinaNome: string;
  turmaNome: string;
  serieNome: string;
  professorNome: string | null;
  anoLetivo: number;
  periodoNome: string;
  periodoInicio: string | null;
  periodoFim: string | null;
  aulasDadas: number;
  aulasPrevistas: number | null;
  assinaturaEm: string | null;
}

export interface MatrizFrequenciaAluno {
  ordem: number;
  alunoId: string;
  alunoNome: string;
  matricula: string;
  presencasPorData: Record<string, { presente: boolean; justificativa: string | null }>;
  totalFaltas: number;
}

export interface MatrizFrequencia {
  cabecalho: MatrizFrequenciaCabecalho;
  datas: string[];
  alunos: MatrizFrequenciaAluno[];
}

export async function getMatrizPresencas(
  turmaDisciplinaProfessorId: string,
  periodoId: string,
): Promise<MatrizFrequencia> {
  const response = await api.get("/api/presencas/matriz", {
    params: { turmaDisciplinaProfessorId, periodoId },
  });
  return response.data.data as MatrizFrequencia;
}

export async function salvarMatrizPresencas(payload: {
  turmaDisciplinaProfessorId: string;
  periodoId: string;
  aulasPrevistas?: number;
  celulas: { alunoId: string; dataAula: string; presente: boolean; justificativa?: string }[];
}): Promise<MatrizFrequencia> {
  const response = await api.post("/api/presencas/matriz", payload);
  return response.data.data as MatrizFrequencia;
}

export async function assinarMatrizPresencas(
  turmaDisciplinaProfessorId: string,
  periodoId: string,
): Promise<MatrizFrequencia> {
  const response = await api.post("/api/presencas/matriz/assinar", null, {
    params: { turmaDisciplinaProfessorId, periodoId },
  });
  return response.data.data as MatrizFrequencia;
}
