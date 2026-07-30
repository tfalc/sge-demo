import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { EmptyState, PageSkeleton } from "../../components/ui/EmptyState";
import { getInadimplenciaEscola } from "../../services/relatoriosService";
import { getTurmaDesempenho } from "../../services/relatoriosService";
import { getTurmas } from "../../services/academicoService";
import type { InadimplenciaEscola } from "../../types";
import { formatCurrency } from "../../utils/format";
import { direcaoModuloCards } from "../secretaria/gestaoNav";
import { useGestaoArea } from "../secretaria/useGestaoArea";

const TURMA_DEMO = "55555555-5555-5555-5555-555555555555";
const MODULOS = direcaoModuloCards();

export function DirecaoPage() {
  const { primaryNav } = useGestaoArea();
  const [financeiro, setFinanceiro] = useState<InadimplenciaEscola | null>(null);
  const [mediaTurma, setMediaTurma] = useState<number | null>(null);
  const [turmaNome, setTurmaNome] = useState<string>("");
  const [alunosRisco, setAlunosRisco] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void (async () => {
      try {
        const fin = await getInadimplenciaEscola();
        setFinanceiro(fin);
        const turmas = await getTurmas();
        // Prefere turma com dados demo ricos (3A), senão a primeira
        const preferida =
          turmas.find((t) => /3A/i.test(t.nome)) ??
          turmas.find((t) => /3/.test(t.nome)) ??
          turmas[0];
        const turmaId = preferida?.id ?? TURMA_DEMO;
        setTurmaNome(preferida?.nome ?? "turma demo");
        const desemp = await getTurmaDesempenho(turmaId);
        setMediaTurma(desemp.mediaTurma);
        setAlunosRisco(desemp.alunosEmRisco);
      } catch {
        setError("Não foi possível carregar o painel. Confirme se a API está ativa e tente novamente.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal da Direção</h2>
        <p className="mt-1 text-sm text-slate-600">
          Visão executiva e gestão estratégica. Use o menu ou os atalhos para cada módulo.
        </p>
      </div>

      <SectionNav items={primaryNav} visibleCount={7} />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {loading ? (
        <PageSkeleton />
      ) : financeiro ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card label="Recebido no mês" value={formatCurrency(financeiro.totalRecebido)} tone="emerald" />
          <Card label="Em atraso" value={formatCurrency(financeiro.totalVencido)} tone="red" />
          <Card label="Inadimplentes" value={String(financeiro.quantidadeInadimplentes)} tone="amber" />
          <Card
            label={`Média ${turmaNome}`}
            value={mediaTurma != null ? mediaTurma.toFixed(2) : "—"}
            tone="blue"
          />
        </div>
      ) : (
        <EmptyState
          title="Indicadores indisponíveis"
          description="Ainda não há dados financeiros/acadêmicos para exibir neste painel."
        />
      )}

      {alunosRisco != null && alunosRisco > 0 ? (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          {alunosRisco} aluno(s) com indicadores acadêmicos abaixo do esperado. Consulte a Coordenação.
        </div>
      ) : null}

      <section>
        <h3 className="text-base font-semibold text-slate-900">Atalhos da gestão</h3>
        <p className="mt-1 text-sm text-slate-600">
          Funções estratégicas em <strong>/direção</strong>. A Secretaria fica com o operacional do dia a dia.
        </p>
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {MODULOS.map((modulo) => (
            <Link
              key={modulo.to}
              to={modulo.to}
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-brand-blue hover:shadow-md focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue"
            >
              <p className="font-semibold text-slate-900">{modulo.label}</p>
              <p className="mt-1 text-sm text-slate-600">{modulo.desc}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}

function Card({
  label,
  value,
  tone,
}: {
  label: string;
  value: string;
  tone: "emerald" | "red" | "amber" | "blue";
}) {
  const tones = {
    emerald: "border-emerald-200 bg-emerald-50",
    red: "border-red-200 bg-red-50",
    amber: "border-amber-200 bg-amber-50",
    blue: "border-sky-200 bg-sky-50",
  };
  return (
    <div className={`rounded-xl border p-4 ${tones[tone]}`}>
      <p className="text-sm text-slate-700">{label}</p>
      <p className="mt-2 text-2xl font-bold text-slate-900">{value}</p>
    </div>
  );
}
