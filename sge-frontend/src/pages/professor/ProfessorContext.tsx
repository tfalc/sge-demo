import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { getMe } from "../../services/authService";
import { getHorariosProfessor } from "../../services/horarioService";
import { getPeriodos, getTurmaAlunos, getTurmaDisciplinas, getTurmas } from "../../services/academicoService";
import type { DisciplinaVinculo, HorarioAula, PeriodoAvaliacao, Turma, TurmaAluno } from "../../types";

type ProfessorContextValue = {
  loading: boolean;
  error: string | null;
  professorId: string | null;
  professorNome: string;
  turmas: Turma[];
  periodos: PeriodoAvaliacao[];
  horarios: HorarioAula[];
  turmaId: string;
  setTurmaId: (id: string) => void;
  disciplinas: DisciplinaVinculo[];
  tdpId: string;
  setTdpId: (id: string) => void;
  alunos: TurmaAluno[];
  reload: () => Promise<void>;
};

const ProfessorContext = createContext<ProfessorContextValue | null>(null);

export function ProfessorProvider({ children }: { children: ReactNode }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [professorId, setProfessorId] = useState<string | null>(null);
  const [professorNome, setProfessorNome] = useState("");
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [periodos, setPeriodos] = useState<PeriodoAvaliacao[]>([]);
  const [horarios, setHorarios] = useState<HorarioAula[]>([]);
  const [turmaId, setTurmaId] = useState("");
  const [disciplinas, setDisciplinas] = useState<DisciplinaVinculo[]>([]);
  const [tdpId, setTdpId] = useState("");
  const [alunos, setAlunos] = useState<TurmaAluno[]>([]);

  const loadContext = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const me = await getMe();
      setProfessorNome(me.nome);
      if (!me.professorId) {
        setProfessorId(null);
        setError("Este usuario nao esta vinculado a um professor. Use prof@sge.com para testar.");
        return;
      }
      setProfessorId(me.professorId);
      const [t, p, h] = await Promise.all([
        getTurmas(me.professorId),
        getPeriodos(),
        getHorariosProfessor(me.professorId),
      ]);
      setTurmas(t);
      setPeriodos(p);
      setHorarios(h);
      if (t.length > 0) {
        setTurmaId((current) => current || t[0].id);
      }
    } catch {
      setError("Nao foi possivel carregar dados do professor.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadContext();
  }, [loadContext]);

  useEffect(() => {
    if (!turmaId || !professorId) return;
    let cancelled = false;
    (async () => {
      try {
        const [d, a] = await Promise.all([
          getTurmaDisciplinas(turmaId, professorId),
          getTurmaAlunos(turmaId),
        ]);
        if (cancelled) return;
        setDisciplinas(d);
        setAlunos(a);
        setTdpId((current) => {
          if (current && d.some((item) => item.id === current)) return current;
          return d[0]?.id ?? "";
        });
      } catch {
        if (!cancelled) setError("Falha ao carregar alunos/disciplinas da turma.");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [turmaId, professorId]);

  const value = useMemo(
    () => ({
      loading,
      error,
      professorId,
      professorNome,
      turmas,
      periodos,
      horarios,
      turmaId,
      setTurmaId,
      disciplinas,
      tdpId,
      setTdpId,
      alunos,
      reload: loadContext,
    }),
    [
      loading,
      error,
      professorId,
      professorNome,
      turmas,
      periodos,
      horarios,
      turmaId,
      disciplinas,
      tdpId,
      alunos,
      loadContext,
    ],
  );

  return <ProfessorContext.Provider value={value}>{children}</ProfessorContext.Provider>;
}

export function useProfessorContext() {
  const ctx = useContext(ProfessorContext);
  if (!ctx) {
    throw new Error("useProfessorContext deve ser usado dentro de ProfessorProvider");
  }
  return ctx;
}
