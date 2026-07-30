import { useCallback, useEffect, useMemo, useState } from "react";
import { Button } from "../../components/ui/Button";
import { getDiarioNotas, lancarNota } from "../../services/academicoService";
import type { DiarioNotas } from "../../types";
import { useProfessorContext } from "./ProfessorContext";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";
import { ProfessorTurmaSelectors } from "./ProfessorTurmaSelectors";

type CelulaKey = string;

function celulaPeriodoKey(alunoId: string, periodoId: string): CelulaKey {
  return `p:${alunoId}:${periodoId}`;
}

function celulaComplementoKey(alunoId: string): CelulaKey {
  return `c:${alunoId}`;
}

function parseNota(raw: string): number | null {
  const t = raw.trim();
  if (!t) return null;
  const valor = Number(t.replace(",", "."));
  if (Number.isNaN(valor) || valor < 0 || valor > 10) return null;
  return valor;
}

function formatNota(valor: number | null | undefined): string {
  if (valor == null) return "";
  return String(valor).replace(".", ",");
}

export function ProfessorNotasPage() {
  const { loading: ctxLoading, error: ctxError, professorId, tdpId, disciplinas } = useProfessorContext();
  const [diario, setDiario] = useState<DiarioNotas | null>(null);
  const [loadingDiario, setLoadingDiario] = useState(false);
  const [valores, setValores] = useState<Record<CelulaKey, string>>({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const disciplinaNome = useMemo(
    () => diario?.disciplinaNome ?? disciplinas.find((d) => d.id === tdpId)?.disciplinaNome ?? "",
    [diario, disciplinas, tdpId],
  );

  const carregarDiario = useCallback(async () => {
    if (!tdpId) return;
    setLoadingDiario(true);
    setError(null);
    try {
      const data = await getDiarioNotas(tdpId);
      setDiario(data);
      const map: Record<CelulaKey, string> = {};
      for (const linha of data.alunos) {
        for (const cel of linha.periodos) {
          map[celulaPeriodoKey(linha.alunoId, cel.periodoId)] = formatNota(cel.valor);
        }
        map[celulaComplementoKey(linha.alunoId)] = formatNota(linha.complemento.valor);
      }
      setValores(map);
    } catch {
      setError("Falha ao carregar diario de notas.");
      setDiario(null);
    } finally {
      setLoadingDiario(false);
    }
  }, [tdpId]);

  useEffect(() => {
    void carregarDiario();
  }, [carregarDiario]);

  function setCelula(key: CelulaKey, value: string) {
    setValores((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSalvar() {
    if (!tdpId || !diario) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const tarefas: Promise<void>[] = [];

      for (const linha of diario.alunos) {
        for (const cel of linha.periodos) {
          const key = celulaPeriodoKey(linha.alunoId, cel.periodoId);
          const raw = valores[key] ?? "";
          const parsed = parseNota(raw);
          const anterior = cel.valor;
          if (parsed == null && raw.trim() === "") continue;
          if (parsed == null) {
            setError("Informe notas validas entre 0 e 10.");
            setSaving(false);
            return;
          }
          if (anterior != null && Math.abs(anterior - parsed) < 0.001) continue;
          tarefas.push(
            lancarNota({
              alunoId: linha.alunoId,
              turmaDisciplinaProfessorId: tdpId,
              periodoId: cel.periodoId,
              valor: parsed,
              tipo: "FINAL",
            }),
          );
        }

        const complKey = celulaComplementoKey(linha.alunoId);
        const complRaw = valores[complKey] ?? "";
        const complParsed = parseNota(complRaw);
        const periodoComplId = linha.complemento.periodoId ?? diario.periodoComplementoId;
        if (complRaw.trim() && complParsed == null) {
          setError("Complemento invalido. Use valores de 0 a 10.");
          setSaving(false);
          return;
        }
        if (complParsed != null && periodoComplId) {
          const anteriorCompl = linha.complemento.valor;
          if (anteriorCompl == null || Math.abs(anteriorCompl - complParsed) >= 0.001) {
            tarefas.push(
              lancarNota({
                alunoId: linha.alunoId,
                turmaDisciplinaProfessorId: tdpId,
                periodoId: periodoComplId,
                valor: complParsed,
                tipo: "COMPLEMENTAR",
              }),
            );
          }
        }
      }

      if (tarefas.length === 0) {
        setSuccess("Nenhuma alteracao para salvar.");
        setSaving(false);
        return;
      }

      await Promise.all(tarefas);
      setSuccess(`${tarefas.length} nota(s) salva(s).`);
      await carregarDiario();
    } catch {
      setError("Falha ao salvar notas.");
    } finally {
      setSaving(false);
    }
  }

  const loading = ctxLoading || loadingDiario;
  const periodoComplNome =
    diario?.alunos[0]?.complemento.periodoNome ??
    diario?.periodos.find((p) => p.id === diario.periodoComplementoId)?.nome;

  return (
    <ProfessorAlerts error={error ?? ctxError} success={success} loading={loading}>
      {professorId ? (
        <div className="space-y-4">
          <ProfessorPanel title="Diario de notas (fichario)">
            <p className="mb-4 text-sm text-slate-600">
              Visao tabular da turma{disciplinaNome ? ` — ${disciplinaNome}` : ""}. Cada coluna e a nota final do
              bimestre. Use <strong>Complemento</strong> para atividade extra ou revisao pontual
              {periodoComplNome ? ` (${periodoComplNome})` : ""}.
            </p>

            <ProfessorTurmaSelectors />

            {diario && diario.alunos.length > 0 ? (
              <div id="fichario-print" className="mt-4">
                <div className="mb-3 hidden print:block">
                  <h2 className="text-lg font-bold">Fichario de notas — {disciplinaNome}</h2>
                  <p className="text-sm text-slate-600">Impresso em {new Date().toLocaleDateString("pt-BR")}</p>
                </div>
                <div className="overflow-x-auto rounded-lg border border-slate-200 print:overflow-visible print:border-black">
                <table className="min-w-full divide-y divide-slate-200 text-sm print:text-xs">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="sticky left-0 z-10 bg-slate-50 px-4 py-3 text-left font-semibold text-slate-700">
                        Aluno
                      </th>
                      <th className="px-3 py-3 text-left font-semibold text-slate-700">Matricula</th>
                      {diario.periodos.map((p) => (
                        <th
                          key={p.id}
                          className="min-w-[5.5rem] px-3 py-3 text-center font-semibold text-slate-700"
                          title="Nota final do bimestre"
                        >
                          {p.nome}
                        </th>
                      ))}
                      <th
                        className="min-w-[5.5rem] px-3 py-3 text-center font-semibold text-slate-700"
                        title="Atividade complementar ou revisao"
                      >
                        Complemento
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white">
                    {diario.alunos.map((linha) => (
                      <tr key={linha.alunoId} className="hover:bg-slate-50/80">
                        <td className="sticky left-0 z-10 bg-white px-4 py-2 font-medium text-slate-900">
                          {linha.nome}
                        </td>
                        <td className="whitespace-nowrap px-3 py-2 text-slate-600">{linha.matricula}</td>
                        {linha.periodos.map((cel) => {
                          const key = celulaPeriodoKey(linha.alunoId, cel.periodoId);
                          return (
                            <td key={cel.periodoId} className="px-2 py-2 text-center">
                              <input
                                type="text"
                                inputMode="decimal"
                                placeholder="—"
                                title={
                                  cel.origem && cel.origem !== "FINAL"
                                    ? `Valor inicial (${cel.origem}); salvar grava como nota final`
                                    : undefined
                                }
                                className="w-16 rounded border border-slate-300 px-2 py-1 text-center print:border-0 print:bg-transparent"
                                value={valores[key] ?? ""}
                                onChange={(e) => setCelula(key, e.target.value)}
                              />
                            </td>
                          );
                        })}
                        <td className="px-2 py-2 text-center">
                          <input
                            type="text"
                            inputMode="decimal"
                            placeholder="—"
                            className="w-16 rounded border border-amber-300 bg-amber-50/40 px-2 py-1 text-center print:border-0 print:bg-transparent"
                            value={valores[celulaComplementoKey(linha.alunoId)] ?? ""}
                            onChange={(e) => setCelula(celulaComplementoKey(linha.alunoId), e.target.value)}
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                </div>
              </div>
            ) : (
              !loadingDiario && <p className="mt-4 text-sm text-slate-500">Nenhum aluno na turma selecionada.</p>
            )}

            <div className="mt-4 flex flex-wrap gap-2 print:hidden">
              <Button variant="neutral" disabled={!diario?.alunos.length} onClick={() => window.print()}>
                Imprimir fichario
              </Button>
              <Button disabled={saving || !diario?.alunos.length} onClick={() => void handleSalvar()}>
                {saving ? "Salvando..." : "Salvar alteracoes"}
              </Button>
            </div>
          </ProfessorPanel>
        </div>
      ) : null}
    </ProfessorAlerts>
  );
}
