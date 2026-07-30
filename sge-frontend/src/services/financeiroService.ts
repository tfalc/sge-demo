import { api } from "./api";
import type {
  Charge,
  ChargeQrCode,
  Contract,
  Defaulter,
  GerarCobrancasMesResult,
  MonthlyReport,
  PixConfig,
  PlanoPagamentoCadastro,
} from "../types";

export async function getCharges(responsavelId: string): Promise<Charge[]> {
  const response = await api.get("/api/financeiro/cobrancas", {
    params: { responsavelId },
  });
  return response.data.data as Charge[];
}

export async function getChargeQrCode(cobrancaId: string): Promise<ChargeQrCode> {
  const response = await api.get(`/api/financeiro/cobrancas/${cobrancaId}/qrcode`);
  return response.data.data as ChargeQrCode;
}

export async function getPixConfig(): Promise<PixConfig> {
  const response = await api.get("/api/financeiro/pix/config");
  return response.data.data as PixConfig;
}

export async function simulatePayment(cobrancaId: string): Promise<void> {
  await api.post(`/api/financeiro/cobrancas/${cobrancaId}/simular-pagamento`);
}

export async function getMonthlyReport(): Promise<MonthlyReport> {
  const response = await api.get("/api/financeiro/relatorio-mensal");
  return response.data.data as MonthlyReport;
}

export async function getDefaulters(): Promise<Defaulter[]> {
  const response = await api.get("/api/financeiro/inadimplentes");
  return response.data.data as Defaulter[];
}

export async function getContracts(): Promise<Contract[]> {
  const response = await api.get("/api/financeiro/contratos");
  return response.data.data as Contract[];
}

export interface CreateChargePayload {
  contratoId: string;
  competencia: string;
  valor: number;
  vencimento: string;
}

export async function createCharge(payload: CreateChargePayload): Promise<void> {
  await api.post("/api/financeiro/cobrancas", payload);
}

export async function getPlanos(): Promise<PlanoPagamentoCadastro[]> {
  const response = await api.get("/api/financeiro/planos");
  return response.data.data as PlanoPagamentoCadastro[];
}

export async function criarPlano(payload: {
  nome: string;
  valorMensalidade: number;
  diaVencimento: number;
}): Promise<void> {
  await api.post("/api/financeiro/planos", payload);
}

export async function criarContrato(payload: {
  alunoId: string;
  planoId: string;
  dataInicio?: string;
}): Promise<void> {
  await api.post("/api/financeiro/contratos", payload);
}

export async function gerarCobrancasMes(): Promise<GerarCobrancasMesResult> {
  const response = await api.post("/api/financeiro/gerar-cobrancas-mes");
  return response.data.data as GerarCobrancasMesResult;
}
