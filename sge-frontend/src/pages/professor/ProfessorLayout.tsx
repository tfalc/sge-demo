import { Outlet } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { ProfessorProvider } from "./ProfessorContext";
import { professorNav } from "./professorNav";

export function ProfessorLayout() {
  return (
    <ProfessorProvider>
      <div className="space-y-6">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Portal do Professor</h2>
          <p className="mt-1 text-sm text-slate-600">
            Diário de classe, plano de aula, lançamento de notas e frequência das suas turmas.
          </p>
        </div>

        <SectionNav items={professorNav} />

        <Outlet />
      </div>
    </ProfessorProvider>
  );
}
