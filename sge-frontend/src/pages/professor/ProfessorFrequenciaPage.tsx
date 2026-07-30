import { useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { getPresencasDaAula, lancarPresencas } from "../../services/academicoService";
import { useProfessorContext } from "./ProfessorContext";
import { FrequenciaMatrizPanel } from "./FrequenciaMatrizPanel";
import { PresencaChamadaTable } from "./PresencaChamadaTable";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";
import { ProfessorTurmaSelectors } from "./ProfessorTurmaSelectors";

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

type ModoFrequencia = "dia" | "bimestre";

export function ProfessorFrequenciaPage() {
  const { loading, error: ctxError, professorId, tdpId, alunos } = useProfessorContext();
  const [modo, setModo] = useState<ModoFrequencia>("bimestre");
  const [dataAula, setDataAula] = useState(todayIso());
  const [presencasInput, setPresencasInput] = useState<Record<string, boolean>>({});
  const [justificativasInput, setJustificativasInput] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!tdpId || !dataAula || alunos.length === 0) return;
    let cancelled = false;
    (async () => {
      try {
        const existentes = await getPresencasDaAula(tdpId, dataAula);
        if (cancelled) return;
        const map: Record<string, boolean> = {};
        const just: Record<string, string> = {};
        alunos.forEach((a) => {
          map[a.id] = true;
        });
        existentes.forEach((p) => {
          map[p.alunoId] = p.presente;
          if (p.justificativa) just[p.alunoId] = p.justificativa;
        });
        setPresencasInput(map);
        setJustificativasInput(just);
      } catch {
        // dia sem lancamento previo
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [tdpId, dataAula, alunos]);

  async function handleSalvarPresencas() {
    if (!tdpId || !dataAula) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await lancarPresencas({
        turmaDisciplinaProfessorId: tdpId,
        dataAula,
        presencas: alunos.map((aluno) => ({
          alunoId: aluno.id,
          presente: presencasInput[aluno.id] ?? true,
          justificativa:
            !(presencasInput[aluno.id] ?? true) && justificativasInput[aluno.id]?.trim()
              ? justificativasInput[aluno.id].trim()
              : undefined,
        })),
      });
      setSuccess("Frequência registrada com sucesso.");
    } catch {
      setError("Falha ao registrar frequência. Confirme turma/data e tente novamente.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <ProfessorAlerts error={error ?? ctxError} success={success} loading={loading}>
      {professorId ? (
        <ProfessorPanel title="Frequência">
          <p className="mb-4 text-sm text-slate-600">
            Chamada rápida por dia ou folha de frequência do bimestre (estilo diário de classe).
          </p>
          <ProfessorTurmaSelectors />

          <div className="mt-4 flex gap-2 border-b border-slate-200 print:hidden">
            <button
              type="button"
              className={`px-3 py-2 text-sm font-medium ${
                modo === "bimestre"
                  ? "border-b-2 border-brand-blue text-brand-blue"
                  : "text-slate-600"
              }`}
              onClick={() => setModo("bimestre")}
            >
              Folha do bimestre
            </button>
            <button
              type="button"
              className={`px-3 py-2 text-sm font-medium ${
                modo === "dia" ? "border-b-2 border-brand-blue text-brand-blue" : "text-slate-600"
              }`}
              onClick={() => setModo("dia")}
            >
              Chamada do dia
            </button>
          </div>

          {modo === "bimestre" ? (
            <div className="mt-4">
              {tdpId ? <FrequenciaMatrizPanel tdpId={tdpId} alunos={alunos} /> : null}
            </div>
          ) : (
            <div className="mt-4 space-y-4">
              <Input
                label="Data da aula"
                type="date"
                value={dataAula}
                onChange={(e) => setDataAula(e.target.value)}
              />
              <PresencaChamadaTable
                alunos={alunos}
                presencasInput={presencasInput}
                justificativasInput={justificativasInput}
                onPresencaChange={(alunoId, presente) =>
                  setPresencasInput((prev) => ({ ...prev, [alunoId]: presente }))
                }
                onJustificativaChange={(alunoId, value) =>
                  setJustificativasInput((prev) => ({ ...prev, [alunoId]: value }))
                }
              />
              <Button disabled={saving || alunos.length === 0} onClick={() => void handleSalvarPresencas()}>
                {saving ? "Salvando..." : "Salvar frequencia"}
              </Button>
            </div>
          )}
        </ProfessorPanel>
      ) : null}
    </ProfessorAlerts>
  );
}
