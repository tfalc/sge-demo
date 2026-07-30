import { useCallback, useEffect, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { getAgenda, getCardapio, getComunicados } from "../../services/comunicacaoService";
import { useParentFilhoStore } from "../../store/parentFilhoStore";
import type { AgendaEvent, CardapioItem, Comunicado } from "../../types";
import { AgendaCalendar } from "../../components/calendar/AgendaCalendar";
import { agendaRangeIso, formatDateTime, todayIso } from "../../utils/dateRange";
import { parentNav } from "./parentNav";

const tipoEventoLabel: Record<string, string> = {
  REUNIAO: "Reunião",
  FERIADO: "Feriado",
  PROVA: "Prova",
  EVENTO: "Evento",
};

const tipoRefeicaoLabel: Record<string, string> = {
  ALMOCO: "Almoço",
  LANCHE: "Lanche",
};

export function ParentCommunicationPage() {
  const filhoAtivoId = useParentFilhoStore((s) => s.filhoAtivoId);
  const filhos = useParentFilhoStore((s) => s.filhos);
  const [comunicados, setComunicados] = useState<Comunicado[]>([]);
  const [cardapio, setCardapio] = useState<CardapioItem[]>([]);
  const [eventos, setEventos] = useState<AgendaEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const filho = filhos.find((f) => f.alunoId === filhoAtivoId) ?? filhos[0];
      const turmaId = filho?.turmaId ?? undefined;
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
      setError("Não foi possível carregar a comunicação. Verifique login e backend.");
    } finally {
      setLoading(false);
    }
  }, [filhoAtivoId, filhos]);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais</h2>
        <p className="mt-1 text-sm text-slate-600">Comunicados, cardápio e agenda escolar.</p>
      </div>

      <SectionNav items={parentNav} />
      <ParentFilhoSelector />

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
                      <span className="text-xs text-slate-500">
                        {formatDateTime(c.publicadoEm)}
                        {c.turmaNome ? ` · ${c.turmaNome}` : " · Escola toda"}
                      </span>
                    </div>
                    <p className="mt-2 whitespace-pre-wrap text-sm text-slate-700">{c.conteudo}</p>
                    {c.publicadoPorNome ? (
                      <p className="mt-2 text-xs text-slate-500">Por {c.publicadoPorNome}</p>
                    ) : null}
                  </article>
                ))}
              </div>
            )}
          </section>

          <section className="space-y-3">
            <h3 className="text-base font-semibold text-slate-900">Cardápio de hoje</h3>
            {cardapio.length === 0 ? (
              <p className="text-sm text-slate-600">Cardápio não publicado para hoje.</p>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2">
                {cardapio.map((item) => (
                  <div
                    key={item.id}
                    className="rounded-xl border border-emerald-200 bg-emerald-50 p-4"
                  >
                    <div className="text-sm font-semibold text-emerald-900">
                      {tipoRefeicaoLabel[item.tipoRefeicao] ?? item.tipoRefeicao}
                    </div>
                    <p className="mt-2 text-sm text-slate-800">{item.descricao}</p>
                    {item.calorias ? (
                      <p className="mt-2 text-xs text-slate-600">{item.calorias} kcal</p>
                    ) : null}
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="space-y-3">
            <h3 className="text-base font-semibold text-slate-900">Agenda escolar</h3>
            {eventos.length === 0 ? (
              <p className="text-sm text-slate-600">Nenhum evento nos próximos meses.</p>
            ) : (
              <AgendaCalendar events={eventos} />
            )}
          </section>
        </>
      )}
    </div>
  );
}
