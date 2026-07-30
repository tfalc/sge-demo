import { useCallback, useEffect, useState } from "react";
import { HorarioWeekCalendar } from "../../components/horarios/HorarioWeekCalendar";
import { listarProfessores } from "../../services/academicoEstruturaService";
import { getHorariosProfessor } from "../../services/horarioService";
import type { HorarioAula, ProfessorCadastro } from "../../types";
import { DIAS_SEMANA, formatHora } from "../../utils/horarioLabels";
import { HorarioPanel, HorariosAlerts } from "./HorariosPageShell";

export function SecretariaHorariosProfessorPage() {
  const [professores, setProfessores] = useState<ProfessorCadastro[]>([]);
  const [professorId, setProfessorId] = useState("");
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadProfessores = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const p = await listarProfessores();
      setProfessores(p);
      if (p.length > 0) setProfessorId((current) => current || p[0].id);
    } catch {
      setError("Falha ao carregar professores.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadProfessores();
  }, [loadProfessores]);

  useEffect(() => {
    if (!professorId) return;
    setLoading(true);
    void getHorariosProfessor(professorId)
      .then(setHorarios)
      .catch(() => setError("Falha ao carregar horarios do professor."))
      .finally(() => setLoading(false));
  }, [professorId]);

  const professorNome = professores.find((p) => p.id === professorId)?.nome;

  return (
    <HorariosAlerts error={error} loading={loading}>
      <div className="space-y-4">
        <label className="block max-w-md text-sm">
          <span className="mb-1 block font-medium text-slate-700">Professor</span>
          <select
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
            value={professorId}
            onChange={(e) => setProfessorId(e.target.value)}
          >
            {professores.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>

        <HorarioPanel title={professorNome ? `Agenda — ${professorNome}` : "Horarios por professor"}>
          {horarios.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhum horario vinculado a este professor.</p>
          ) : (
            <>
              <HorarioWeekCalendar horarios={horarios} emptyMessage="" />
              <ul className="mt-6 divide-y divide-slate-100">
                {horarios.map((h) => (
                  <li key={h.id} className="flex flex-wrap justify-between gap-2 py-3 text-sm first:pt-0">
                    <span className="font-medium text-slate-900">
                      {DIAS_SEMANA[h.diaSemana]} · {formatHora(h.horaInicio)}–{formatHora(h.horaFim)}
                    </span>
                    <span className="text-slate-600">
                      {h.disciplinaNome} · {h.turmaNome}
                    </span>
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
