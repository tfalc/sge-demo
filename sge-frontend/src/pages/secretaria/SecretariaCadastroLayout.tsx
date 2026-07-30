import { Outlet } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { useGestaoArea } from "./useGestaoArea";

export function SecretariaCadastroLayout() {
  const { areaLabel, primaryNav, cadastroNav } = useGestaoArea();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Cadastro</h2>
        <p className="mt-1 text-sm text-slate-600">
          Dados da escola (instancia unica), alunos e responsaveis. Pais tambem podem cadastrar filhos pelo portal.
        </p>
      </div>

      <SectionNav items={primaryNav} />
      <SectionNav items={cadastroNav} />

      <Outlet />
    </div>
  );
}
