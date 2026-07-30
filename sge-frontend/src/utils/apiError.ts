import axios from "axios";

export function apiErrorMessage(err: unknown, fallback: string): string {
  if (!axios.isAxiosError(err)) {
    return fallback;
  }
  if (err.code === "ERR_NETWORK" || err.message === "Network Error") {
    return "Nao foi possivel contatar a API. Confira se o backend esta rodando em http://localhost:8080.";
  }
  const apiMessage = err.response?.data?.message;
  if (typeof apiMessage === "string" && apiMessage.trim()) {
    return apiMessage;
  }
  const status = err.response?.status;
  if (status === 500) {
    return `${fallback} Reinicie o backend (mvn spring-boot:run) apos atualizar o codigo.`;
  }
  if (status) {
    return `${fallback} (HTTP ${status})`;
  }
  return fallback;
}
