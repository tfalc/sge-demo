import { useCallback, useEffect, useState } from "react";
import { AgendaCalendar } from "../../components/calendar/AgendaCalendar";
import { getAgenda } from "../../services/comunicacaoService";
import type { AgendaEvent } from "../../types";
import { agendaRangeIso } from "../../utils/dateRange";
import { ComunicacaoAlerts, ComunicacaoPanel } from "./ComunicacaoPageShell";

export function SecretariaComunicacaoCalendarioPage() {
  const [eventos, setEventos] = useState<AgendaEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const range = agendaRangeIso(3);
      setEventos(await getAgenda(range));
    } catch {
      setError("Nao foi possivel carregar o calendario.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <ComunicacaoAlerts error={error} loading={loading}>
      <div className="grid items-start gap-6 xl:grid-cols-3">
        <ComunicacaoPanel title="Calendario de eventos">
          <p className="text-xs text-slate-500">
            {eventos.length} evento(s) no periodo. Clique em um dia para detalhes.
          </p>
          <div className="mt-4">
            {eventos.length === 0 ? (
              <p className="text-sm text-slate-600">Cadastre um evento na aba Eventos para preencher o calendario.</p>
            ) : (
              <AgendaCalendar events={eventos} />
            )}
          </div>
        </ComunicacaoPanel>

        <ComunicacaoPanel title="Legenda">
          <ul className="space-y-2 text-sm text-slate-600">
            <li className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-sky-600" /> Reuniao
            </li>
            <li className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-amber-500" /> Prova
            </li>
            <li className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-rose-500" /> Feriado
            </li>
            <li className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-emerald-600" /> Evento
            </li>
          </ul>
          <p className="mt-4 text-xs text-slate-500">
            Os eventos cadastrados na aba Eventos aparecem aqui no calendario mensal.
          </p>
        </ComunicacaoPanel>
      </div>
    </ComunicacaoAlerts>
  );
}
