import type { HorarioAula } from "../../types";
import { DIAS_SEMANA, formatHora } from "../../utils/horarioLabels";
import { sortHorarios, timeToMinutes } from "../../utils/horarioUtils";

const HOUR_START = 7;
const HOUR_END = 19;
const PX_PER_MINUTE = 1.1;

type Props = {
  horarios: HorarioAula[];
  showTurma?: boolean;
  emptyMessage?: string;
};

const COLORS = [
  "bg-sky-100 border-sky-300 text-sky-950",
  "bg-violet-100 border-violet-300 text-violet-950",
  "bg-emerald-100 border-emerald-300 text-emerald-950",
  "bg-amber-100 border-amber-300 text-amber-950",
  "bg-rose-100 border-rose-300 text-rose-950",
  "bg-indigo-100 border-indigo-300 text-indigo-950",
];

function colorForKey(key: string): string {
  let hash = 0;
  for (let i = 0; i < key.length; i++) hash = (hash + key.charCodeAt(i) * (i + 1)) % COLORS.length;
  return COLORS[hash];
}

export function HorarioWeekCalendar({
  horarios,
  showTurma = true,
  emptyMessage = "Nenhum horario para exibir no calendario.",
}: Props) {
  const sorted = sortHorarios(horarios);
  const dias = [1, 2, 3, 4, 5];
  const gridStart = HOUR_START * 60;
  const gridEnd = HOUR_END * 60;
  const bodyHeight = (gridEnd - gridStart) * PX_PER_MINUTE;

  if (sorted.length === 0) {
    return <p className="text-sm text-slate-600">{emptyMessage}</p>;
  }

  const hours: number[] = [];
  for (let h = HOUR_START; h <= HOUR_END; h++) hours.push(h);

  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
      <div className="flex min-w-[760px]">
        <div className="w-14 shrink-0 border-r border-slate-100 bg-slate-50">
          <div className="h-10 border-b border-slate-100" />
          <div className="relative" style={{ height: bodyHeight }}>
            {hours.map((h) => (
              <div
                key={h}
                className="absolute left-0 right-0 border-t border-slate-100 px-1 text-[10px] text-slate-400"
                style={{ top: (h * 60 - gridStart) * PX_PER_MINUTE }}
              >
                {String(h).padStart(2, "0")}:00
              </div>
            ))}
          </div>
        </div>

        {dias.map((dia) => {
          const aulasDia = sorted.filter((h) => h.diaSemana === dia);
          return (
            <div key={dia} className="min-w-0 flex-1 border-r border-slate-100 last:border-r-0">
              <div className="flex h-10 items-center justify-center border-b border-slate-100 bg-slate-50 text-xs font-semibold text-slate-700">
                {DIAS_SEMANA[dia]}
              </div>
              <div className="relative bg-white" style={{ height: bodyHeight }}>
                {hours.map((h) => (
                  <div
                    key={h}
                    className="absolute left-0 right-0 border-t border-slate-50"
                    style={{ top: (h * 60 - gridStart) * PX_PER_MINUTE }}
                  />
                ))}
                {aulasDia.map((h) => {
                  const top = (timeToMinutes(h.horaInicio) - gridStart) * PX_PER_MINUTE;
                  const height = Math.max(
                    28,
                    (timeToMinutes(h.horaFim) - timeToMinutes(h.horaInicio)) * PX_PER_MINUTE - 2,
                  );
                  return (
                    <div
                      key={h.id}
                      className={`absolute left-1 right-1 overflow-hidden rounded border px-1.5 py-1 text-[10px] leading-tight shadow-sm ${colorForKey(h.disciplinaId)}`}
                      style={{ top: top + 1, height }}
                      title={`${h.disciplinaNome} ${formatHora(h.horaInicio)}-${formatHora(h.horaFim)}`}
                    >
                      <p className="truncate font-semibold">{h.disciplinaNome}</p>
                      <p className="truncate opacity-80">
                        {formatHora(h.horaInicio)}–{formatHora(h.horaFim)}
                      </p>
                      {showTurma ? <p className="truncate opacity-70">{h.turmaNome}</p> : null}
                      {h.professorNome ? <p className="truncate opacity-70">{h.professorNome}</p> : null}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
