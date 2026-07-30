import { api } from "./api";

export interface Notificacao {
  id: string;
  tipo: string;
  titulo: string;
  mensagem: string;
  link: string | null;
  referenciaId: string | null;
  lida: boolean;
  criadoEm: string;
}

interface ApiEnvelope<T> {
  data: T;
}

export async function listarNotificacoes(): Promise<Notificacao[]> {
  const { data } = await api.get<ApiEnvelope<Notificacao[]>>("/api/notificacoes");
  return data.data;
}

export async function resumoNotificacoes(): Promise<{ naoLidas: number }> {
  const { data } = await api.get<ApiEnvelope<{ naoLidas: number }>>("/api/notificacoes/resumo");
  return data.data;
}

export async function marcarNotificacaoLida(id: string): Promise<Notificacao> {
  const { data } = await api.post<ApiEnvelope<Notificacao>>(`/api/notificacoes/${id}/lida`);
  return data.data;
}

export async function marcarTodasNotificacoesLidas(): Promise<number> {
  const { data } = await api.post<ApiEnvelope<{ atualizadas: number }>>(
    "/api/notificacoes/marcar-todas-lidas",
  );
  return data.data.atualizadas;
}
