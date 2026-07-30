import type { HorarioAula } from "../../types";
import { DIAS_SEMANA, formatHora } from "../../utils/horarioLabels";

type Props = {
  horarios: HorarioAula[];
  emptyMessage?: string;
};

export function HorarioGrid({ horarios, emptyMessage = "Nenhum horario cadastrado." }: Props) {
  if (horarios.length === 0) {
    return <p className="text-sm text-slate-600">{emptyMessage}</p>;
  }

  const porDia = new Map<number, HorarioAula[]>();
  horarios.forEach((h) => {
    const list = porDia.get(h.diaSemana) ?? [];
    list.push(h);
    porDia.set(h.diaSemana, list);
  });

  const dias = [1, 2, 3, 4, 5].filter((d) => porDia.has(d));

  return (
    <div className="space-y-4">
      {dias.map((dia) => (
        <section key={dia} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <h4 className="text-sm font-semibold text-slate-900">{DIAS_SEMANA[dia]}</h4>
          <ul className="mt-3 space-y-2">
            {(porDia.get(dia) ?? []).map((h) => (
              <li
                key={h.id}
                className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-slate-100 bg-slate-50 px-3 py-2 text-sm"
              >
                <span className="font-medium text-slate-800">
                  {formatHora(h.horaInicio)} – {formatHora(h.horaFim)}
                </span>
                <span className="text-slate-700">{h.disciplinaNome}</span>
                {h.professorNome ? (
                  <span className="text-xs text-slate-500">{h.professorNome}</span>
                ) : null}
              </li>
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}
