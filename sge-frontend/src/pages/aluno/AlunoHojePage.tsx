import { HojeInboxPanel } from "../../components/inbox/HojeInboxPanel";
import { SectionNav } from "../../components/layout/SectionNav";
import { alunoNav } from "./alunoNav";

export function AlunoHojePage() {
  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal do Aluno</h2>
        <p className="mt-1 text-sm text-slate-600">Avisos e o que acompanhar neste dia.</p>
      </div>
      <SectionNav items={alunoNav} />
      <HojeInboxPanel homePath="/aluno/hoje" />
    </div>
  );
}
