import { useCallback, useEffect, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { AgendaCalendar } from "../../components/calendar/AgendaCalendar";
import { getMe } from "../../services/authService";
import { getAgenda, getCardapio, getComunicados } from "../../services/comunicacaoService";
import type { AgendaEvent, CardapioItem, Comunicado } from "../../types";
import { agendaRangeIso, formatDateTime, todayIso } from "../../utils/dateRange";
import { alunoNav } from "./alunoNav";

const tipoRefeicaoLabel: Record<string, string> = {
  ALMOCO: "Almoco",
  LANCHE: "Lanche",
};

export function AlunoCommunicationPage() {
  const [comunicados, setComunicados] = useState<Comunicado[]>([]);
  const [cardapio, setCardapio] = useState<CardapioItem[]>([]);
  const [eventos, setEventos] = useState<AgendaEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const me = await getMe();
      const turmaId = me.turmaId ?? undefined;
      const range = agendaRangeIso();
      const [c, card, ev] = await Promise.all([
        getComunicados({ audiencia: "PAIS", turmaId }),
        getCardapio(todayIso()),
        getAgenda({ ...range, turmaId }),
      ]);
      setComunicados(c);
      setCardapio(card);
      setEventos(ev);
    } catch {
      setError("Nao foi possivel carregar comunicacao.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal do Aluno</h2>
        <p className="mt-1 text-sm text-slate-600">Comunicados, cardapio e agenda da turma.</p>
      </div>

      <SectionNav items={alunoNav} />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : (
        <>
          <section className="space-y-3">
            <h3 className="text-base font-semibold text-slate-900">Comunicados</h3>
            {comunicados.length === 0 ? (
              <p className="text-sm text-slate-600">Nenhum comunicado no momento.</p>
            ) : (
              <div className="space-y-3">
                {comunicados.map((c) => (
                  <article
                    key={c.id}
                    className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
                  >
                    <div className="flex flex-wrap items-start justify-between gap-2">
                      <h4 className="font-semibold text-slate-900">{c.titulo}</h4>
                      <span className="text-xs text-slate-500">{formatDateTime(c.publicadoEm)}</span>
                    </div>
                    <p className="mt-2 whitespace-pre-wrap text-sm text-slate-700">{c.conteudo}</p>
                  </article>
                ))}
              </div>
            )}
          </section>

          <section className="space-y-3">
            <h3 className="text-base font-semibold text-slate-900">Cardapio de hoje</h3>
            {cardapio.length === 0 ? (
              <p className="text-sm text-slate-600">Cardapio nao publicado para hoje.</p>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2">
                {cardapio.map((item) => (
                  <div key={item.id} className="rounded-xl border border-emerald-200 bg-emerald-50 p-4">
                    <div className="text-sm font-semibold text-emerald-900">
                      {tipoRefeicaoLabel[item.tipoRefeicao] ?? item.tipoRefeicao}
                    </div>
                    <p className="mt-2 text-sm text-slate-800">{item.descricao}</p>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="space-y-3">
            <h3 className="text-base font-semibold text-slate-900">Agenda escolar</h3>
            {eventos.length === 0 ? (
              <p className="text-sm text-slate-600">Nenhum evento nos proximos meses.</p>
            ) : (
              <AgendaCalendar events={eventos} />
            )}
          </section>
        </>
      )}
    </div>
  );
}
