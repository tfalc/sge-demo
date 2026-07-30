import { api, ACCESS_TOKEN_KEY } from "./api";
import type { AuthTokensResponse, UserMe } from "../types";
import { useAuthStore } from "../store/authStore";

export interface LoginRequest {
  email: string;
  password: string;
}

export async function login(payload: LoginRequest): Promise<AuthTokensResponse> {
  const response = await api.post("/api/auth/login", payload);
  const tokens = response.data.data as AuthTokensResponse;
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  useAuthStore.getState().setAccessToken(tokens.accessToken);
  const me = await getMe();
  useAuthStore.getState().setPerfil(me.perfil);
  useAuthStore.getState().setAreasMenu(me.areasMenu ?? []);
  return tokens;
}

export function logout() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  useAuthStore.getState().setAccessToken(null);
  useAuthStore.getState().setPerfil(null);
  useAuthStore.getState().setAreasMenu([]);
}

export async function forgotPassword(email: string): Promise<string> {
  const response = await api.post("/api/auth/esqueci-senha", null, {
    headers: { "X-Email": email },
  });
  const data = response.data.data as { mensagem: string };
  return data.mensagem;
}

export async function getMe(): Promise<UserMe> {
  const response = await api.get("/api/auth/me");
  return response.data.data as UserMe;
}

export async function atualizarPerfil(payload: {
  nome: string;
  email: string;
  telefone?: string;
}): Promise<UserMe> {
  const response = await api.put("/api/auth/perfil", payload);
  return response.data.data as UserMe;
}

export async function trocarSenha(payload: {
  senhaAtual: string;
  senhaNova: string;
}): Promise<void> {
  await api.put("/api/auth/senha", payload);
}
