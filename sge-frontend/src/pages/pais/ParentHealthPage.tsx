import { useEffect, useState } from "react";
import { EmptyState, PageSkeleton } from "../../components/ui/EmptyState";
import { SectionNav } from "../../components/layout/SectionNav";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { getHistoricoAluno } from "../../services/saudeService";
import { useParentFilhoStore } from "../../store/parentFilhoStore";
import type { AgendamentoSaude } from "../../types";
import { formatDateTime } from "../../utils/dateRange";
import { parentNav } from "./parentNav";

const statusLabel: Record<string, string> = {
  AGENDADO: "Agendado",
  REALIZADO: "Realizado",
  CANCELADO: "Cancelado",
};

export function ParentHealthPage() {
  const filhoAtivoId = useParentFilhoStore((s) => s.filhoAtivoId);
  const filhos = useParentFilhoStore((s) => s.filhos);
  const [agendamentos, setAgendamentos] = useState<AgendamentoSaude[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!filhoAtivoId) {
      if (filhos.length === 0) setLoading(false);
      return;
    }
    let cancelled = false;
    void (async () => {
      setLoading(true);
      setError(null);
      try {
        const items = await getHistoricoAluno(filhoAtivoId, false);
        if (!cancelled) setAgendamentos(items);
      } catch {
        if (!cancelled) {
          setError(
            "Não foi possível carregar o histórico de atendimentos. Tente novamente em instantes.",
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [filhoAtivoId, filhos.length]);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais</h2>
        <p className="mt-1 text-sm text-slate-600">
          Acompanhe agendamentos de saúde e psicologia dos seus filhos. Observações clínicas privadas não são
          exibidas aqui.
        </p>
      </div>

      <SectionNav items={parentNav} />
      <ParentFilhoSelector />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {!filhoAtivoId && !loading ? (
        <EmptyState
          title="Nenhum filho vinculado"
          description="Peça à secretaria para vincular o responsável ao aluno."
        />
      ) : null}

      {loading ? (
        <PageSkeleton />
      ) : filhoAtivoId && agendamentos.length === 0 ? (
        <EmptyState
          title="Nenhum atendimento visível"
          description="Agendamentos com sigilo clínico aparecem apenas para a equipe de saúde."
        />
      ) : (
        <div className="space-y-3">
          {agendamentos.map((a) => (
            <article key={a.id} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div>
                  <p className="font-semibold text-slate-900">{formatDateTime(a.dataHora)}</p>
                  <p className="mt-1 text-sm text-slate-600">{a.alunoNome}</p>
                </div>
                <span className="rounded-full bg-sky-100 px-3 py-1 text-xs font-semibold text-sky-900">
                  {statusLabel[a.status] ?? a.status}
                </span>
              </div>
              {a.observacoes ? (
                <p className="mt-3 text-sm text-slate-700">{a.observacoes}</p>
              ) : a.privado ? (
                <p className="mt-3 text-sm italic text-slate-500">
                  Atendimento registrado — detalhes sob sigilo profissional.
                </p>
              ) : null}
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
