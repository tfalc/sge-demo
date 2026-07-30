import type { TurmaAluno } from "../../types";

type Props = {
  alunos: TurmaAluno[];
  presencasInput: Record<string, boolean>;
  justificativasInput: Record<string, string>;
  onPresencaChange: (alunoId: string, presente: boolean) => void;
  onJustificativaChange: (alunoId: string, value: string) => void;
};

export function PresencaChamadaTable({
  alunos,
  presencasInput,
  justificativasInput,
  onPresencaChange,
  onJustificativaChange,
}: Props) {
  return (
    <div className="overflow-hidden rounded-lg border border-slate-200">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Aluno</th>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Presenca</th>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Justificativa (se falta)</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {alunos.map((aluno) => {
            const presente = presencasInput[aluno.id] ?? true;
            return (
              <tr key={aluno.id}>
                <td className="px-4 py-3 font-medium">{aluno.nome}</td>
                <td className="px-4 py-3">
                  <label className="inline-flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={presente}
                      onChange={(e) => onPresencaChange(aluno.id, e.target.checked)}
                    />
                    <span className={presente ? "text-emerald-700" : "text-red-700"}>
                      {presente ? "Presente" : "Falta"}
                    </span>
                  </label>
                </td>
                <td className="px-4 py-3">
                  {!presente ? (
                    <input
                      type="text"
                      placeholder="Ex.: atestado medico"
                      className="w-full rounded border border-slate-300 px-2 py-1 text-sm"
                      value={justificativasInput[aluno.id] ?? ""}
                      onChange={(e) => onJustificativaChange(aluno.id, e.target.value)}
                    />
                  ) : (
                    <span className="text-slate-400">—</span>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
