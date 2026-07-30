import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { HorarioDiaVigente } from "../../components/horarios/HorarioDiaVigente";
import { getTurmas } from "../../services/academicoService";
import { listarDisciplinas, listarProfessores } from "../../services/academicoEstruturaService";
import { criarHorario, getHorariosTurma } from "../../services/horarioService";
import type { DisciplinaCadastro, HorarioAula, ProfessorCadastro, Turma } from "../../types";
import { apiErrorMessage } from "../../utils/apiError";
import { DIAS_SEMANA } from "../../utils/horarioLabels";
import { HorarioPanel, HorariosAlerts } from "./HorariosPageShell";

export function SecretariaHorariosGradePage() {
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [disciplinas, setDisciplinas] = useState<DisciplinaCadastro[]>([]);
  const [professores, setProfessores] = useState<ProfessorCadastro[]>([]);
  const [turmaId, setTurmaId] = useState("");
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [diaSemana, setDiaSemana] = useState("1");
  const [horaInicio, setHoraInicio] = useState("08:00");
  const [horaFim, setHoraFim] = useState("09:00");
  const [disciplinaId, setDisciplinaId] = useState("");
  const [professorId, setProfessorId] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const turmaSelecionada = turmas.find((t) => t.id === turmaId);

  const loadHorarios = useCallback(async (tid: string) => {
    if (!tid) return;
    setHorarios(await getHorariosTurma(tid));
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [t, d, p] = await Promise.all([getTurmas(), listarDisciplinas(), listarProfessores()]);
      setTurmas(t);
      setDisciplinas(d);
      setProfessores(p);
      if (t.length > 0) {
        setTurmaId(t[0].id);
        await loadHorarios(t[0].id);
      }
      if (d.length > 0) setDisciplinaId(d[0].id);
      if (p.length > 0) setProfessorId(p[0].id);
    } catch {
      setError("Falha ao carregar dados.");
    } finally {
      setLoading(false);
    }
  }, [loadHorarios]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    if (!turmaId) return;
    void loadHorarios(turmaId).catch(() => setError("Falha ao carregar horarios da turma."));
  }, [turmaId, loadHorarios]);

  async function handleCriar(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await criarHorario({
        turmaId,
        diaSemana: Number(diaSemana),
        horaInicio,
        horaFim,
        disciplinaId,
        professorId: professorId || null,
      });
      setSuccess("Horario adicionado.");
      await loadHorarios(turmaId);
    } catch (err) {
      setError(apiErrorMessage(err, "Falha ao criar horario."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <HorariosAlerts error={error} success={success} loading={loading}>
      <div className="grid gap-6 xl:grid-cols-2">
        <HorarioPanel title="Vincular horarios a turma">
          <form onSubmit={(e) => void handleCriar(e)} className="space-y-3">
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Turma</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={turmaId}
                onChange={(e) => setTurmaId(e.target.value)}
              >
                {turmas.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nome} — {t.serieNome}
                  </option>
                ))}
              </select>
            </label>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Dia</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={diaSemana}
                  onChange={(e) => setDiaSemana(e.target.value)}
                >
                  {Object.entries(DIAS_SEMANA).map(([v, label]) => (
                    <option key={v} value={v}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Disciplina</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={disciplinaId}
                  onChange={(e) => setDisciplinaId(e.target.value)}
                >
                  {disciplinas.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.nome}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input label="Inicio" type="time" value={horaInicio} onChange={(e) => setHoraInicio(e.target.value)} />
              <Input label="Fim" type="time" value={horaFim} onChange={(e) => setHoraFim(e.target.value)} />
            </div>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Professor</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={professorId}
                onChange={(e) => setProfessorId(e.target.value)}
              >
                <option value="">—</option>
                {professores.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nome}
                  </option>
                ))}
              </select>
            </label>
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando..." : "Adicionar horario"}
            </Button>
          </form>
        </HorarioPanel>

        <HorarioPanel title="Aulas de hoje">
          <HorarioDiaVigente horarios={horarios} turmaNome={turmaSelecionada?.nome} />
        </HorarioPanel>
      </div>
    </HorariosAlerts>
  );
}
