import { useEffect, useState } from "react";
import { HorarioGrid } from "../../components/horarios/HorarioGrid";
import { EmptyState, PageSkeleton } from "../../components/ui/EmptyState";
import { SectionNav } from "../../components/layout/SectionNav";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { getHorariosTurma } from "../../services/horarioService";
import { useParentFilhoStore } from "../../store/parentFilhoStore";
import type { HorarioAula } from "../../types";
import { parentNav } from "./parentNav";

export function ParentHorariosPage() {
  const filhoAtivoId = useParentFilhoStore((s) => s.filhoAtivoId);
  const filhos = useParentFilhoStore((s) => s.filhos);
  const filho = useParentFilhoStore((s) => s.filhoAtivo());
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!filhoAtivoId) {
      if (filhos.length === 0) setLoading(false);
      return;
    }
    const ativo = filhos.find((f) => f.alunoId === filhoAtivoId) ?? filho;
    if (!ativo?.turmaId) {
      setHorarios([]);
      setError("Filho sem turma vinculada. Solicite o vínculo à secretaria.");
      setLoading(false);
      return;
    }
    let cancelled = false;
    void (async () => {
      setLoading(true);
      setError(null);
      try {
        const list = await getHorariosTurma(ativo.turmaId!);
        if (!cancelled) setHorarios(list);
      } catch {
        if (!cancelled) {
          setError("Não foi possível carregar horários. Verifique a API e tente novamente.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [filhoAtivoId, filhos, filho]);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais</h2>
        <p className="mt-1 text-sm text-slate-600">Grade de horários dos seus filhos.</p>
      </div>

      <SectionNav items={parentNav} />
      <ParentFilhoSelector />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {!filhoAtivoId && !loading ? (
        <EmptyState
          title="Nenhum filho vinculado"
          description="Use a conta pai@sge.com na demo ou peça o vínculo à secretaria."
        />
      ) : null}

      {loading ? <PageSkeleton /> : !error ? <HorarioGrid horarios={horarios} /> : null}
    </div>
  );
}
