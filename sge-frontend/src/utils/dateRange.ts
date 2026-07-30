export function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export function agendaRangeIso(monthsAhead = 2): { inicio: string; fim: string } {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  const end = new Date(now.getFullYear(), now.getMonth() + monthsAhead, 0, 23, 59, 59);
  return { inicio: start.toISOString(), fim: end.toISOString() };
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}
