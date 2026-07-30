import { api } from "./api";
import type { AgendaEvent, CardapioItem, Comunicado, TipoEvento, TipoRefeicao } from "../types";

export async function getComunicados(params?: {
  audiencia?: string;
  turmaId?: string;
}): Promise<Comunicado[]> {
  const response = await api.get("/api/comunicados", { params });
  return response.data.data as Comunicado[];
}

export async function createComunicado(payload: {
  titulo: string;
  conteudo: string;
  visivelPara: string;
  turmaId?: string | null;
}): Promise<void> {
  await api.post("/api/comunicados", payload);
}

export async function getCardapio(data?: string): Promise<CardapioItem[]> {
  const response = await api.get("/api/cardapio", { params: data ? { data } : undefined });
  return response.data.data as CardapioItem[];
}

export async function createCardapio(payload: {
  dataRefeicao: string;
  tipoRefeicao: TipoRefeicao;
  descricao: string;
  calorias?: number;
}): Promise<void> {
  await api.post("/api/cardapio", payload);
}

export async function getAgenda(params: {
  inicio: string;
  fim: string;
  turmaId?: string;
}): Promise<AgendaEvent[]> {
  const response = await api.get("/api/agenda", { params });
  return response.data.data as AgendaEvent[];
}

export async function updateComunicado(
  id: string,
  payload: { titulo: string; conteudo: string; visivelPara: string; turmaId?: string | null },
): Promise<void> {
  await api.put(`/api/comunicados/${id}`, payload);
}

export async function deleteComunicado(id: string): Promise<void> {
  await api.delete(`/api/comunicados/${id}`);
}

export async function deleteCardapioItem(id: string): Promise<void> {
  await api.delete(`/api/cardapio/${id}`);
}

export async function updateAgendaEvent(
  id: string,
  payload: {
    titulo: string;
    descricao?: string;
    dataInicio: string;
    dataFim?: string;
    tipo?: TipoEvento;
    turmaId?: string | null;
  },
): Promise<void> {
  await api.put(`/api/agenda/${id}`, payload);
}

export async function deleteAgendaEvent(id: string): Promise<void> {
  await api.delete(`/api/agenda/${id}`);
}

export async function createAgendaEvent(payload: {
  titulo: string;
  descricao?: string;
  dataInicio: string;
  dataFim?: string;
  tipo?: TipoEvento;
  turmaId?: string | null;
}): Promise<void> {
  await api.post("/api/agenda", payload);
}
