export const DIAS_SEMANA: Record<number, string> = {
  1: "Segunda",
  2: "Terca",
  3: "Quarta",
  4: "Quinta",
  5: "Sexta",
};

export function formatHora(hora: string): string {
  return hora.slice(0, 5);
}
