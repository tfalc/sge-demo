import { HojeInboxPanel } from "../../components/inbox/HojeInboxPanel";
import { SectionNav } from "../../components/layout/SectionNav";
import { useGestaoArea } from "../secretaria/useGestaoArea";

export function GestaoHojePage() {
  const { areaLabel, primaryNav, basePath } = useGestaoArea();
  const homePath = basePath === "/direcao" ? "/direcao" : "/secretaria/matricula-nova";
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel}</h2>
        <p className="mt-1 text-sm text-slate-600">Pendências operacionais e avisos da escola.</p>
      </div>
      <SectionNav items={primaryNav} />
      <HojeInboxPanel homePath={homePath} />
    </div>
  );
}
