import { api } from "./api";

export type GaleriaAlbum = {
  id: string;
  titulo: string;
  descricao?: string | null;
  visivelPara: string;
  exigirConsentimentoImagem?: boolean;
  turmaId?: string | null;
  turmaNome?: string | null;
  publicadoEm: string;
  quantidadeFotos: number;
  publicadoPorNome?: string | null;
};

export type GaleriaFoto = {
  id: string;
  albumId: string;
  nomeArquivo: string;
  contentType: string;
  tamanhoBytes: number;
  legenda?: string | null;
  ordem: number;
  enviadoEm: string;
};

export type GaleriaAlbumDetalhe = GaleriaAlbum & {
  fotos: GaleriaFoto[];
};

type ListParams = {
  audiencia?: string;
  turmaId?: string;
  gestao?: boolean;
  autorizadoImagem?: boolean;
};

export async function listarAlbunsGaleria(params: ListParams = {}): Promise<GaleriaAlbum[]> {
  const response = await api.get("/api/galeria/albuns", { params });
  return response.data.data as GaleriaAlbum[];
}

export async function obterAlbumGaleria(
  id: string,
  params: ListParams = {},
): Promise<GaleriaAlbumDetalhe> {
  const response = await api.get(`/api/galeria/albuns/${id}`, { params });
  return response.data.data as GaleriaAlbumDetalhe;
}

export async function criarAlbumGaleria(payload: {
  titulo: string;
  descricao?: string;
  visivelPara: string;
  turmaId?: string | null;
  exigirConsentimentoImagem?: boolean;
}): Promise<GaleriaAlbum> {
  const response = await api.post("/api/galeria/albuns", payload);
  return response.data.data as GaleriaAlbum;
}

export async function uploadFotoGaleria(
  albumId: string,
  file: File,
  legenda?: string,
): Promise<GaleriaFoto> {
  const form = new FormData();
  form.append("file", file);
  const response = await api.post(`/api/galeria/albuns/${albumId}/fotos`, form, {
    params: legenda ? { legenda } : undefined,
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data.data as GaleriaFoto;
}

export async function downloadFotoGaleria(
  fotoId: string,
  params: ListParams = {},
): Promise<Blob> {
  const response = await api.get(`/api/galeria/fotos/${fotoId}/arquivo`, {
    params,
    responseType: "blob",
  });
  return response.data as Blob;
}

export async function excluirAlbumGaleria(id: string): Promise<void> {
  await api.delete(`/api/galeria/albuns/${id}`);
}

export async function excluirFotoGaleria(id: string): Promise<void> {
  await api.delete(`/api/galeria/fotos/${id}`);
}

export function fotoGaleriaUrl(fotoId: string, params: ListParams = {}): string {
  const search = new URLSearchParams();
  if (params.audiencia) search.set("audiencia", params.audiencia);
  if (params.turmaId) search.set("turmaId", params.turmaId);
  if (params.gestao) search.set("gestao", "true");
  if (params.autorizadoImagem === false) search.set("autorizadoImagem", "false");
  const qs = search.toString();
  const base = api.defaults.baseURL ?? "";
  return `${base}/api/galeria/fotos/${fotoId}/arquivo${qs ? `?${qs}` : ""}`;
}
