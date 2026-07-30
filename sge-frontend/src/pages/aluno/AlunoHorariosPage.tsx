import { useCallback, useEffect, useState } from "react";
import { HorarioGrid } from "../../components/horarios/HorarioGrid";
import { SectionNav } from "../../components/layout/SectionNav";
import { getMe } from "../../services/authService";
import { getHorariosTurma } from "../../services/horarioService";
import type { HorarioAula } from "../../types";
import { alunoNav } from "./alunoNav";

export function AlunoHorariosPage() {
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [turmaNome, setTurmaNome] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const me = await getMe();
      if (!me.turmaId) {
        setError("Aluno sem turma vinculada.");
        return;
      }
      setTurmaNome(me.turmaNome);
      const list = await getHorariosTurma(me.turmaId);
      setHorarios(list);
    } catch {
      setError("Nao foi possivel carregar horarios.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal do Aluno</h2>
        <p className="mt-1 text-sm text-slate-600">
          Grade de horarios{turmaNome ? ` — turma ${turmaNome}` : ""}.
        </p>
      </div>

      <SectionNav items={alunoNav} />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : (
        <HorarioGrid horarios={horarios} />
      )}
    </div>
  );
}
