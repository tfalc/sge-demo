import type { HorarioAula } from "../../types";
import { DIAS_SEMANA, formatHora } from "../../utils/horarioLabels";
import { diaSemanaVigente, horariosDoDia, labelDiaSemana } from "../../utils/horarioUtils";

type Props = {
  horarios: HorarioAula[];
  turmaNome?: string;
};

export function HorarioDiaVigente({ horarios, turmaNome }: Props) {
  const dia = diaSemanaVigente();
  const hoje = new Date().toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });

  if (dia == null) {
    return (
      <div className="rounded-lg border border-amber-100 bg-amber-50 px-4 py-3 text-sm text-amber-900">
        <p className="font-medium">Fim de semana</p>
        <p className="mt-1 text-amber-800">
          Nao ha aulas regulares hoje ({hoje}). Selecione uma turma na grade para revisar a semana completa.
        </p>
      </div>
    );
  }

  const aulasHoje = horariosDoDia(horarios, dia);

  return (
    <div className="space-y-3">
      <div>
        <p className="text-sm font-semibold text-brand-blue">{labelDiaSemana(dia)}</p>
        <p className="text-xs capitalize text-slate-500">{hoje}</p>
        {turmaNome ? <p className="mt-1 text-sm text-slate-600">Turma: {turmaNome}</p> : null}
      </div>

      {aulasHoje.length === 0 ? (
        <p className="text-sm text-slate-500">Nenhuma aula cadastrada para hoje nesta turma.</p>
      ) : (
        <ul className="space-y-2">
          {aulasHoje.map((h) => (
            <li
              key={h.id}
              className="rounded-lg border border-slate-100 bg-slate-50 px-3 py-2.5 text-sm"
            >
              <p className="font-semibold text-slate-900">
                {formatHora(h.horaInicio)} – {formatHora(h.horaFim)}
              </p>
              <p className="text-slate-700">{h.disciplinaNome}</p>
              {h.professorNome ? <p className="text-xs text-slate-500">{h.professorNome}</p> : null}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function HorarioDiaResumo({ horarios, dia }: { horarios: HorarioAula[]; dia: number }) {
  const aulas = horariosDoDia(horarios, dia);
  return (
    <div>
      <p className="mb-2 text-sm font-semibold text-slate-800">{DIAS_SEMANA[dia]}</p>
      {aulas.length === 0 ? (
        <p className="text-xs text-slate-400">Sem aulas</p>
      ) : (
        <ul className="space-y-1.5">
          {aulas.map((h) => (
            <li key={h.id} className="rounded border border-slate-100 bg-white px-2 py-1.5 text-xs">
              <span className="font-medium text-slate-800">
                {formatHora(h.horaInicio)} {h.disciplinaNome}
              </span>
              {h.turmaNome ? <span className="text-slate-500"> · {h.turmaNome}</span> : null}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
