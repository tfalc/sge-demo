import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { getMe } from "../../services/authService";
import { listarAlunos } from "../../services/cadastroService";
import { criarAgendamento, getAgendaProfissional } from "../../services/saudeService";
import type { AgendamentoSaude, AlunoCadastro } from "../../types";
import { formatDateTime } from "../../utils/dateRange";

export function PsicologiaPage() {
  const [profissionalId, setProfissionalId] = useState<string | null>(null);
  const [agenda, setAgenda] = useState<AgendamentoSaude[]>([]);
  const [alunos, setAlunos] = useState<AlunoCadastro[]>([]);
  const [alunoId, setAlunoId] = useState("");
  const [data, setData] = useState("");
  const [hora, setHora] = useState("14:00");
  const [observacoes, setObservacoes] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const loadAgenda = useCallback(async (profId: string) => {
    const items = await getAgendaProfissional(profId);
    setAgenda(items);
  }, []);

  useEffect(() => {
    void (async () => {
      try {
        const me = await getMe();
        if (!me.profissionalSaudeId) {
          setError("Use psico@sge.com para testar este portal.");
          return;
        }
        setProfissionalId(me.profissionalSaudeId);
        const [a, al] = await Promise.all([
          getAgendaProfissional(me.profissionalSaudeId),
          listarAlunos(),
        ]);
        setAgenda(a);
        setAlunos(al);
        if (al.length > 0) setAlunoId(al[0].id);
      } catch {
        setError("Falha ao carregar agenda ou lista de alunos.");
      } finally {
        setLoading(false);
      }
    })();
  }, [loadAgenda]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!profissionalId || !data || !alunoId) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const dataHora = new Date(`${data}T${hora}:00`).toISOString();
      await criarAgendamento({ alunoId, dataHora, observacoes, privado: true });
      setSuccess("Agendamento criado.");
      setObservacoes("");
      await loadAgenda(profissionalId);
    } catch {
      setError("Falha ao criar agendamento.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Psicologia Escolar</h2>
        <p className="mt-1 text-sm text-slate-600">Agenda e atendimentos (observacoes privadas para a equipe).</p>
      </div>

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Novo agendamento</h3>
            <form className="mt-4 space-y-3" onSubmit={(e) => void handleSubmit(e)}>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Aluno</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={alunoId}
                  onChange={(e) => setAlunoId(e.target.value)}
                >
                  {alunos.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nome} ({a.turmaNome})
                    </option>
                  ))}
                </select>
              </label>
              <div className="grid gap-3 sm:grid-cols-2">
                <Input label="Data" type="date" value={data} onChange={(e) => setData(e.target.value)} required />
                <Input label="Horario" type="time" value={hora} onChange={(e) => setHora(e.target.value)} />
              </div>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Observacoes (privadas)</span>
                <textarea
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  rows={3}
                  value={observacoes}
                  onChange={(e) => setObservacoes(e.target.value)}
                />
              </label>
              <Button type="submit" disabled={saving}>
                {saving ? "Salvando..." : "Agendar"}
              </Button>
            </form>
          </section>

          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Agenda ({agenda.length})</h3>
            <div className="mt-4 space-y-3">
              {agenda.map((a) => (
                <div key={a.id} className="rounded-lg border border-slate-100 bg-slate-50 p-3 text-sm">
                  <p className="font-medium text-slate-900">{a.alunoNome}</p>
                  <p className="text-slate-600">{formatDateTime(a.dataHora)} — {a.status}</p>
                  {a.observacoes ? (
                    <p className="mt-1 text-xs text-slate-500">{a.observacoes}</p>
                  ) : null}
                </div>
              ))}
            </div>
          </section>
        </div>
      )}
    </div>
  );
}
