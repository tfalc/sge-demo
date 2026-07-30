import { api } from "./api";
import type {
  AnoLetivoResumo,
  MatriculaDocumentoResumo,
  MatriculaProcessoResumo,
  StatusMatriculaProcesso,
  TipoDocumentoMatricula,
} from "../types";

export async function listarAnosLetivosMatricula(): Promise<AnoLetivoResumo[]> {
  const response = await api.get("/api/matricula-nova/anos-letivos");
  return response.data.data as AnoLetivoResumo[];
}

export async function listarProcessosMatricula(
  status?: StatusMatriculaProcesso,
): Promise<MatriculaProcessoResumo[]> {
  const response = await api.get("/api/matricula-nova/processos", {
    params: status ? { status } : undefined,
  });
  return response.data.data as MatriculaProcessoResumo[];
}

export async function obterProcessoMatricula(id: string): Promise<MatriculaProcessoResumo> {
  const response = await api.get(`/api/matricula-nova/processos/${id}`);
  return response.data.data as MatriculaProcessoResumo;
}

export async function criarProcessoMatricula(payload: {
  anoLetivoId: string;
  turmaPretendidaId?: string;
  responsavelId?: string;
  candidatoNome: string;
  matriculaSugerida?: string;
  responsavelNome?: string;
  responsavelEmail?: string;
  responsavelTelefone?: string;
  observacoes?: string;
}): Promise<MatriculaProcessoResumo> {
  const response = await api.post("/api/matricula-nova/processos", payload);
  return response.data.data as MatriculaProcessoResumo;
}

export async function atualizarProcessoMatricula(
  id: string,
  payload: {
    turmaPretendidaId?: string;
    responsavelId?: string;
    candidatoNome: string;
    matriculaSugerida?: string;
    responsavelNome?: string;
    responsavelEmail?: string;
    responsavelTelefone?: string;
    observacoes?: string;
  },
): Promise<MatriculaProcessoResumo> {
  const response = await api.put(`/api/matricula-nova/processos/${id}`, payload);
  return response.data.data as MatriculaProcessoResumo;
}

export async function enviarProcessoMatricula(id: string): Promise<MatriculaProcessoResumo> {
  const response = await api.put(`/api/matricula-nova/processos/${id}/enviar`);
  return response.data.data as MatriculaProcessoResumo;
}

export async function aprovarProcessoMatricula(id: string): Promise<MatriculaProcessoResumo> {
  const response = await api.put(`/api/matricula-nova/processos/${id}/aprovar`);
  return response.data.data as MatriculaProcessoResumo;
}

export async function rejeitarProcessoMatricula(
  id: string,
  motivo: string,
): Promise<MatriculaProcessoResumo> {
  const response = await api.put(`/api/matricula-nova/processos/${id}/rejeitar`, { motivo });
  return response.data.data as MatriculaProcessoResumo;
}

export async function concluirProcessoMatricula(id: string): Promise<MatriculaProcessoResumo> {
  const response = await api.post(`/api/matricula-nova/processos/${id}/concluir`);
  return response.data.data as MatriculaProcessoResumo;
}

export async function uploadDocumentoMatricula(
  processoId: string,
  tipo: TipoDocumentoMatricula,
  file: File,
): Promise<MatriculaDocumentoResumo> {
  const form = new FormData();
  form.append("file", file);
  const response = await api.post(`/api/matricula-nova/processos/${processoId}/documentos`, form, {
    params: { tipo },
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data.data as MatriculaDocumentoResumo;
}

export async function downloadDocumentoMatricula(processoId: string, documentoId: string): Promise<Blob> {
  const response = await api.get(
    `/api/matricula-nova/processos/${processoId}/documentos/${documentoId}`,
    { responseType: "blob" },
  );
  return response.data as Blob;
}

export async function excluirDocumentoMatricula(processoId: string, documentoId: string): Promise<void> {
  await api.delete(`/api/matricula-nova/processos/${processoId}/documentos/${documentoId}`);
}
