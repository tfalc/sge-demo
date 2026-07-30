import { useCallback, useEffect, useState } from "react";
import { HorarioWeekCalendar } from "../../components/horarios/HorarioWeekCalendar";
import { getTurmas } from "../../services/academicoService";
import { getHorariosTurma, listarHorariosTurmas } from "../../services/horarioService";
import type { HorarioAula, Turma } from "../../types";
import { HorarioPanel, HorariosAlerts } from "./HorariosPageShell";

export function SecretariaHorariosCalendarioPage() {
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [turmaId, setTurmaId] = useState("");
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTurmas = useCallback(async () => {
    try {
      const t = await getTurmas();
      setTurmas(t);
    } catch {
      setError("Falha ao carregar turmas.");
    }
  }, []);

  const loadHorarios = useCallback(async (filtroTurmaId: string) => {
    setLoading(true);
    setError(null);
    try {
      if (!filtroTurmaId) {
        const t = await getTurmas();
        setHorarios(await listarHorariosTurmas(t.map((x) => x.id)));
      } else {
        setHorarios(await getHorariosTurma(filtroTurmaId));
      }
    } catch {
      setError("Falha ao carregar calendario.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTurmas();
  }, [loadTurmas]);

  useEffect(() => {
    void loadHorarios(turmaId);
  }, [turmaId, loadHorarios]);

  const turmaLabel =
    turmaId === "" ? "Todas as turmas" : turmas.find((t) => t.id === turmaId)?.nome ?? "Turma";

  return (
    <HorariosAlerts error={error} loading={loading}>
      <div className="space-y-4">
        <div className="flex flex-wrap items-end gap-4">
          <label className="block min-w-[220px] text-sm">
            <span className="mb-1 block font-medium text-slate-700">Filtrar turma</span>
            <select
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              value={turmaId}
              onChange={(e) => setTurmaId(e.target.value)}
            >
              <option value="">Todas as turmas</option>
              {turmas.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.nome} — {t.serieNome}
                </option>
              ))}
            </select>
          </label>
          <p className="text-sm text-slate-500">
            Visao semanal estilo calendario · {turmaLabel} · {horarios.length} aula(s)
          </p>
        </div>

        <HorarioPanel title="Calendario semanal de aulas">
          <HorarioWeekCalendar
            horarios={horarios}
            showTurma={turmaId === ""}
            emptyMessage="Nenhuma aula no calendario. Cadastre horarios em Grade atual."
          />
        </HorarioPanel>
      </div>
    </HorariosAlerts>
  );
}
