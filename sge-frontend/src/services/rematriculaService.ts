import { api } from "./api";
import type {
  ApiResponse,
  FormularioRematricula,
  RematriculaConfig,
  RematriculaPortal,
  RematriculaRevisao,
  RematriculaSubmissaoResumo,
} from "../types";

export async function getRematriculaConfig(): Promise<RematriculaConfig> {
  const response = await api.get<ApiResponse<RematriculaConfig>>("/api/rematricula/config");
  return response.data.data;
}

export async function atualizarRematriculaConfig(payload: {
  titulo?: string;
  habilitada?: boolean;
  formulario?: FormularioRematricula;
}): Promise<RematriculaConfig> {
  const response = await api.put<ApiResponse<RematriculaConfig>>("/api/rematricula/config", payload);
  return response.data.data;
}

export async function uploadRematriculaModeloPdf(file: File): Promise<RematriculaConfig> {
  const formData = new FormData();
  formData.append("file", file);
  const response = await api.post<ApiResponse<RematriculaConfig>>("/api/rematricula/config/modelo-pdf", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data.data;
}

export async function downloadRematriculaModeloPdf(): Promise<Blob> {
  const response = await api.get("/api/rematricula/config/modelo-pdf", { responseType: "blob" });
  return response.data as Blob;
}

export async function getRematriculaPortal(): Promise<RematriculaPortal> {
  const response = await api.get<ApiResponse<RematriculaPortal>>("/api/rematricula/portal");
  return response.data.data;
}

export async function salvarRematriculaRascunho(
  alunoId: string,
  respostas: Record<string, unknown>,
): Promise<Record<string, unknown>> {
  const response = await api.put<ApiResponse<Record<string, unknown>>>(`/api/rematricula/alunos/${alunoId}/rascunho`, {
    respostas,
  });
  return response.data.data;
}

export async function revisarRematricula(
  alunoId: string,
  respostas: Record<string, unknown>,
): Promise<RematriculaRevisao> {
  const response = await api.post<ApiResponse<RematriculaRevisao>>(`/api/rematricula/alunos/${alunoId}/revisao`, {
    respostas,
  });
  return response.data.data;
}

export async function confirmarRematricula(alunoId: string): Promise<RematriculaSubmissaoResumo> {
  const response = await api.post<ApiResponse<RematriculaSubmissaoResumo>>(
    `/api/rematricula/alunos/${alunoId}/confirmar`,
  );
  return response.data.data;
}

export async function listarRematriculasPendentes(): Promise<RematriculaSubmissaoResumo[]> {
  const response = await api.get<ApiResponse<RematriculaSubmissaoResumo[]>>("/api/rematricula/submissoes/pendentes");
  return response.data.data;
}

export async function validarRematriculaSecretaria(submissaoId: string): Promise<RematriculaSubmissaoResumo> {
  const response = await api.put<ApiResponse<RematriculaSubmissaoResumo>>(
    `/api/rematricula/submissoes/${submissaoId}/validar`,
  );
  return response.data.data;
}

export async function detalheRematriculaSubmissao(submissaoId: string): Promise<RematriculaRevisao> {
  const response = await api.get<ApiResponse<RematriculaRevisao>>(`/api/rematricula/submissoes/${submissaoId}`);
  return response.data.data;
}

export async function downloadRematriculaPdfPreenchido(submissaoId: string): Promise<Blob> {
  const response = await api.get(`/api/rematricula/submissoes/${submissaoId}/pdf`, { responseType: "blob" });
  return response.data as Blob;
}
