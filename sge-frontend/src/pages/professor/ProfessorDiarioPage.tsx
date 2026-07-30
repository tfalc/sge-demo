import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  getDiarioNotas,
  getHistoricoAtas,
  getPresencasDaAula,
} from "../../services/academicoService";
import { listarOcorrencias } from "../../services/ocorrenciaService";
import type { AtaAulaResumo, OcorrenciaDisciplinar } from "../../types";
import { useProfessorContext } from "./ProfessorContext";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";
import { ProfessorTurmaSelectors } from "./ProfessorTurmaSelectors";

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export function ProfessorDiarioPage() {
  const { loading, error: ctxError, professorId, tdpId, alunos, disciplinas } = useProfessorContext();
  const [historico, setHistorico] = useState<AtaAulaResumo[]>([]);
  const [ocorrencias, setOcorrencias] = useState<OcorrenciaDisciplinar[]>([]);
  const [faltasHoje, setFaltasHoje] = useState(0);
  const [notasPreenchidas, setNotasPreenchidas] = useState(0);
  const [loadingExtra, setLoadingExtra] = useState(false);

  const disciplinaNome = useMemo(
    () => disciplinas.find((d) => d.id === tdpId)?.disciplinaNome ?? "",
    [disciplinas, tdpId],
  );

  const carregarResumo = useCallback(async () => {
    if (!tdpId) return;
    setLoadingExtra(true);
    try {
      const [atas, ocorr, presencas, diario] = await Promise.all([
        getHistoricoAtas(tdpId),
        listarOcorrencias(tdpId),
        getPresencasDaAula(tdpId, todayIso()).catch(() => []),
        getDiarioNotas(tdpId).catch(() => null),
      ]);
      setHistorico(atas.slice(0, 5));
      setOcorrencias(ocorr.slice(0, 5));
      setFaltasHoje(presencas.filter((p) => !p.presente).length);
      if (diario) {
        let count = 0;
        for (const linha of diario.alunos) {
          for (const cel of linha.periodos) {
            if (cel.valor != null) count++;
          }
        }
        setNotasPreenchidas(count);
      } else {
        setNotasPreenchidas(0);
      }
    } finally {
      setLoadingExtra(false);
    }
  }, [tdpId]);

  useEffect(() => {
    void carregarResumo();
  }, [carregarResumo]);

  return (
    <ProfessorAlerts error={ctxError} loading={loading || loadingExtra}>
      {professorId ? (
        <div className="space-y-4">
          <ProfessorPanel title="Diario de classe">
            <p className="mb-4 text-sm text-slate-600">
              Visao integrada da turma{disciplinaNome ? ` — ${disciplinaNome}` : ""}. Acesse atas, notas e
              frequencia pelos atalhos abaixo.
            </p>
            <ProfessorTurmaSelectors />

            <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                <p className="text-xs font-medium uppercase text-slate-500">Alunos</p>
                <p className="text-2xl font-semibold text-slate-900">{alunos.length}</p>
              </div>
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                <p className="text-xs font-medium uppercase text-slate-500">Faltas hoje</p>
                <p className="text-2xl font-semibold text-slate-900">{faltasHoje}</p>
              </div>
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                <p className="text-xs font-medium uppercase text-slate-500">Celulas de nota</p>
                <p className="text-2xl font-semibold text-slate-900">{notasPreenchidas}</p>
              </div>
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                <p className="text-xs font-medium uppercase text-slate-500">Ocorrencias</p>
                <p className="text-2xl font-semibold text-slate-900">{ocorrencias.length}</p>
              </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              <Link
                to="/professor/ata"
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
              >
                Ata de aula
              </Link>
              <Link
                to="/professor/notas"
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
              >
                Fichario de notas
              </Link>
              <Link
                to="/professor/frequencia"
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
              >
                Frequencia
              </Link>
              <Link
                to="/professor/ocorrencias"
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium hover:bg-slate-50"
              >
                Ocorrencias
              </Link>
            </div>
          </ProfessorPanel>

          <div className="grid gap-4 lg:grid-cols-2">
            <ProfessorPanel title="Ultimas atas">
              {historico.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma ata registrada nos ultimos 60 dias.</p>
              ) : (
                <ul className="divide-y divide-slate-100 text-sm">
                  {historico.map((item) => (
                    <li key={item.id} className="py-2">
                      <Link to="/professor/ata" className="font-medium text-brand-blue hover:underline">
                        {new Date(item.dataAula + "T12:00:00").toLocaleDateString("pt-BR")}
                      </Link>
                      <p className="text-slate-600">{item.conteudoResumo ?? "—"}</p>
                    </li>
                  ))}
                </ul>
              )}
            </ProfessorPanel>

            <ProfessorPanel title="Ocorrencias recentes">
              {ocorrencias.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhuma ocorrencia registrada.</p>
              ) : (
                <ul className="divide-y divide-slate-100 text-sm">
                  {ocorrencias.map((o) => (
                    <li key={o.id} className="py-2">
                      <p className="font-medium">
                        {o.alunoNome} — <span className="text-slate-600">{o.tipo}</span>
                      </p>
                      <p className="text-slate-600">{o.descricao.slice(0, 100)}</p>
                    </li>
                  ))}
                </ul>
              )}
            </ProfessorPanel>
          </div>
        </div>
      ) : null}
    </ProfessorAlerts>
  );
}
