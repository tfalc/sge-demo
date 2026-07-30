import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import { getTurmas } from "../../services/academicoService";
import {
  createAgendaEvent,
  deleteAgendaEvent,
  getAgenda,
  updateAgendaEvent,
} from "../../services/comunicacaoService";
import type { AgendaEvent, TipoEvento, Turma } from "../../types";
import { agendaRangeIso, formatDateTime } from "../../utils/dateRange";
import { ComunicacaoAlerts, ComunicacaoPanel } from "./ComunicacaoPageShell";

export function SecretariaComunicacaoEventosPage() {
  const [eventos, setEventos] = useState<AgendaEvent[]>([]);
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [eventoTitulo, setEventoTitulo] = useState("");
  const [eventoDescricao, setEventoDescricao] = useState("");
  const [eventoData, setEventoData] = useState("");
  const [eventoHora, setEventoHora] = useState("19:00");
  const [eventoTipo, setEventoTipo] = useState<TipoEvento>("REUNIAO");
  const [eventoTurmaId, setEventoTurmaId] = useState("");
  const [editEventoId, setEditEventoId] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const range = agendaRangeIso(3);
      const [e, t] = await Promise.all([getAgenda(range), getTurmas()]);
      setEventos(e);
      setTurmas(t);
    } catch {
      setError("Nao foi possivel carregar eventos.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!eventoData) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const dataInicio = new Date(`${eventoData}T${eventoHora}:00`).toISOString();
      if (editEventoId) {
        await updateAgendaEvent(editEventoId, {
          titulo: eventoTitulo,
          descricao: eventoDescricao || undefined,
          dataInicio,
          tipo: eventoTipo,
          turmaId: eventoTurmaId || null,
        });
        setEditEventoId(null);
        setSuccess("Evento atualizado.");
      } else {
        await createAgendaEvent({
          titulo: eventoTitulo,
          descricao: eventoDescricao || undefined,
          dataInicio,
          tipo: eventoTipo,
          turmaId: eventoTurmaId || null,
        });
        setSuccess("Evento cadastrado na agenda.");
      }
      setEventoTitulo("");
      setEventoDescricao("");
      setEventoData("");
      setEventoTurmaId("");
      await load();
    } catch {
      setError(editEventoId ? "Falha ao atualizar evento." : "Falha ao cadastrar evento.");
    } finally {
      setSaving(false);
    }
  }

  function startEdit(ev: AgendaEvent) {
    const date = new Date(ev.dataInicio);
    setEditEventoId(ev.id);
    setEventoTitulo(ev.titulo);
    setEventoDescricao(ev.descricao ?? "");
    setEventoData(date.toISOString().slice(0, 10));
    setEventoHora(date.toTimeString().slice(0, 5));
    setEventoTipo(ev.tipo ?? "EVENTO");
    setEventoTurmaId(ev.turmaId ?? "");
  }

  function cancelEdicao() {
    setEditEventoId(null);
    setEventoTitulo("");
    setEventoDescricao("");
    setEventoData("");
    setEventoTurmaId("");
  }

  return (
    <ComunicacaoAlerts error={error} success={success} loading={loading}>
      <div className="grid items-start gap-6 xl:grid-cols-2">
        <ComunicacaoPanel title={editEventoId ? "Editar evento" : "Novo evento na agenda"}>
          <form className="space-y-3" onSubmit={(e) => void handleSubmit(e)}>
            <Input
              label="Titulo"
              value={eventoTitulo}
              onChange={(e) => setEventoTitulo(e.target.value)}
              required
            />
            <Input
              label="Descricao"
              value={eventoDescricao}
              onChange={(e) => setEventoDescricao(e.target.value)}
            />
            <div className="grid gap-3 sm:grid-cols-2">
              <Input
                label="Data"
                type="date"
                value={eventoData}
                onChange={(e) => setEventoData(e.target.value)}
                required
              />
              <Input
                label="Horario"
                type="time"
                value={eventoHora}
                onChange={(e) => setEventoHora(e.target.value)}
              />
            </div>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Tipo</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={eventoTipo}
                onChange={(e) => setEventoTipo(e.target.value as TipoEvento)}
              >
                <option value="REUNIAO">Reuniao</option>
                <option value="FERIADO">Feriado</option>
                <option value="PROVA">Prova</option>
                <option value="EVENTO">Evento</option>
              </select>
            </label>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Turma (opcional)</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={eventoTurmaId}
                onChange={(e) => setEventoTurmaId(e.target.value)}
              >
                <option value="">Escola toda</option>
                {turmas.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nome}
                  </option>
                ))}
              </select>
            </label>
            <div className="flex flex-wrap items-center gap-2">
              <Button type="submit" disabled={saving}>
                {saving ? "Salvando..." : editEventoId ? "Salvar alteracoes" : "Cadastrar evento"}
              </Button>
              {editEventoId ? (
                <ActionButton type="button" variant="neutral" onClick={cancelEdicao}>
                  Cancelar edicao
                </ActionButton>
              ) : null}
            </div>
          </form>
        </ComunicacaoPanel>

        <ComunicacaoPanel title={`Eventos cadastrados (${eventos.length})`}>
          {eventos.length === 0 ? (
            <p className="text-sm text-slate-600">Nenhum evento na agenda.</p>
          ) : (
            <ul className="max-h-[32rem] space-y-2 overflow-y-auto text-sm">
              {eventos.map((ev) => (
                <li
                  key={ev.id}
                  className="flex items-center justify-between gap-2 rounded-lg border border-slate-100 bg-slate-50 px-3 py-2"
                >
                  <span className="min-w-0">
                    <span className="font-medium text-slate-900">{ev.titulo}</span>
                    <span className="block text-xs text-slate-500">
                      {ev.tipo ?? "EVENTO"} · {formatDateTime(ev.dataInicio)}
                    </span>
                  </span>
                  <span className="flex shrink-0 gap-2">
                    <ActionButton type="button" onClick={() => startEdit(ev)}>
                      Editar
                    </ActionButton>
                    <ActionButton
                      type="button"
                      variant="danger"
                      onClick={() => {
                        void (async () => {
                          await deleteAgendaEvent(ev.id);
                          await load();
                        })();
                      }}
                    >
                      Excluir
                    </ActionButton>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </ComunicacaoPanel>
      </div>
    </ComunicacaoAlerts>
  );
}
