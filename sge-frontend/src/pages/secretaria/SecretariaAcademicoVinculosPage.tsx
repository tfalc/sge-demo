import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { getTurmas } from "../../services/academicoService";
import { AcademicoAlerts, AcademicoFormPanel, SelectField } from "./AcademicoPageShell";
import { useAcademicoAction } from "./useAcademicoAction";
import {
  listarDisciplinas,
  listarProfessores,
  vincularDisciplinaTurma,
} from "../../services/academicoEstruturaService";
import type { DisciplinaCadastro, ProfessorCadastro, Turma } from "../../types";

export function SecretariaAcademicoVinculosPage() {
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [disciplinas, setDisciplinas] = useState<DisciplinaCadastro[]>([]);
  const [professores, setProfessores] = useState<ProfessorCadastro[]>([]);
  const [loading, setLoading] = useState(true);
  const [turmaId, setTurmaId] = useState("");
  const [disciplinaId, setDisciplinaId] = useState("");
  const [professorId, setProfessorId] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [t, d, p] = await Promise.all([getTurmas(), listarDisciplinas(), listarProfessores()]);
      setTurmas(t);
      setDisciplinas(d);
      setProfessores(p);
      if (t.length > 0) setTurmaId((current) => current || t[0].id);
      if (d.length > 0) setDisciplinaId((current) => current || d[0].id);
      if (p.length > 0) setProfessorId((current) => current || p[0].id);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const { saving, error, success, runAction } = useAcademicoAction(load);

  const turmaSelecionada = turmas.find((t) => t.id === turmaId);

  return (
    <AcademicoAlerts error={error} success={success} loading={loading}>
      <div className="max-w-xl">
        <AcademicoFormPanel title="Vincular disciplina e professor">
          <p className="mb-4 text-sm text-slate-600">
            Associe um componente curricular a um professor em uma turma. Consulte a matriz curricular para revisar os
            vinculos existentes.
          </p>
          <form
            className="space-y-3"
            onSubmit={(e) => {
              e.preventDefault();
              void runAction(
                () => vincularDisciplinaTurma(turmaId, { disciplinaId, professorId }),
                "Vinculo criado.",
              );
            }}
          >
            <SelectField label="Turma" value={turmaId} onChange={setTurmaId}>
              {turmas.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.nome} — {t.serieNome}
                </option>
              ))}
            </SelectField>
            <SelectField label="Disciplina" value={disciplinaId} onChange={setDisciplinaId}>
              {disciplinas.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.nome}
                </option>
              ))}
            </SelectField>
            <SelectField label="Professor" value={professorId} onChange={setProfessorId}>
              {professores.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nome}
                </option>
              ))}
            </SelectField>
            <Button type="submit" disabled={saving || !turmaId || !disciplinaId || !professorId}>
              Criar vinculo
            </Button>
          </form>
        </AcademicoFormPanel>
      </div>

      {turmaSelecionada ? (
        <p className="mt-4 text-sm text-slate-500">
          Turma selecionada: <strong>{turmaSelecionada.nome}</strong>. Para ver todos os vinculos da turma, use a tela
          Matriz.
        </p>
      ) : null}
    </AcademicoAlerts>
  );
}
