import { useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { EmptyState, PageSkeleton } from "../../components/ui/EmptyState";
import { SectionNav } from "../../components/layout/SectionNav";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { getBoletim, getFrequencia } from "../../services/academicoService";
import { downloadBoletimPdf } from "../../services/relatoriosService";
import { useParentFilhoStore } from "../../store/parentFilhoStore";
import type { Boletim, Frequencia } from "../../types";
import { parentNav } from "./parentNav";

export function ParentAcademicPage() {
  const filhoAtivoId = useParentFilhoStore((s) => s.filhoAtivoId);
  const filhos = useParentFilhoStore((s) => s.filhos);
  const [boletim, setBoletim] = useState<Boletim | null>(null);
  const [frequencia, setFrequencia] = useState<Frequencia | null>(null);
  const [loading, setLoading] = useState(true);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!filhoAtivoId) {
      if (filhos.length === 0) {
        setLoading(false);
        return;
      }
      return;
    }
    let cancelled = false;
    void (async () => {
      setLoading(true);
      setError(null);
      try {
        const [b, f] = await Promise.all([getBoletim(filhoAtivoId), getFrequencia(filhoAtivoId)]);
        if (cancelled) return;
        setBoletim(b);
        setFrequencia(f);
      } catch {
        if (!cancelled) {
          setError(
            "Não foi possível carregar boletim e frequência. Confirme a conexão e tente novamente.",
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
        <p className="mt-1 text-sm text-slate-600">Acompanhe notas e frequência dos seus filhos.</p>
      </div>

      <SectionNav items={parentNav} />
      <ParentFilhoSelector />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {!filhoAtivoId && !loading ? (
        <EmptyState
          title="Nenhum filho vinculado"
          description="Use a conta pai@sge.com na demo ou peça à secretaria para vincular o responsável."
        />
      ) : null}

      {loading ? (
        <PageSkeleton />
      ) : boletim && frequencia ? (
        <div className="space-y-6">
          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h3 className="text-base font-semibold text-slate-900">
                Boletim — {boletim.alunoNome}
                {boletim.turmaNome ? ` (${boletim.turmaNome})` : ""}
              </h3>
              <Button
                className="!px-3 !py-1.5 text-xs"
                disabled={pdfLoading}
                onClick={() => {
                  setPdfLoading(true);
                  void downloadBoletimPdf(filhoAtivoId!)
                    .catch(() =>
                      setError("Falha ao baixar o PDF. Tente novamente em alguns segundos."),
                    )
                    .finally(() => setPdfLoading(false));
                }}
              >
                {pdfLoading ? "Gerando..." : "Baixar PDF"}
              </Button>
            </div>
            <p className="mt-1 text-xs text-slate-500">
              Nota mínima para aprovação: {boletim.notaMinimaAprovacao}
            </p>

            {boletim.periodos.length === 0 ? (
              <EmptyState
                title="Nenhuma nota lançada"
                description="Assim que o professor registrar avaliações, o boletim aparece aqui."
              />
            ) : (
              <div className="mt-4 space-y-4">
                {boletim.periodos.map((periodo) => (
                  <div key={periodo.periodoId} className="rounded-lg border border-slate-100 bg-slate-50 p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <h4 className="font-semibold text-slate-800">{periodo.periodoNome}</h4>
                      <span
                        className={`text-sm font-semibold ${
                          periodo.aprovado ? "text-emerald-700" : "text-amber-700"
                        }`}
                      >
                        Média geral: {periodo.mediaGeral.toFixed(2)}
                      </span>
                    </div>
                    <div className="mt-3 overflow-hidden rounded-lg border border-slate-200 bg-white">
                      <table className="min-w-full text-sm">
                        <thead className="bg-slate-50">
                          <tr>
                            <th className="px-3 py-2 text-left font-medium text-slate-700">Disciplina</th>
                            <th className="px-3 py-2 text-left font-medium text-slate-700">Notas</th>
                            <th className="px-3 py-2 text-left font-medium text-slate-700">Média</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {periodo.disciplinas.map((d) => (
                            <tr key={d.disciplinaId}>
                              <td className="px-3 py-2 font-medium">{d.disciplinaNome}</td>
                              <td className="px-3 py-2 text-slate-600">
                                {d.notas.map((n) => `${n.tipo}: ${n.valor.toFixed(1)}`).join(" · ")}
                              </td>
                              <td className="px-3 py-2">
                                <span className={d.aprovado ? "text-emerald-700" : "text-red-700"}>
                                  {d.media.toFixed(2)}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h3 className="text-base font-semibold text-slate-900">Frequência</h3>
              <span
                className={`rounded-full px-3 py-1 text-sm font-semibold ${
                  frequencia.aprovadoFrequencia
                    ? "bg-emerald-100 text-emerald-800"
                    : "bg-red-100 text-red-800"
                }`}
              >
                {frequencia.percentualGeral.toFixed(1)}% geral
              </span>
            </div>
            <p className="mt-1 text-xs text-slate-500">Mínimo exigido: {frequencia.frequenciaMinima}%</p>

            {frequencia.porDisciplina.length === 0 ? (
              <EmptyState
                title="Nenhuma aula registrada"
                description="A frequência por disciplina aparece após o lançamento no diário."
              />
            ) : (
              <div className="mt-4 overflow-hidden rounded-lg border border-slate-200">
                <table className="min-w-full text-sm">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-3 py-2 text-left font-medium text-slate-700">Disciplina</th>
                      <th className="px-3 py-2 text-left font-medium text-slate-700">Aulas</th>
                      <th className="px-3 py-2 text-left font-medium text-slate-700">Presenças</th>
                      <th className="px-3 py-2 text-left font-medium text-slate-700">Faltas</th>
                      <th className="px-3 py-2 text-left font-medium text-slate-700">%</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {frequencia.porDisciplina.map((d) => (
                      <tr key={d.disciplinaId}>
                        <td className="px-3 py-2 font-medium">{d.disciplinaNome}</td>
                        <td className="px-3 py-2">{d.totalAulas}</td>
                        <td className="px-3 py-2 text-emerald-700">{d.presencas}</td>
                        <td className="px-3 py-2 text-red-700">{d.faltas}</td>
                        <td className="px-3 py-2 font-semibold">{d.percentual.toFixed(1)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>
      ) : null}
    </div>
  );
}
