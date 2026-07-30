import { useProfessorContext } from "./ProfessorContext";

export function ProfessorTurmaSelectors({ showDisciplina = true }: { showDisciplina?: boolean }) {
  const { turmas, turmaId, setTurmaId, disciplinas, tdpId, setTdpId } = useProfessorContext();

  return (
    <div className="grid gap-4 md:grid-cols-2">
      <label className="block text-sm">
        <span className="mb-1 block font-medium text-slate-700">Turma</span>
        <select
          className="w-full rounded-lg border border-slate-300 px-3 py-2"
          value={turmaId}
          onChange={(e) => setTurmaId(e.target.value)}
        >
          {turmas.map((t) => (
            <option key={t.id} value={t.id}>
              {t.nome} — {t.serieNome} ({t.anoLetivo})
            </option>
          ))}
        </select>
      </label>

      {showDisciplina ? (
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Disciplina</span>
          <select
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
            value={tdpId}
            onChange={(e) => setTdpId(e.target.value)}
          >
            {disciplinas.map((d) => (
              <option key={d.id} value={d.id}>
                {d.disciplinaNome}
              </option>
            ))}
          </select>
        </label>
      ) : null}
    </div>
  );
}
