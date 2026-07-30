import { Outlet } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { useGestaoArea } from "./useGestaoArea";

export function SecretariaHorariosLayout() {
  const { areaLabel, primaryNav, horariosNav } = useGestaoArea();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Horarios</h2>
        <p className="mt-1 text-sm text-slate-600">
          Grade por turma, visao do dia, filtros por materia e professor, e calendario semanal.
        </p>
      </div>

      <SectionNav items={primaryNav} />
      <SectionNav items={horariosNav} />

      <Outlet />
    </div>
  );
}
