import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { getHistoricoAtas, getTurmaDisciplinas } from "../../services/academicoService";
import { listarOcorrenciasTurma, marcarOcorrenciaVista } from "../../services/ocorrenciaService";
import type { AtaAulaResumo, DisciplinaVinculo, OcorrenciaDisciplinar } from "../../types";

type Props = {
  turmaId: string;
};

export function CoordenacaoSupervisaoPanel({ turmaId }: Props) {
  const [disciplinas, setDisciplinas] = useState<DisciplinaVinculo[]>([]);
  const [tdpId, setTdpId] = useState("");
  const [historico, setHistorico] = useState<AtaAulaResumo[]>([]);
  const [ocorrencias, setOcorrencias] = useState<OcorrenciaDisciplinar[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!turmaId) return;
    void getTurmaDisciplinas(turmaId)
      .then((d) => {
        setDisciplinas(d);
        if (d.length > 0) setTdpId(d[0].id);
      })
      .catch(() => setDisciplinas([]));
  }, [turmaId]);

  const carregar = useCallback(async () => {
    if (!tdpId || !turmaId) return;
    setLoading(true);
    setError(null);
    try {
      const [atas, ocorr] = await Promise.all([getHistoricoAtas(tdpId), listarOcorrenciasTurma(turmaId)]);
      setHistorico(atas);
      setOcorrencias(ocorr);
    } catch {
      setError("Falha ao carregar diario da turma.");
    } finally {
      setLoading(false);
    }
  }, [tdpId, turmaId]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  async function handleMarcarVista(id: string) {
    try {
      await marcarOcorrenciaVista(id);
      await carregar();
    } catch {
      setError("Falha ao atualizar ocorrencia.");
    }
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-slate-600">
        Supervisao do diario de classe: atas registradas pelos professores e ocorrencias disciplinares.
      </p>

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      <label className="block max-w-md text-sm">
        <span className="mb-1 block font-medium text-slate-700">Disciplina (para atas)</span>
        <select
          className="w-full rounded-lg border border-slate-300 px-3 py-2"
          value={tdpId}
          onChange={(e) => setTdpId(e.target.value)}
        >
          {disciplinas.map((d) => (
            <option key={d.id} value={d.id}>
              {d.disciplinaNome} — {d.professorNome ?? "Professor"}
            </option>
          ))}
        </select>
      </label>

      {loading ? <p className="text-sm text-slate-500">Carregando...</p> : null}

      <div className="grid gap-4 lg:grid-cols-2">
        <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="font-semibold text-slate-900">Historico de atas</h3>
          {historico.length === 0 ? (
            <p className="mt-2 text-sm text-slate-500">Nenhuma ata no periodo.</p>
          ) : (
            <ul className="mt-3 divide-y divide-slate-100 text-sm">
              {historico.map((a) => (
                <li key={a.id} className="py-2">
                  <p className="font-medium">
                    {new Date(a.dataAula + "T12:00:00").toLocaleDateString("pt-BR")}
                  </p>
                  <p className="text-slate-600">{a.conteudoResumo ?? "Sem conteudo."}</p>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="font-semibold text-slate-900">Ocorrencias da turma</h3>
          {ocorrencias.length === 0 ? (
            <p className="mt-2 text-sm text-slate-500">Nenhuma ocorrencia registrada.</p>
          ) : (
            <ul className="mt-3 divide-y divide-slate-100 text-sm">
              {ocorrencias.map((o) => (
                <li key={o.id} className="py-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-medium">{o.alunoNome}</span>
                    <span className="text-slate-500">{o.disciplinaNome}</span>
                    <span
                      className={
                        o.status === "REGISTRADA"
                          ? "rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-900"
                          : "rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-600"
                      }
                    >
                      {o.status === "REGISTRADA" ? "Nova" : "Vista"}
                    </span>
                  </div>
                  <p className="mt-1 text-slate-700">{o.descricao}</p>
                  {o.status === "REGISTRADA" ? (
                    <Button
                      className="mt-2"
                      variant="neutral"
                      onClick={() => void handleMarcarVista(o.id)}
                    >
                      Marcar como vista
                    </Button>
                  ) : null}
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
