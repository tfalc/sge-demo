import { Link } from "react-router-dom";
import { HorarioGrid } from "../../components/horarios/HorarioGrid";
import { useProfessorContext } from "./ProfessorContext";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";

export function ProfessorInicioPage() {
  const { loading, error, professorId, professorNome, turmas, horarios } = useProfessorContext();

  return (
    <ProfessorAlerts error={error} loading={loading}>
      {professorId ? (
        <div className="space-y-4">
          <p className="text-sm text-slate-600">
            {professorNome ? `Ola, ${professorNome}. ` : ""}
            Escolha uma funcao no menu acima para registrar aulas, notas ou frequencia.
          </p>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <Link
              to="/professor/diario"
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-brand-blue hover:shadow"
            >
              <p className="font-semibold text-slate-900">Diario de classe</p>
              <p className="mt-1 text-sm text-slate-600">Visao integrada: atas, notas, faltas e ocorrencias.</p>
            </Link>
            <Link
              to="/professor/ata"
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-brand-blue hover:shadow"
            >
              <p className="font-semibold text-slate-900">Ata de aula</p>
              <p className="mt-1 text-sm text-slate-600">Conteudo ministrado, tarefa e chamada do dia.</p>
            </Link>
            <Link
              to="/professor/notas"
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-brand-blue hover:shadow"
            >
              <p className="font-semibold text-slate-900">Notas</p>
              <p className="mt-1 text-sm text-slate-600">Lancamento por turma e avaliacao.</p>
            </Link>
            <Link
              to="/professor/frequencia"
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-brand-blue hover:shadow"
            >
              <p className="font-semibold text-slate-900">Frequencia</p>
              <p className="mt-1 text-sm text-slate-600">Registro rapido de presencas.</p>
            </Link>
            <Link
              to="/professor/horarios"
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-brand-blue hover:shadow"
            >
              <p className="font-semibold text-slate-900">Horarios</p>
              <p className="mt-1 text-sm text-slate-600">Sua grade semanal de aulas.</p>
            </Link>
          </div>

          <ProfessorPanel title={`Minhas turmas (${turmas.length})`}>
            {turmas.length === 0 ? (
              <p className="text-sm text-slate-500">Nenhuma turma vinculada.</p>
            ) : (
              <ul className="space-y-2 text-sm">
                {turmas.map((t) => (
                  <li key={t.id} className="rounded-lg bg-slate-50 px-3 py-2 text-slate-700">
                    {t.nome} — {t.serieNome} ({t.anoLetivo})
                  </li>
                ))}
              </ul>
            )}
          </ProfessorPanel>

          <ProfessorPanel title="Proximos horarios">
            <HorarioGrid horarios={horarios.slice(0, 6)} emptyMessage="Nenhum horario cadastrado." />
          </ProfessorPanel>
        </div>
      ) : null}
    </ProfessorAlerts>
  );
}
