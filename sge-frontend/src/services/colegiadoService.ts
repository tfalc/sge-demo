import { api } from "./api";

export type StatusReuniaoColegiado = "AGENDADA" | "REALIZADA" | "CANCELADA";
export type StatusEncaminhamentoColegiado = "PENDENTE" | "CONCLUIDO" | "CANCELADO";

export interface ReuniaoColegiadoResumo {
  id: string;
  titulo: string;
  tipo: string;
  dataReuniao: string;
  horaReuniao: string | null;
  status: StatusReuniaoColegiado;
  turmaId?: string;
  turmaNome?: string;
  serieNome?: string;
}

export interface ParticipanteColegiado {
  id: string;
  usuarioId: string | null;
  nomeExibicao: string;
  perfil: string | null;
}

export interface EncaminhamentoColegiado {
  id: string;
  reuniaoId: string;
  descricao: string;
  responsavelUsuarioId: string | null;
  responsavelNome: string | null;
  prazo: string | null;
  status: StatusEncaminhamentoColegiado;
  criadoEm: string;
  concluidoEm: string | null;
  turmaId?: string;
}

export interface ReuniaoColegiadoDetalhe extends ReuniaoColegiadoResumo {
  pauta: string | null;
  ataTexto: string | null;
  criadoEm: string;
  concluidaEm: string | null;
  participantes: ParticipanteColegiado[];
  encaminhamentos: EncaminhamentoColegiado[];
}

export interface PainelColegiadoDados {
  turmaId?: string;
  turmaNome?: string;
  mediaTurma?: number;
  mensagem?: string;
  alunosEmRiscoNota: { alunoId: string; alunoNome: string; mediaGeral: number }[];
  alunosEmRiscoFrequencia: { alunoId: string; alunoNome: string; percentual: number }[];
  ocorrenciasRecentes: {
    id: string;
    alunoNome: string;
    tipo: string;
    descricao: string;
    dataOcorrencia: string;
  }[];
  encaminhamentosPendentes: EncaminhamentoColegiado[];
}

export async function listarReunioesColegiado(turmaId?: string): Promise<ReuniaoColegiadoResumo[]> {
  const response = await api.get("/api/colegiados/reunioes", {
    params: turmaId ? { turmaId } : undefined,
  });
  return response.data.data as ReuniaoColegiadoResumo[];
}

export async function obterReuniaoColegiado(id: string): Promise<ReuniaoColegiadoDetalhe> {
  const response = await api.get(`/api/colegiados/reunioes/${id}`);
  return response.data.data as ReuniaoColegiadoDetalhe;
}

export async function obterPainelColegiado(id: string): Promise<PainelColegiadoDados> {
  const response = await api.get(`/api/colegiados/reunioes/${id}/painel-dados`);
  return response.data.data as PainelColegiadoDados;
}

export async function criarReuniaoColegiado(payload: {
  titulo: string;
  tipo?: string;
  turmaId?: string;
  dataReuniao: string;
  horaReuniao?: string;
  pauta?: string;
  participanteUsuarioIds?: string[];
}): Promise<ReuniaoColegiadoDetalhe> {
  const response = await api.post("/api/colegiados/reunioes", payload);
  return response.data.data as ReuniaoColegiadoDetalhe;
}

export async function atualizarReuniaoColegiado(
  id: string,
  payload: Partial<{
    titulo: string;
    pauta: string;
    ataTexto: string;
    status: StatusReuniaoColegiado;
  }>,
): Promise<ReuniaoColegiadoDetalhe> {
  const response = await api.put(`/api/colegiados/reunioes/${id}`, payload);
  return response.data.data as ReuniaoColegiadoDetalhe;
}

export async function concluirReuniaoColegiado(
  id: string,
  ataTexto?: string,
): Promise<ReuniaoColegiadoDetalhe> {
  const response = await api.post(`/api/colegiados/reunioes/${id}/concluir`, { ataTexto });
  return response.data.data as ReuniaoColegiadoDetalhe;
}

export async function criarEncaminhamentoColegiado(
  reuniaoId: string,
  payload: {
    descricao: string;
    responsavelUsuarioId?: string;
    responsavelNome?: string;
    prazo?: string;
  },
): Promise<EncaminhamentoColegiado> {
  const response = await api.post(`/api/colegiados/reunioes/${reuniaoId}/encaminhamentos`, payload);
  return response.data.data as EncaminhamentoColegiado;
}

export async function atualizarEncaminhamentoColegiado(
  id: string,
  status: StatusEncaminhamentoColegiado,
): Promise<EncaminhamentoColegiado> {
  const response = await api.put(`/api/colegiados/encaminhamentos/${id}`, { status });
  return response.data.data as EncaminhamentoColegiado;
}

export async function listarEncaminhamentosPendentes(
  turmaId?: string,
): Promise<EncaminhamentoColegiado[]> {
  const response = await api.get("/api/colegiados/encaminhamentos/pendentes", {
    params: turmaId ? { turmaId } : undefined,
  });
  return response.data.data as EncaminhamentoColegiado[];
}
