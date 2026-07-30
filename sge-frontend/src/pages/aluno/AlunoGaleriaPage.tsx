import { useEffect, useState } from "react";
import { GaleriaPanel } from "../../components/galeria/GaleriaPanel";
import { SectionNav } from "../../components/layout/SectionNav";
import { getMe } from "../../services/authService";
import { alunoNav } from "./alunoNav";

export function AlunoGaleriaPage() {
  const [turmaId, setTurmaId] = useState<string | undefined>();

  useEffect(() => {
    void getMe().then((me) => setTurmaId(me.turmaId ?? undefined));
  }, []);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal do Aluno</h2>
        <p className="mt-1 text-sm text-slate-600">Galeria de fotos da sua turma e da escola.</p>
      </div>
      <SectionNav items={alunoNav} />
      <GaleriaPanel canManage={false} audiencia="ALUNOS" turmaId={turmaId} />
    </div>
  );
}
