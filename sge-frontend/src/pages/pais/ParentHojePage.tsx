import { HojeInboxPanel } from "../../components/inbox/HojeInboxPanel";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { SectionNav } from "../../components/layout/SectionNav";
import { parentNav } from "./parentNav";

export function ParentHojePage() {
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais</h2>
        <p className="mt-1 text-sm text-slate-600">
          Pendências, cobranças e avisos que precisam da sua atenção.
        </p>
      </div>
      <SectionNav items={parentNav} />
      <ParentFilhoSelector />
      <HojeInboxPanel homePath="/pais/hoje" />
    </div>
  );
}
