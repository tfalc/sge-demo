import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import { getTurmas } from "../../services/academicoService";
import { criarAluno, desvincularResponsavel, listarAlunos } from "../../services/cadastroService";
import type { AlunoCadastro, Turma } from "../../types";
import { CadastroAlerts, CadastroPanel } from "./CadastroPageShell";

export function SecretariaCadastroAlunosPage() {
  const [alunos, setAlunos] = useState<AlunoCadastro[]>([]);
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [nome, setNome] = useState("");
  const [matricula, setMatricula] = useState("");
  const [turmaId, setTurmaId] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [a, t] = await Promise.all([listarAlunos(), getTurmas()]);
      setAlunos(a);
      setTurmas(t);
      if (t.length > 0 && !turmaId) setTurmaId(t[0].id);
    } catch {
      setError("Falha ao carregar alunos.");
    } finally {
      setLoading(false);
    }
  }, [turmaId]);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await criarAluno({ nome, matricula, turmaId });
      setNome("");
      setMatricula("");
      setSuccess("Aluno cadastrado.");
      await load();
    } catch {
      setError("Falha ao cadastrar aluno (matricula duplicada?).");
    } finally {
      setSaving(false);
    }
  }

  async function handleDesvincular(alunoId: string, responsavelId: string) {
    try {
      await desvincularResponsavel(alunoId, responsavelId);
      setSuccess("Vinculo removido.");
      await load();
    } catch {
      setError("Falha ao remover vinculo.");
    }
  }

  return (
    <CadastroAlerts error={error} success={success} loading={loading}>
      <div className="grid items-start gap-6 xl:grid-cols-2">
        <CadastroPanel title="Novo aluno">
          <p className="mb-4 text-sm text-slate-600">
            Responsaveis tambem podem cadastrar filhos em Pais → Meus filhos. Contratos de mensalidade ficam em
            Financeiro.
          </p>
          <form className="grid gap-3" onSubmit={(e) => void handleSubmit(e)}>
            <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input label="Matricula" value={matricula} onChange={(e) => setMatricula(e.target.value)} required />
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Turma</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={turmaId}
                onChange={(e) => setTurmaId(e.target.value)}
              >
                {turmas.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nome} — {t.serieNome}
                  </option>
                ))}
              </select>
            </label>
            <div>
              <Button type="submit" disabled={saving}>
                {saving ? "Salvando..." : "Cadastrar aluno"}
              </Button>
            </div>
          </form>
        </CadastroPanel>

        <CadastroPanel title={`Alunos cadastrados (${alunos.length})`}>
          {alunos.length === 0 ? (
            <p className="text-sm text-slate-600">Nenhum aluno cadastrado.</p>
          ) : (
            <div className="max-h-[32rem] overflow-y-auto">
              <table className="min-w-full text-sm">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-semibold">Nome</th>
                    <th className="px-3 py-2 text-left font-semibold">Matricula</th>
                    <th className="px-3 py-2 text-left font-semibold">Turma</th>
                    <th className="px-3 py-2 text-left font-semibold">Responsaveis</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {alunos.map((a) => (
                    <tr key={a.id}>
                      <td className="px-3 py-2 font-medium">{a.nome}</td>
                      <td className="px-3 py-2">{a.matricula}</td>
                      <td className="px-3 py-2">{a.turmaNome ?? "—"}</td>
                      <td className="px-3 py-2">
                        {(a.responsaveis ?? []).length === 0 ? (
                          <span className="text-slate-500">—</span>
                        ) : (
                          <ul className="space-y-1">
                            {(a.responsaveis ?? []).map((r) => (
                              <li key={r.responsavelId} className="flex items-center gap-2">
                                <span>
                                  {r.nome}
                                  {r.grauParentesco ? ` (${r.grauParentesco})` : ""}
                                </span>
                                <ActionButton
                                  type="button"
                                  variant="danger"
                                  onClick={() => void handleDesvincular(a.id, r.responsavelId)}
                                >
                                  Remover
                                </ActionButton>
                              </li>
                            ))}
                          </ul>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CadastroPanel>
      </div>
    </CadastroAlerts>
  );
}
