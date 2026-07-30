import { useCallback, useEffect, useMemo, useState } from "react";
import {
  assinarMatrizPresencas,
  getMatrizPresencas,
  getPeriodos,
  salvarMatrizPresencas,
  type MatrizFrequencia,
} from "../../services/academicoService";
import type { PeriodoAvaliacao, TurmaAluno } from "../../types";
import { formatDate } from "../../utils/format";

type CelulaMap = Record<string, boolean>;

function celKey(alunoId: string, data: string): string {
  return `${alunoId}|${data}`;
}

type Props = {
  tdpId: string;
  alunos: TurmaAluno[];
};

export function FrequenciaMatrizPanel({ tdpId, alunos }: Props) {
  const [periodos, setPeriodos] = useState<PeriodoAvaliacao[]>([]);
  const [periodoId, setPeriodoId] = useState("");
  const [matriz, setMatriz] = useState<MatrizFrequencia | null>(null);
  const [celulas, setCelulas] = useState<CelulaMap>({});
  const [aulasPrevistas, setAulasPrevistas] = useState("");
  const [novaData, setNovaData] = useState("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const datas = useMemo(() => matriz?.datas ?? [], [matriz]);

  const carregarMatriz = useCallback(async () => {
    if (!tdpId || !periodoId) return;
    setLoading(true);
    setError(null);
    try {
      const m = await getMatrizPresencas(tdpId, periodoId);
      setMatriz(m);
      setAulasPrevistas(m.cabecalho.aulasPrevistas != null ? String(m.cabecalho.aulasPrevistas) : "");
      const map: CelulaMap = {};
      for (const linha of m.alunos) {
        for (const data of m.datas) {
          const cel = linha.presencasPorData[data];
          if (cel) {
            map[celKey(linha.alunoId, data)] = cel.presente;
          }
        }
      }
      setCelulas(map);
    } catch {
      setError("Falha ao carregar folha de frequencia.");
      setMatriz(null);
    } finally {
      setLoading(false);
    }
  }, [tdpId, periodoId]);

  useEffect(() => {
    void getPeriodos()
      .then((p) => {
        setPeriodos(p);
        if (p.length > 0) setPeriodoId(p[0].id);
      })
      .catch(() => setError("Falha ao carregar bimestres."));
  }, []);

  useEffect(() => {
    void carregarMatriz();
  }, [carregarMatriz]);

  function toggleCelula(alunoId: string, data: string) {
    const key = celKey(alunoId, data);
    setCelulas((prev) => {
      const atual = prev[key];
      const proximo = atual === undefined ? false : !atual;
      return { ...prev, [key]: proximo };
    });
  }

  function totalFaltas(alunoId: string): number {
    let faltas = 0;
    for (const data of datas) {
      const v = celulas[celKey(alunoId, data)];
      if (v === false) faltas++;
    }
    return faltas;
  }

  function marcarColuna(data: string, presente: boolean) {
    setCelulas((prev) => {
      const next = { ...prev };
      for (const aluno of alunos) {
        next[celKey(aluno.id, data)] = presente;
      }
      return next;
    });
  }

  function adicionarDataColuna() {
    if (!novaData || datas.includes(novaData)) return;
    setMatriz((prev) => {
      if (!prev) return prev;
      return { ...prev, datas: [...prev.datas, novaData].sort() };
    });
    setCelulas((prev) => {
      const next = { ...prev };
      for (const aluno of alunos) {
        next[celKey(aluno.id, novaData)] = true;
      }
      return next;
    });
    setNovaData("");
  }

  async function handleSalvar() {
    if (!tdpId || !periodoId || !matriz) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const celulasPayload: {
        alunoId: string;
        dataAula: string;
        presente: boolean;
      }[] = [];
      for (const aluno of alunos) {
        for (const data of datas) {
          const key = celKey(aluno.id, data);
          if (celulas[key] !== undefined) {
            celulasPayload.push({
              alunoId: aluno.id,
              dataAula: data,
              presente: celulas[key],
            });
          }
        }
      }
      const atualizada = await salvarMatrizPresencas({
        turmaDisciplinaProfessorId: tdpId,
        periodoId,
        aulasPrevistas: aulasPrevistas ? Number(aulasPrevistas) : undefined,
        celulas: celulasPayload,
      });
      setMatriz(atualizada);
      setSuccess("Folha de frequencia salva.");
    } catch {
      setError("Falha ao salvar folha.");
    } finally {
      setSaving(false);
    }
  }

  async function handleAssinar() {
    if (!tdpId || !periodoId) return;
    setSaving(true);
    setError(null);
    try {
      const atualizada = await assinarMatrizPresencas(tdpId, periodoId);
      setMatriz(atualizada);
      setSuccess("Frequencia assinada.");
    } catch {
      setError("Falha ao assinar.");
    } finally {
      setSaving(false);
    }
  }

  const cab = matriz?.cabecalho;

  return (
    <div className="space-y-4">
      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      <div className="flex flex-wrap gap-3 print:hidden">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Bimestre</span>
          <select
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
            value={periodoId}
            onChange={(e) => setPeriodoId(e.target.value)}
          >
            {periodos.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Aulas previstas</span>
          <input
            type="number"
            min="0"
            className="w-24 rounded-lg border border-slate-300 px-3 py-2 text-sm"
            value={aulasPrevistas}
            onChange={(e) => setAulasPrevistas(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Nova data (coluna)</span>
          <div className="flex gap-2">
            <input
              type="date"
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
              value={novaData}
              onChange={(e) => setNovaData(e.target.value)}
            />
            <button
              type="button"
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
              onClick={adicionarDataColuna}
            >
              +
            </button>
          </div>
        </label>
      </div>

      {loading ? (
        <p className="text-sm text-slate-500">Carregando folha...</p>
      ) : cab && matriz ? (
        <div id="freq-matriz-print">
          <div className="mb-4 hidden text-center print:block">
            <p className="text-lg font-bold">{cab.escolaNome}</p>
            <p className="text-sm">Folha de frequencia — {cab.disciplinaNome}</p>
          </div>

          <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs text-slate-700 sm:text-sm">
            <div className="grid gap-1 sm:grid-cols-2 lg:grid-cols-3">
              <span>
                <strong>Disciplina:</strong> {cab.disciplinaNome}
              </span>
              <span>
                <strong>Turma:</strong> {cab.serieNome} — {cab.turmaNome}
              </span>
              <span>
                <strong>Professor:</strong> {cab.professorNome ?? "—"}
              </span>
              <span>
                <strong>Periodo:</strong> {cab.periodoNome}
              </span>
              <span>
                <strong>Aulas dadas:</strong> {cab.aulasDadas}
                {cab.aulasPrevistas != null ? ` / previstas: ${cab.aulasPrevistas}` : ""}
              </span>
              {cab.periodoInicio && cab.periodoFim ? (
                <span>
                  <strong>Vigencia:</strong> {formatDate(cab.periodoInicio)} a {formatDate(cab.periodoFim)}
                </span>
              ) : null}
            </div>
          </div>

          <div className="mt-3 overflow-x-auto rounded-lg border border-slate-200 print:overflow-visible">
            <table className="min-w-full border-collapse text-xs print:text-[10px]">
              <thead>
                <tr className="bg-slate-100">
                  <th className="border border-slate-300 px-2 py-1 text-left">Nº</th>
                  <th className="border border-slate-300 px-2 py-1 text-left min-w-[140px]">Aluno</th>
                  {datas.map((data) => (
                    <th key={data} className="border border-slate-300 px-1 py-1 text-center">
                      <div className="print:hidden">
                        <button
                          type="button"
                          className="text-[10px] text-brand-blue"
                          onClick={() => marcarColuna(data, true)}
                        >
                          P
                        </button>
                        /
                        <button
                          type="button"
                          className="text-[10px] text-red-600"
                          onClick={() => marcarColuna(data, false)}
                        >
                          F
                        </button>
                      </div>
                      <span className="block whitespace-nowrap">{formatDate(data)}</span>
                    </th>
                  ))}
                  <th className="border border-slate-300 px-2 py-1">Faltas</th>
                </tr>
              </thead>
              <tbody>
                {alunos.map((aluno, idx) => (
                  <tr key={aluno.id}>
                    <td className="border border-slate-300 px-2 py-1">{String(idx + 1).padStart(2, "0")}</td>
                    <td className="border border-slate-300 px-2 py-1 font-medium">{aluno.nome}</td>
                    {datas.map((data) => {
                      const presente = celulas[celKey(aluno.id, data)];
                      const label = presente === undefined ? "—" : presente ? "P" : "F";
                      const tone =
                        presente === false
                          ? "text-red-700 font-semibold"
                          : presente === true
                            ? "text-emerald-700"
                            : "text-slate-400";
                      return (
                        <td key={data} className="border border-slate-300 px-1 py-1 text-center">
                          <button
                            type="button"
                            className={`w-full print:pointer-events-none ${tone}`}
                            onClick={() => toggleCelula(aluno.id, data)}
                          >
                            {label}
                          </button>
                        </td>
                      );
                    })}
                    <td className="border border-slate-300 px-2 py-1 text-center font-semibold text-red-700">
                      {totalFaltas(aluno.id)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {cab.assinaturaEm ? (
            <p className="mt-3 text-xs text-slate-600">
              Assinado em {new Date(cab.assinaturaEm).toLocaleString("pt-BR")}
            </p>
          ) : (
            <p className="mt-6 hidden border-t border-slate-400 pt-8 text-center text-sm print:block">
              Assinatura do(a) professor(a)
            </p>
          )}

          <div className="mt-4 flex flex-wrap gap-2 print:hidden">
            <button
              type="button"
              className="rounded-lg bg-brand-blue px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
              disabled={saving || alunos.length === 0}
              onClick={() => void handleSalvar()}
            >
              {saving ? "Salvando..." : "Salvar folha"}
            </button>
            <button
              type="button"
              className="rounded-lg border border-slate-300 px-4 py-2 text-sm hover:bg-slate-50"
              disabled={saving || !!cab.assinaturaEm}
              onClick={() => void handleAssinar()}
            >
              Assinar periodo
            </button>
            <button
              type="button"
              className="rounded-lg border border-slate-300 px-4 py-2 text-sm hover:bg-slate-50"
              onClick={() => window.print()}
            >
              Imprimir
            </button>
          </div>
        </div>
      ) : (
        <p className="text-sm text-slate-600">Selecione turma e bimestre para montar a folha.</p>
      )}
    </div>
  );
}
