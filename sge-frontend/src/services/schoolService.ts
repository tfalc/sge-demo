import { api } from "./api";

export type SchoolBranding = {
  titulo_sistema?: string;
  subtitulo?: string;
  cor_primaria?: string;
  cor_destaque?: string;
};

export type SchoolConfig = {
  packageId: string;
  nome: string;
  nomeCurto?: string;
  siglaSge?: string;
  municipio?: string;
  uf?: string;
  normativa?: Record<string, string[]>;
  regrasAcademicas?: {
    minutos_aula_diurno?: number;
    frequencia_minima_percentual?: number;
    nota_minima_aprovacao?: number;
  };
  branding?: SchoolBranding;
};

type ApiEnvelope<T> = {
  data: T;
};

export async function getSchoolConfig(): Promise<SchoolConfig> {
  const { data } = await api.get<ApiEnvelope<SchoolConfig>>("/api/school/config");
  return data.data;
}

export type NormativaEscola = {
  packageId: string;
  fonte: string;
  consultadaEm?: string;
  avisoPreservacao?: string;
  normativa?: Record<string, string[]>;
  regrasAcademicas?: Record<string, number>;
  regrasFinanceiras?: Record<string, unknown>;
  resumo?: string[];
  matrizesPacote?: Array<{
    arquivo: string;
    codigo: string;
    nome: string;
    modoValidacao: string;
    normativaRef?: string;
  }>;
};

export type NormativaAlteracao = {
  area: string;
  campo: string;
  atual: unknown;
  novo: unknown;
};

export type NormativaPreview = {
  alteracoes: NormativaAlteracao[];
  temAlteracoes: boolean;
  packageId: string;
  consultadaEm?: string;
  preservacaoMatrizes?: string;
};

export async function getNormativa(): Promise<NormativaEscola> {
  const { data } = await api.get<ApiEnvelope<NormativaEscola>>("/api/school/normativa");
  return data.data;
}

export async function previewAplicarNormativa(): Promise<NormativaPreview> {
  const { data } = await api.get<ApiEnvelope<NormativaPreview>>("/api/school/normativa/preview-aplicar");
  return data.data;
}

export async function aplicarNormativa(): Promise<Record<string, unknown>> {
  const { data } = await api.post<ApiEnvelope<Record<string, unknown>>>("/api/school/normativa/aplicar");
  return data.data;
}
