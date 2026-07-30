import { useCallback, useEffect, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Modal } from "../../components/ui/Modal";
import { getMe } from "../../services/authService";
import {
  getChargeQrCode,
  getCharges,
  getPixConfig,
  simulatePayment,
} from "../../services/financeiroService";
import type { Charge, ChargeQrCode, PixModo } from "../../types";
import { formatCompetencia, formatCurrency, formatDate } from "../../utils/format";
import { parentNav } from "./parentNav";

export function ParentChargesPage() {
  const [charges, setCharges] = useState<Charge[]>([]);
  const [guardianName, setGuardianName] = useState<string>("");
  const [pixModo, setPixModo] = useState<PixModo>("SIMULACAO");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCharge, setSelectedCharge] = useState<Charge | null>(null);
  const [qrData, setQrData] = useState<ChargeQrCode | null>(null);
  const [pixLoading, setPixLoading] = useState(false);
  const [payLoading, setPayLoading] = useState(false);
  const [copyFeedback, setCopyFeedback] = useState<string | null>(null);

  const loadCharges = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [me, config] = await Promise.all([getMe(), getPixConfig()]);
      setPixModo(config.modo);
      setGuardianName(me.nome);
      if (!me.responsavelId) {
        setError("Este usuário não está vinculado a um responsável. Use pai@sge.com para testar.");
        setCharges([]);
        return;
      }
      const list = await getCharges(me.responsavelId);
      setCharges(list);
    } catch {
      setError(
        "Não foi possível carregar cobranças. Verifique se o backend está rodando e você está logado.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadCharges();
  }, [loadCharges]);

  async function openPixModal(charge: Charge) {
    setSelectedCharge(charge);
    setQrData(null);
    setCopyFeedback(null);
    setPixLoading(true);
    try {
      const qr = await getChargeQrCode(charge.id);
      setQrData(qr);
      if (qr.pixModo) {
        setPixModo(qr.pixModo);
      }
    } catch {
      setError("Não foi possível gerar o PIX desta cobrança.");
      setSelectedCharge(null);
    } finally {
      setPixLoading(false);
    }
  }

  function closePixModal() {
    setSelectedCharge(null);
    setQrData(null);
    setCopyFeedback(null);
  }

  async function handleCopyPix() {
    if (!qrData?.pixCopyPaste) return;
    try {
      await navigator.clipboard.writeText(qrData.pixCopyPaste);
      setCopyFeedback("Código PIX copiado.");
    } catch {
      setCopyFeedback("Não foi possível copiar automaticamente. Selecione o texto abaixo.");
    }
  }

  async function handleSimulatePayment() {
    if (!selectedCharge) return;
    setPayLoading(true);
    try {
      await simulatePayment(selectedCharge.id);
      closePixModal();
      await loadCharges();
    } catch {
      setError("Falha ao simular pagamento.");
    } finally {
      setPayLoading(false);
    }
  }

  const canPay =
    selectedCharge?.status === "PENDENTE" || selectedCharge?.status === "VENCIDO";
  const simulacao = pixModo === "SIMULACAO";

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais</h2>
        <p className="mt-1 text-sm text-slate-600">
          {guardianName ? `Ola, ${guardianName}. ` : ""}
          {simulacao
            ? "Acompanhe mensalidades e pague via PIX (simulado em ambiente local)."
            : "Acompanhe mensalidades e pague via PIX. A confirmacao ocorre automaticamente apos o pagamento."}
        </p>
      </div>

      <SectionNav items={parentNav} />
      <ParentFilhoSelector />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          {error}
        </div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando cobrancas...</p>
      ) : charges.length === 0 && !error ? (
        <div className="rounded-lg border border-slate-200 bg-white px-4 py-8 text-center text-sm text-slate-600">
          Nenhuma cobranca encontrada para este responsavel.
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Aluno</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Competencia</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Vencimento</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Valor</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Status</th>
                <th className="px-4 py-3 text-right font-semibold text-slate-700">Acao</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {charges.map((charge) => (
                <tr key={charge.id} className="hover:bg-slate-50/80">
                  <td className="px-4 py-3 font-medium text-slate-900">{charge.alunoNome}</td>
                  <td className="px-4 py-3 text-slate-700">{formatCompetencia(charge.competencia)}</td>
                  <td className="px-4 py-3 text-slate-700">{formatDate(charge.vencimento)}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{formatCurrency(charge.valor)}</td>
                  <td className="px-4 py-3">
                    <Badge status={charge.status} />
                  </td>
                  <td className="px-4 py-3 text-right">
                    {charge.status === "PAGO" ? (
                      <span className="text-xs text-emerald-700">
                        Pago{charge.pagoEm ? ` em ${new Date(charge.pagoEm).toLocaleDateString("pt-BR")}` : ""}
                      </span>
                    ) : (
                      <Button size="sm" onClick={() => void openPixModal(charge)}>
                        Pagar PIX
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={selectedCharge !== null}
        title="Pagamento via PIX"
        onClose={closePixModal}
        footer={
          canPay ? (
            <div className="flex flex-wrap items-center justify-end gap-2">
              <Button variant="neutral" onClick={() => void handleCopyPix()} disabled={!qrData}>
                Copiar codigo PIX
              </Button>
              {simulacao ? (
                <Button onClick={() => void handleSimulatePayment()} disabled={payLoading || pixLoading}>
                  {payLoading ? "Confirmando..." : "Simular pagamento (local)"}
                </Button>
              ) : null}
            </div>
          ) : null
        }
      >
        {pixLoading ? (
          <p className="text-sm text-slate-600">Gerando codigo PIX...</p>
        ) : qrData && selectedCharge ? (
          <div className="space-y-4">
            <div className="rounded-lg bg-slate-50 px-4 py-3">
              <p className="text-sm text-slate-600">Valor</p>
              <p className="text-2xl font-bold text-slate-900">{formatCurrency(qrData.valor)}</p>
              <p className="mt-1 text-sm text-slate-600">
                {selectedCharge.alunoNome} — {formatCompetencia(selectedCharge.competencia)}
              </p>
            </div>

            {qrData.qrCodeImageUrl ? (
              <div className="flex justify-center">
                <img
                  src={qrData.qrCodeImageUrl}
                  alt="QR Code PIX"
                  className="h-48 w-48 rounded-lg border border-slate-200 bg-white p-2"
                />
              </div>
            ) : null}

            <div>
              <p className="mb-2 text-sm font-medium text-slate-700">Codigo PIX (copia e cola)</p>
              <textarea
                readOnly
                rows={4}
                className="w-full rounded-lg border border-slate-200 bg-slate-50 p-3 font-mono text-xs text-slate-800"
                value={qrData.pixCopyPaste}
              />
            </div>

            {copyFeedback ? <p className="text-sm text-emerald-700">{copyFeedback}</p> : null}

            {simulacao ? (
              <p className="text-xs text-slate-500">
                Em ambiente local o PIX e simulado. Use &quot;Simular pagamento&quot; para testar a confirmacao
                automatica, como se o banco tivesse notificado o sistema.
              </p>
            ) : (
              <p className="text-xs text-slate-500">
                Apos pagar no app do banco, o status sera atualizado automaticamente (pode levar alguns segundos).
              </p>
            )}
          </div>
        ) : null}
      </Modal>
    </div>
  );
}
