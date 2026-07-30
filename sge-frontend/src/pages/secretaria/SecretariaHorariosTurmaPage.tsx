import { useCallback, useEffect, useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { HorarioWeekCalendar } from "../../components/horarios/HorarioWeekCalendar";
import { getTurmas } from "../../services/academicoService";
import { excluirHorario, getHorariosTurma } from "../../services/horarioService";
import type { HorarioAula, Turma } from "../../types";
import { DIAS_SEMANA, formatHora } from "../../utils/horarioLabels";
import { HorarioPanel, HorariosAlerts } from "./HorariosPageShell";

export function SecretariaHorariosTurmaPage() {
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [turmaId, setTurmaId] = useState("");
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const turmaSelecionada = turmas.find((t) => t.id === turmaId);

  const loadHorarios = useCallback(async (tid: string) => {
    if (!tid) return;
    setHorarios(await getHorariosTurma(tid));
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const t = await getTurmas();
      setTurmas(t);
      if (t.length > 0) {
        setTurmaId(t[0].id);
        await loadHorarios(t[0].id);
      }
    } catch {
      setError("Falha ao carregar turmas.");
    } finally {
      setLoading(false);
    }
  }, [loadHorarios]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    if (!turmaId) return;
    void loadHorarios(turmaId).catch(() => setError("Falha ao carregar horarios da turma."));
  }, [turmaId, loadHorarios]);

  async function handleExcluir(id: string) {
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await excluirHorario(id);
      setSuccess("Horario excluido.");
      await loadHorarios(turmaId);
    } catch {
      setError("Falha ao excluir horario.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <HorariosAlerts error={error} success={success} loading={loading}>
      <div className="space-y-4">
        <label className="block max-w-md text-sm">
          <span className="mb-1 block font-medium text-slate-700">Turma</span>
          <select
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
            value={turmaId}
            onChange={(e) => setTurmaId(e.target.value)}
          >
            {turmas.map((t) => (
              <option key={t.id} value={t.id}>
                {t.nome} — {t.serieNome}
              </option>
            ))}
          </select>
        </label>

        <HorarioPanel
          title={turmaSelecionada ? `Grade da turma ${turmaSelecionada.nome}` : "Grade da turma"}
        >
          {horarios.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhum horario cadastrado para esta turma.</p>
          ) : (
            <>
              <HorarioWeekCalendar horarios={horarios} showTurma={false} emptyMessage="" />
              <ul className="mt-6 divide-y divide-slate-100">
                {horarios.map((h) => (
                  <li key={h.id} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0">
                    <div className="text-sm">
                      <p className="font-medium text-slate-900">
                        {DIAS_SEMANA[h.diaSemana]} · {formatHora(h.horaInicio)}–{formatHora(h.horaFim)}
                      </p>
                      <p className="text-slate-600">
                        {h.disciplinaNome}
                        {h.professorNome ? ` · ${h.professorNome}` : ""}
                      </p>
                    </div>
                    <ActionButton
                      type="button"
                      variant="danger"
                      disabled={saving}
                      onClick={() => void handleExcluir(h.id)}
                    >
                      Excluir
                    </ActionButton>
                  </li>
                ))}
              </ul>
            </>
          )}
        </HorarioPanel>
      </div>
    </HorariosAlerts>
  );
}
