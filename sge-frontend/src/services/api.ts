import axios from "axios";

export const ACCESS_TOKEN_KEY = "sge.accessToken";

function resolveApiBaseUrl(): string {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL.replace(/\/$/, "");
  }
  // Dev: requests via Vite proxy (same origin) — evita CORS em qualquer porta do Vite
  if (import.meta.env.DEV) {
    return "";
  }
  // Produção/demo sem VITE_API_BASE_URL: same-origin (nginx proxy /api) ou falha explícita
  return "";
}

export const api = axios.create({
  baseURL: resolveApiBaseUrl(),
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
