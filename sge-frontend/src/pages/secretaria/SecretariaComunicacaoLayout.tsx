import { Outlet } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { useGestaoArea } from "./useGestaoArea";

export function SecretariaComunicacaoLayout() {
  const { areaLabel, primaryNav, comunicacaoNav } = useGestaoArea();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Comunicacao</h2>
        <p className="mt-1 text-sm text-slate-600">
          Publicar comunicados, cadastrar eventos na agenda e consultar o calendario escolar.
        </p>
      </div>

      <SectionNav items={primaryNav} />
      <SectionNav items={comunicacaoNav} />

      <Outlet />
    </div>
  );
}
