import { useCallback, useEffect, useState } from "react";
import { HorarioWeekCalendar } from "../../components/horarios/HorarioWeekCalendar";
import { getTurmas } from "../../services/academicoService";
import { listarDisciplinas } from "../../services/academicoEstruturaService";
import { listarHorariosTurmas } from "../../services/horarioService";
import type { DisciplinaCadastro, HorarioAula } from "../../types";
import { DIAS_SEMANA, formatHora } from "../../utils/horarioLabels";
import { horariosDaDisciplina } from "../../utils/horarioUtils";
import { HorarioPanel, HorariosAlerts } from "./HorariosPageShell";

export function SecretariaHorariosDisciplinaPage() {
  const [disciplinas, setDisciplinas] = useState<DisciplinaCadastro[]>([]);
  const [disciplinaId, setDisciplinaId] = useState("");
  const [todosHorarios, setTodosHorarios] = useState<HorarioAula[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [d, t] = await Promise.all([listarDisciplinas(), getTurmas()]);
      setDisciplinas(d);
      const horarios = await listarHorariosTurmas(t.map((x) => x.id));
      setTodosHorarios(horarios);
      if (d.length > 0) setDisciplinaId((current) => current || d[0].id);
    } catch {
      setError("Falha ao carregar horarios por materia.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const filtrados = disciplinaId ? horariosDaDisciplina(todosHorarios, disciplinaId) : [];
  const disciplinaNome = disciplinas.find((d) => d.id === disciplinaId)?.nome;

  return (
    <HorariosAlerts error={error} loading={loading}>
      <div className="space-y-4">
        <label className="block max-w-md text-sm">
          <span className="mb-1 block font-medium text-slate-700">Materia / disciplina</span>
          <select
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
            value={disciplinaId}
            onChange={(e) => setDisciplinaId(e.target.value)}
          >
            {disciplinas.map((d) => (
              <option key={d.id} value={d.id}>
                {d.nome}
              </option>
            ))}
          </select>
        </label>

        <HorarioPanel title={disciplinaNome ? `Horarios — ${disciplinaNome}` : "Horarios por materia"}>
          {filtrados.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhum horario cadastrado para esta materia.</p>
          ) : (
            <>
              <HorarioWeekCalendar horarios={filtrados} emptyMessage="" />
              <ul className="mt-6 divide-y divide-slate-100">
                {filtrados.map((h) => (
                  <li key={h.id} className="flex flex-wrap justify-between gap-2 py-3 text-sm first:pt-0">
                    <span className="font-medium text-slate-900">
                      {DIAS_SEMANA[h.diaSemana]} · {formatHora(h.horaInicio)}–{formatHora(h.horaFim)}
                    </span>
                    <span className="text-slate-600">
                      {h.turmaNome}
                      {h.professorNome ? ` · ${h.professorNome}` : ""}
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
