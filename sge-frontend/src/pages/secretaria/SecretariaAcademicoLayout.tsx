import { Outlet } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { useGestaoArea } from "./useGestaoArea";

export function SecretariaAcademicoLayout() {
  const { areaLabel, primaryNav, academicoNav } = useGestaoArea();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Academico</h2>
        <p className="mt-1 text-sm text-slate-600">
          Cadastre disciplinas, professores, turmas e vinculos — cada area em sua propria tela.
        </p>
      </div>

      <SectionNav items={primaryNav} />
      <SectionNav items={academicoNav} />

      <Outlet />
    </div>
  );
}
