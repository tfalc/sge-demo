import { api } from "./api";

export type PerfilUsuario =
  | "ADMIN"
  | "DIRETOR"
  | "COORDENADOR"
  | "PROFESSOR"
  | "SECRETARIA"
  | "PAI"
  | "ALUNO"
  | "NUTRICIONISTA"
  | "PSICOLOGA";

export interface UsuarioAdmin {
  id: string;
  email: string;
  nome: string | null;
  perfil: PerfilUsuario;
  ativo: boolean;
}

export async function listarUsuarios(): Promise<UsuarioAdmin[]> {
  const response = await api.get("/api/admin/usuarios");
  return response.data.data as UsuarioAdmin[];
}

export async function atualizarUsuario(
  id: string,
  payload: { perfil: PerfilUsuario; ativo: boolean },
): Promise<void> {
  await api.put(`/api/admin/usuarios/${id}`, payload);
}
