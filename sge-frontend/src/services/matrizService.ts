import { api } from "./api";

type ApiEnvelope<T> = {
  data: T;
};

export type MatrizComponente = {
  id: string;
  componente: string;
  area: string;
  aulasSemanais: number;
  aulasSemanaisMin?: number | null;
  aulasSemanaisMax?: number | null;
  baseNacionalComum: boolean;
  ordem: number;
};

export type MatrizCurricular = {
  id: string;
  codigo: string;
  nome: string;
  etapa: string;
  modalidade: string;
  modoValidacao?: string;
  aulasSemanaisTotal: number;
  aulasSemanaisTotalMin?: number | null;
  aulasSemanaisTotalMax?: number | null;
  minutosAula: number;
  horasAnuaisMinimas?: number;
  normativaRef?: string;
  serieId?: string;
  serieNome?: string;
  componentes?: MatrizComponente[];
};

export type ValidacaoTurmaItem = {
  componente: string;
  area: string;
  aulasEsperadas: number;
  aulasMinimas?: number;
  aulasMaximas?: number;
  aulasNaGrade: number;
  vinculoDisciplina: boolean;
  disciplinaNome: string | null;
  conforme: boolean;
  observacao?: string | null;
};

export type ValidacaoTurma = {
  turmaId: string;
  turmaNome: string;
  serieNome: string;
  matrizNome: string;
  modoValidacao?: string;
  aulasSemanaisEsperadas: number;
  aulasSemanaisMinimas?: number | null;
  aulasSemanaisMaximas?: number | null;
  aulasSemanaisNaGrade: number;
  minutosAula: number;
  normativaRef?: string;
  conforme: boolean;
  itens: ValidacaoTurmaItem[];
};

export async function listarMatrizes(): Promise<MatrizCurricular[]> {
  const { data } = await api.get<ApiEnvelope<MatrizCurricular[]>>("/api/academico/matrizes");
  return data.data;
}

export async function obterMatriz(id: string): Promise<MatrizCurricular> {
  const { data } = await api.get<ApiEnvelope<MatrizCurricular>>(`/api/academico/matrizes/${id}`);
  return data.data;
}

export async function validarTurmaMatriz(turmaId: string, matrizId?: string): Promise<ValidacaoTurma> {
  const qs = matrizId ? `?matrizId=${encodeURIComponent(matrizId)}` : "";
  const { data } = await api.get<ApiEnvelope<ValidacaoTurma>>(
    `/api/academico/matrizes/validacao-turma/${turmaId}${qs}`,
  );
  return data.data;
}
