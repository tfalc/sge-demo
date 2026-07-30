import { useCallback, useEffect, useMemo, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";

import {
  createCharge,
  criarContrato,
  criarPlano,
  gerarCobrancasMes,
  getContracts,
  getDefaulters,
  getMonthlyReport,
  getPlanos,
} from "../../services/financeiroService";
import { listarAlunos } from "../../services/cadastroService";
import { useGestaoArea } from "./useGestaoArea";
import type { AlunoCadastro, Contract, Defaulter, MonthlyReport, PlanoPagamentoCadastro } from "../../types";
import { formatCompetencia, formatCurrency, formatDate } from "../../utils/format";

function currentMonthIso(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  return `${now.getFullYear()}-${month}-01`;
}

function dueDateForMonth(yearMonth: string, day: number): string {
  const [year, month] = yearMonth.split("-").map(Number);
  const lastDay = new Date(year, month, 0).getDate();
  const safeDay = Math.min(day, lastDay);
  return `${year}-${String(month).padStart(2, "0")}-${String(safeDay).padStart(2, "0")}`;
}

export function SecretariaFinanceiroPage() {
  const { areaLabel, primaryNav } = useGestaoArea();
  const [report, setReport] = useState<MonthlyReport | null>(null);
  const [defaulters, setDefaulters] = useState<Defaulter[]>([]);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [selectedContractId, setSelectedContractId] = useState("");
  const [competencia, setCompetencia] = useState(currentMonthIso());
  const [valor, setValor] = useState("");
  const [vencimento, setVencimento] = useState("");
  const [creating, setCreating] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [planos, setPlanos] = useState<PlanoPagamentoCadastro[]>([]);
  const [planoNome, setPlanoNome] = useState("");
  const [planoValor, setPlanoValor] = useState("850");
  const [planoDia, setPlanoDia] = useState("10");

  const [alunos, setAlunos] = useState<AlunoCadastro[]>([]);
  const [contratoAlunoId, setContratoAlunoId] = useState("");
  const [contratoPlanoId, setContratoPlanoId] = useState("");
  const [contratoDataInicio, setContratoDataInicio] = useState(new Date().toISOString().slice(0, 10));
  const [contratoSaving, setContratoSaving] = useState(false);

  const alunosSemContrato = useMemo(() => {
    const comContrato = new Set(contracts.map((c) => c.alunoId).filter(Boolean));
    return alunos.filter((a) => !comContrato.has(a.id));
  }, [alunos, contracts]);

  const selectedContract = useMemo(
    () => contracts.find((c) => c.id === selectedContractId) ?? null,
    [contracts, selectedContractId],
  );

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [r, d, c, p, a] = await Promise.all([
        getMonthlyReport(),
        getDefaulters(),
        getContracts(),
        getPlanos(),
        listarAlunos(),
      ]);
      setReport(r);
      setDefaulters(d);
      setContracts(c);
      setPlanos(p);
      setAlunos(a);
      setContratoPlanoId((prev) => prev || (p[0]?.id ?? ""));
      setSelectedContractId((prev) => {
        if (prev) return prev;
        if (c.length === 0) return prev;
        setValor(String(c[0].valorMensalidade));
        setVencimento(dueDateForMonth(currentMonthIso(), c[0].diaVencimento));
        return c[0].id;
      });
    } catch {
      setError("Nao foi possivel carregar dados financeiros. Verifique login e backend.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  function handleContractChange(contractId: string) {
    setSelectedContractId(contractId);
    const contract = contracts.find((c) => c.id === contractId);
    if (contract) {
      setValor(String(contract.valorMensalidade));
      setVencimento(dueDateForMonth(competencia, contract.diaVencimento));
    }
  }

  function handleCompetenciaChange(value: string) {
    const normalized = value.length >= 7 ? `${value.slice(0, 7)}-01` : value;
    setCompetencia(normalized);
    if (selectedContract) {
      setVencimento(dueDateForMonth(normalized, selectedContract.diaVencimento));
    }
  }

  async function handleCriarContrato(e: React.FormEvent) {
    e.preventDefault();
    if (!contratoAlunoId || !contratoPlanoId) return;

    setContratoSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await criarContrato({
        alunoId: contratoAlunoId,
        planoId: contratoPlanoId,
        dataInicio: contratoDataInicio || undefined,
      });
      setContratoAlunoId("");
      setSuccess("Contrato de mensalidade criado.");
      await loadData();
    } catch {
      setError("Falha ao criar contrato. Verifique se o aluno ja possui contrato ativo.");
    } finally {
      setContratoSaving(false);
    }
  }

  async function handleCriarPlano(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true);
    setError(null);
    setSuccess(null);
    try {
      await criarPlano({
        nome: planoNome,
        valorMensalidade: Number(planoValor),
        diaVencimento: Number(planoDia),
      });
      setPlanoNome("");
      setSuccess("Plano cadastrado.");
      await loadData();
    } catch {
      setError("Falha ao cadastrar plano.");
    } finally {
      setCreating(false);
    }
  }

  async function handleGerarCobrancasMes() {
    setGenerating(true);
    setError(null);
    setSuccess(null);
    try {
      const result = await gerarCobrancasMes();
      setSuccess(
        `Cobrancas do mes ${result.competencia}: ${result.criadas} criada(s), ${result.ignoradas} ja existente(s).`,
      );
      await loadData();
    } catch {
      setError("Falha ao gerar cobrancas do mes.");
    } finally {
      setGenerating(false);
    }
  }

  async function handleCreateCharge(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedContractId || !competencia || !valor || !vencimento) return;

    setCreating(true);
    setError(null);
    setSuccess(null);
    try {
      await createCharge({
        contratoId: selectedContractId,
        competencia,
        valor: Number(valor),
        vencimento,
      });
      setSuccess("Cobranca criada com sucesso. O responsavel ja pode visualiza-la no portal dos pais.");
      await loadData();
    } catch {
      setError("Falha ao criar cobranca. Verifique os dados e tente novamente.");
    } finally {
      setCreating(false);
    }
  }

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Financeiro</h2>
        <p className="mt-1 text-sm text-slate-600">
          Resumo do mes, inadimplencia e geracao de novas cobrancas.
        </p>
      </div>

      <SectionNav items={primaryNav} />

      <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-base font-semibold text-slate-900">Planos de pagamento</h3>
        <form className="mt-4 grid gap-3 sm:grid-cols-4" onSubmit={(e) => void handleCriarPlano(e)}>
          <Input label="Nome" value={planoNome} onChange={(e) => setPlanoNome(e.target.value)} required />
          <Input label="Valor mensal" type="number" value={planoValor} onChange={(e) => setPlanoValor(e.target.value)} required />
          <Input label="Dia venc." type="number" value={planoDia} onChange={(e) => setPlanoDia(e.target.value)} required />
          <div className="flex items-end">
            <Button type="submit">Novo plano</Button>
          </div>
        </form>
        {planos.length > 0 ? (
          <ul className="mt-3 space-y-1 text-sm text-slate-600">
            {planos.map((p) => (
              <li key={p.id}>
                {p.nome} — R$ {p.valorMensalidade} (dia {p.diaVencimento})
              </li>
            ))}
          </ul>
        ) : null}
      </section>

      <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-base font-semibold text-slate-900">Contratos de mensalidade</h3>
        <p className="mt-1 text-sm text-slate-600">
          Vincule aluno a um plano para gerar cobrancas automaticas ou avulsas.
        </p>

        <form className="mt-4 grid gap-3 md:grid-cols-4" onSubmit={(e) => void handleCriarContrato(e)}>
          <label className="block text-sm md:col-span-2">
            <span className="mb-1 block font-medium text-slate-700">Aluno sem contrato</span>
            <select
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              value={contratoAlunoId}
              onChange={(e) => setContratoAlunoId(e.target.value)}
              required
            >
              <option value="">Selecione...</option>
              {alunosSemContrato.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.nome} ({a.matricula})
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Plano</span>
            <select
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              value={contratoPlanoId}
              onChange={(e) => setContratoPlanoId(e.target.value)}
              required
            >
              {planos.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nome} — R$ {p.valorMensalidade}
                </option>
              ))}
            </select>
          </label>
          <Input
            label="Inicio"
            type="date"
            value={contratoDataInicio}
            onChange={(e) => setContratoDataInicio(e.target.value)}
          />
          <div className="flex items-end md:col-span-4">
            <Button type="submit" size="sm" disabled={contratoSaving || planos.length === 0}>
              {contratoSaving ? "Criando..." : "Novo contrato"}
            </Button>
          </div>
        </form>

        {contracts.length > 0 ? (
          <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Aluno</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Matricula</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Plano</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Valor</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Venc.</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Inicio</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {contracts.map((c) => (
                  <tr key={c.id}>
                    <td className="px-4 py-2 font-medium text-slate-900">{c.alunoNome}</td>
                    <td className="px-4 py-2">{c.matricula}</td>
                    <td className="px-4 py-2">{c.planoNome}</td>
                    <td className="px-4 py-2">{formatCurrency(c.valorMensalidade)}</td>
                    <td className="px-4 py-2">Dia {c.diaVencimento}</td>
                    <td className="px-4 py-2">{c.dataInicio ? formatDate(c.dataInicio) : "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="mt-3 text-sm text-slate-600">Nenhum contrato ativo.</p>
        )}
      </section>

      <div className="flex flex-wrap gap-3">
        <Button variant="brand" disabled={generating} onClick={() => void handleGerarCobrancasMes()}>
          {generating ? "Gerando..." : "Gerar cobrancas do mes"}
        </Button>
        <p className="self-center text-xs text-slate-500">
          Cria cobrancas para todos os contratos ativos sem competencia do mes corrente.
        </p>
      </div>

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          {error}
        </div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : (
        <>
          {report ? (
            <div className="grid gap-4 sm:grid-cols-3">
              <SummaryCard
                label="Recebido no mes"
                value={formatCurrency(report.totalRecebido)}
                hint={report.mes}
                tone="emerald"
              />
              <SummaryCard
                label="Pendente no prazo"
                value={formatCurrency(report.totalPendente)}
                hint="Vencimento ainda nao passou"
                tone="amber"
              />
              <SummaryCard
                label="Em atraso"
                value={formatCurrency(report.totalVencido)}
                hint="Vencidas e nao pagas"
                tone="red"
              />
            </div>
          ) : null}

          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Nova cobranca</h3>
            <p className="mt-1 text-sm text-slate-600">
              Gera mensalidade com PIX simulado para o responsavel do aluno.
            </p>

            <form className="mt-4 grid gap-4 md:grid-cols-2" onSubmit={(e) => void handleCreateCharge(e)}>
              <label className="block text-sm md:col-span-2">
                <span className="mb-1 block font-medium text-slate-700">Contrato / Aluno</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                  value={selectedContractId}
                  onChange={(e) => handleContractChange(e.target.value)}
                >
                  {contracts.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.alunoNome} ({c.matricula}) — {c.planoNome}
                    </option>
                  ))}
                </select>
              </label>

              <Input
                label="Competencia (mes)"
                type="month"
                value={competencia.slice(0, 7)}
                onChange={(e) => handleCompetenciaChange(e.target.value)}
              />
              <Input
                label="Valor (R$)"
                type="number"
                min="0"
                step="0.01"
                value={valor}
                onChange={(e) => setValor(e.target.value)}
              />
              <Input
                label="Vencimento"
                type="date"
                value={vencimento}
                onChange={(e) => setVencimento(e.target.value)}
              />

              <div className="flex items-end md:col-span-2">
                <Button type="submit" disabled={creating}>
                  {creating ? "Criando..." : "Gerar cobranca"}
                </Button>
              </div>
            </form>
          </section>

          <section className="space-y-3">
            <h3 className="text-base font-semibold text-slate-900">
              Inadimplentes ({defaulters.length})
            </h3>

            {defaulters.length === 0 ? (
              <div className="rounded-lg border border-slate-200 bg-white px-4 py-6 text-center text-sm text-slate-600">
                Nenhuma cobranca em atraso no momento.
              </div>
            ) : (
              <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                <table className="min-w-full divide-y divide-slate-200 text-sm">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-4 py-3 text-left font-semibold text-slate-700">Aluno</th>
                      <th className="px-4 py-3 text-left font-semibold text-slate-700">Competencia</th>
                      <th className="px-4 py-3 text-left font-semibold text-slate-700">Vencimento</th>
                      <th className="px-4 py-3 text-left font-semibold text-slate-700">Atraso</th>
                      <th className="px-4 py-3 text-left font-semibold text-slate-700">Valor</th>
                      <th className="px-4 py-3 text-left font-semibold text-slate-700">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {defaulters.map((item) => (
                      <tr key={item.id} className="hover:bg-slate-50/80">
                        <td className="px-4 py-3 font-medium text-slate-900">{item.alunoNome}</td>
                        <td className="px-4 py-3">{formatCompetencia(item.competencia)}</td>
                        <td className="px-4 py-3">{formatDate(item.vencimento)}</td>
                        <td className="px-4 py-3 text-red-700">{item.diasAtraso} dias</td>
                        <td className="px-4 py-3 font-medium">{formatCurrency(item.valor)}</td>
                        <td className="px-4 py-3">
                          <Badge status={item.status} />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}

type SummaryTone = "emerald" | "amber" | "red";

function SummaryCard({
  label,
  value,
  hint,
  tone,
}: {
  label: string;
  value: string;
  hint: string;
  tone: SummaryTone;
}) {
  const toneClass: Record<SummaryTone, string> = {
    emerald: "border-emerald-200 bg-emerald-50",
    amber: "border-amber-200 bg-amber-50",
    red: "border-red-200 bg-red-50",
  };

  return (
    <div className={`rounded-xl border p-4 ${toneClass[tone]}`}>
      <p className="text-sm font-medium text-slate-700">{label}</p>
      <p className="mt-2 text-2xl font-bold text-slate-900">{value}</p>
      <p className="mt-1 text-xs text-slate-600">{hint}</p>
    </div>
  );
}
