import { HorarioGrid } from "../../components/horarios/HorarioGrid";
import { useProfessorContext } from "./ProfessorContext";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";

export function ProfessorHorariosPage() {
  const { loading, error, professorId, horarios } = useProfessorContext();

  return (
    <ProfessorAlerts error={error} loading={loading}>
      {professorId ? (
        <ProfessorPanel title="Meus horarios">
          <HorarioGrid horarios={horarios} emptyMessage="Nenhum horario cadastrado para suas turmas." />
        </ProfessorPanel>
      ) : null}
    </ProfessorAlerts>
  );
}
