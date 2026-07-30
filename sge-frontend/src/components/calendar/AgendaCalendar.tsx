import { useMemo, useState } from "react";
import type { AgendaEvent } from "../../types";

const WEEKDAYS = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab"];

const tipoColors: Record<string, string> = {
  PROVA: "bg-amber-500",
  REUNIAO: "bg-sky-600",
  FERIADO: "bg-rose-500",
  EVENTO: "bg-emerald-600",
};

function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function addMonths(date: Date, delta: number): Date {
  return new Date(date.getFullYear(), date.getMonth() + delta, 1);
}

function sameDay(a: Date, b: Date): boolean {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

function parseEventDate(iso: string): Date {
  return new Date(iso);
}

interface AgendaCalendarProps {
  events: AgendaEvent[];
}

export function AgendaCalendar({ events }: AgendaCalendarProps) {
  const [cursor, setCursor] = useState(() => startOfMonth(new Date()));
  const [selectedDay, setSelectedDay] = useState<Date | null>(null);

  const monthLabel = cursor.toLocaleDateString("pt-BR", { month: "long", year: "numeric" });

  const cells = useMemo(() => {
    const year = cursor.getFullYear();
    const month = cursor.getMonth();
    const firstWeekday = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const grid: (Date | null)[] = [];
    for (let i = 0; i < firstWeekday; i++) grid.push(null);
    for (let day = 1; day <= daysInMonth; day++) grid.push(new Date(year, month, day));
    while (grid.length % 7 !== 0) grid.push(null);
    return grid;
  }, [cursor]);

  const eventsByDay = useMemo(() => {
    const map = new Map<string, AgendaEvent[]>();
    for (const event of events) {
      const d = parseEventDate(event.dataInicio);
      const key = `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
      const list = map.get(key) ?? [];
      list.push(event);
      map.set(key, list);
    }
    return map;
  }, [events]);

  const selectedEvents =
    selectedDay == null
      ? []
      : (eventsByDay.get(
          `${selectedDay.getFullYear()}-${selectedDay.getMonth()}-${selectedDay.getDate()}`,
        ) ?? []);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <button
          type="button"
          className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
          onClick={() => setCursor((c) => addMonths(c, -1))}
        >
          Anterior
        </button>
        <h4 className="text-sm font-semibold capitalize text-slate-800">{monthLabel}</h4>
        <button
          type="button"
          className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
          onClick={() => setCursor((c) => addMonths(c, 1))}
        >
          Proximo
        </button>
      </div>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="grid grid-cols-7 border-b border-slate-100 bg-slate-50 text-center text-xs font-semibold text-slate-600">
          {WEEKDAYS.map((d) => (
            <div key={d} className="px-2 py-2">
              {d}
            </div>
          ))}
        </div>
        <div className="grid grid-cols-7">
          {cells.map((day, idx) => {
            if (!day) {
              return <div key={`empty-${idx}`} className="min-h-[4.5rem] border-b border-r border-slate-100 bg-slate-50/40" />;
            }
            const key = `${day.getFullYear()}-${day.getMonth()}-${day.getDate()}`;
            const dayEvents = eventsByDay.get(key) ?? [];
            const isSelected = selectedDay != null && sameDay(day, selectedDay);
            const isToday = sameDay(day, new Date());
            return (
              <button
                key={key}
                type="button"
                onClick={() => setSelectedDay(day)}
                className={[
                  "min-h-[4.5rem] border-b border-r border-slate-100 p-1.5 text-left transition-colors",
                  isSelected ? "bg-sky-50" : "hover:bg-slate-50",
                ].join(" ")}
              >
                <span
                  className={[
                    "inline-flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold",
                    isToday ? "bg-[#0c2d57] text-white" : "text-slate-700",
                  ].join(" ")}
                >
                  {day.getDate()}
                </span>
                <div className="mt-1 space-y-0.5">
                  {dayEvents.slice(0, 2).map((e) => (
                    <div
                      key={e.id}
                      className={[
                        "truncate rounded px-1 py-0.5 text-[10px] font-medium text-white",
                        tipoColors[e.tipo ?? "EVENTO"] ?? "bg-slate-500",
                      ].join(" ")}
                      title={e.titulo}
                    >
                      {e.titulo}
                    </div>
                  ))}
                  {dayEvents.length > 2 ? (
                    <div className="text-[10px] text-slate-500">+{dayEvents.length - 2}</div>
                  ) : null}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {selectedDay ? (
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <h4 className="text-sm font-semibold text-slate-900">
            Eventos em {selectedDay.toLocaleDateString("pt-BR")}
          </h4>
          {selectedEvents.length === 0 ? (
            <p className="mt-2 text-sm text-slate-600">Nenhum evento neste dia.</p>
          ) : (
            <ul className="mt-3 space-y-2">
              {selectedEvents.map((e) => (
                <li key={e.id} className="rounded-lg bg-slate-50 px-3 py-2 text-sm">
                  <div className="font-medium text-slate-900">{e.titulo}</div>
                  {e.descricao ? <div className="text-slate-600">{e.descricao}</div> : null}
                  <div className="mt-1 text-xs text-slate-500">
                    {e.tipo ?? "Evento"}
                    {e.turmaNome ? ` · ${e.turmaNome}` : " · Escola toda"}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      ) : null}
    </div>
  );
}
