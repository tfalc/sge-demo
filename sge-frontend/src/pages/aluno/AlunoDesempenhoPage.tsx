import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { SectionNav } from "../../components/layout/SectionNav";
import { getMe } from "../../services/authService";
import { getBoletim, getFrequencia } from "../../services/academicoService";
import { downloadBoletimPdf } from "../../services/relatoriosService";
import type { Boletim, Frequencia } from "../../types";
import { alunoNav } from "./alunoNav";

export function AlunoDesempenhoPage() {
  const [alunoId, setAlunoId] = useState("");
  const [turmaNome, setTurmaNome] = useState<string | null>(null);
  const [boletim, setBoletim] = useState<Boletim | null>(null);
  const [frequencia, setFrequencia] = useState<Frequencia | null>(null);
  const [loading, setLoading] = useState(true);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadAcademic = useCallback(async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const [b, f] = await Promise.all([getBoletim(id), getFrequencia(id)]);
      setBoletim(b);
      setFrequencia(f);
    } catch {
      setError("Nao foi possivel carregar boletim e frequencia.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void (async () => {
      try {
        const me = await getMe();
        if (!me.alunoId) {
          setError("Este usuario nao esta vinculado a um aluno. Use aluno@sge.com para testar.");
          setLoading(false);
          return;
        }
        setAlunoId(me.alunoId);
        setTurmaNome(me.turmaNome);
        await loadAcademic(me.alunoId);
      } catch {
        setError("Falha ao carregar dados do aluno.");
        setLoading(false);
      }
    })();
  }, [loadAcademic]);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal do Aluno</h2>
        <p className="mt-1 text-sm text-slate-600">
          Suas notas e frequencia{turmaNome ? ` — turma ${turmaNome}` : ""}.
        </p>
      </div>

      <SectionNav items={alunoNav} />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : boletim && frequencia ? (
        <div className="space-y-6">
          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h3 className="text-base font-semibold text-slate-900">Boletim</h3>
              <Button
                className="!px-3 !py-1.5 text-xs"
                disabled={pdfLoading || !alunoId}
                onClick={() => {
                  setPdfLoading(true);
                  void downloadBoletimPdf(alunoId)
                    .catch(() => setError("Falha ao baixar PDF do boletim."))
                    .finally(() => setPdfLoading(false));
                }}
              >
                {pdfLoading ? "Gerando..." : "Baixar PDF"}
              </Button>
            </div>
            <p className="mt-1 text-xs text-slate-500">
              Nota minima para aprovacao: {boletim.notaMinimaAprovacao}
            </p>
            {boletim.periodos.length === 0 ? (
              <p className="mt-4 text-sm text-slate-600">Nenhuma nota lancada ainda.</p>
            ) : (
              <div className="mt-4 space-y-4">
                {boletim.periodos.map((periodo) => (
                  <div key={periodo.periodoId} className="rounded-lg border border-slate-100 bg-slate-50 p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <h4 className="font-semibold text-slate-800">{periodo.periodoNome}</h4>
                      <span className="text-sm font-semibold text-slate-700">
                        Media: {periodo.mediaGeral.toFixed(2)}
                      </span>
                    </div>
                    <div className="mt-3 overflow-hidden rounded-lg border border-slate-200 bg-white">
                      <table className="min-w-full text-sm">
                        <thead className="bg-slate-50">
                          <tr>
                            <th className="px-3 py-2 text-left font-medium text-slate-700">Disciplina</th>
                            <th className="px-3 py-2 text-left font-medium text-slate-700">Media</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {periodo.disciplinas.map((d) => (
                            <tr key={d.disciplinaId}>
                              <td className="px-3 py-2 font-medium">{d.disciplinaNome}</td>
                              <td className="px-3 py-2">{d.media.toFixed(2)}</td>
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
            <h3 className="text-base font-semibold text-slate-900">Frequencia</h3>
            <p className="mt-2 text-sm font-semibold text-slate-700">
              {frequencia.percentualGeral.toFixed(1)}% geral (minimo {frequencia.frequenciaMinima}%)
            </p>
            {frequencia.porDisciplina.length > 0 ? (
              <div className="mt-4 overflow-hidden rounded-lg border border-slate-200">
                <table className="min-w-full text-sm">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-3 py-2 text-left">Disciplina</th>
                      <th className="px-3 py-2 text-left">%</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {frequencia.porDisciplina.map((d) => (
                      <tr key={d.disciplinaId}>
                        <td className="px-3 py-2">{d.disciplinaNome}</td>
                        <td className="px-3 py-2 font-semibold">{d.percentual.toFixed(1)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : null}
          </section>
        </div>
      ) : null}
    </div>
  );
}
