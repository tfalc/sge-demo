import type { HorarioAula } from "../types";
import { DIAS_SEMANA } from "./horarioLabels";

/** Segunda=1 … Sexta=5 (alinhado ao backend). Fim de semana retorna null. */
export function diaSemanaVigente(): number | null {
  const jsDay = new Date().getDay();
  if (jsDay >= 1 && jsDay <= 5) return jsDay;
  return null;
}

export function labelDiaSemana(dia: number): string {
  return DIAS_SEMANA[dia] ?? `Dia ${dia}`;
}

export function horariosDoDia(horarios: HorarioAula[], dia: number): HorarioAula[] {
  return horarios
    .filter((h) => h.diaSemana === dia)
    .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));
}

export function horariosDaDisciplina(horarios: HorarioAula[], disciplinaId: string): HorarioAula[] {
  return horarios
    .filter((h) => h.disciplinaId === disciplinaId)
    .sort((a, b) => a.diaSemana - b.diaSemana || a.horaInicio.localeCompare(b.horaInicio));
}

export function timeToMinutes(time: string): number {
  const [h, m] = time.split(":").map(Number);
  return h * 60 + (m ?? 0);
}

export function sortHorarios(horarios: HorarioAula[]): HorarioAula[] {
  return [...horarios].sort(
    (a, b) => a.diaSemana - b.diaSemana || a.horaInicio.localeCompare(b.horaInicio),
  );
}
