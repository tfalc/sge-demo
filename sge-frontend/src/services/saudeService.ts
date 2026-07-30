import { api } from "./api";
import type { AgendamentoSaude } from "../types";

export async function getAgendaProfissional(profissionalId: string): Promise<AgendamentoSaude[]> {
  const response = await api.get(`/api/saude/agenda/${profissionalId}`);
  return response.data.data as AgendamentoSaude[];
}

export async function getHistoricoAluno(alunoId: string, incluirPrivado = false): Promise<AgendamentoSaude[]> {
  const response = await api.get(`/api/saude/alunos/${alunoId}/historico`, {
    params: { incluirPrivado },
  });
  return response.data.data as AgendamentoSaude[];
}

export async function criarAgendamento(payload: {
  alunoId: string;
  dataHora: string;
  observacoes?: string;
  privado?: boolean;
}): Promise<void> {
  await api.post("/api/saude/agendamentos", payload);
}
