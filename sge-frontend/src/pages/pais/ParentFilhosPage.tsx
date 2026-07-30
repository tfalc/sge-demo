import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { SectionNav } from "../../components/layout/SectionNav";
import { getTurmas } from "../../services/academicoService";
import { getMe } from "../../services/authService";
import { cadastrarMeuFilho } from "../../services/cadastroService";
import type { FilhoResumo, Turma } from "../../types";
import { parentNav } from "./parentNav";

export function ParentFilhosPage() {
  const [filhos, setFilhos] = useState<FilhoResumo[]>([]);
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
      const [me, t] = await Promise.all([getMe(), getTurmas()]);
      setFilhos(me.filhos);
      setTurmas(t);
      if (t.length > 0 && !turmaId) setTurmaId(t[0].id);
    } catch {
      setError("Nao foi possivel carregar seus filhos.");
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
      await cadastrarMeuFilho({ nome, matricula, turmaId });
      setNome("");
      setMatricula("");
      setSuccess("Filho cadastrado e vinculado a sua conta.");
      await load();
    } catch {
      setError("Falha ao cadastrar filho (matricula duplicada?).");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Meus filhos</h2>
        <p className="mt-1 text-sm text-slate-600">
          Cadastre seus filhos vinculados a esta escola. A secretaria confere turma e matricula; exclusao so pela
          escola.
        </p>
      </div>

      <SectionNav items={parentNav} />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : (
        <div className="grid items-start gap-6 xl:grid-cols-2">
          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Cadastrar filho</h3>
            <form className="mt-4 grid gap-3" onSubmit={(e) => void handleSubmit(e)}>
              <Input label="Nome completo" value={nome} onChange={(e) => setNome(e.target.value)} required />
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
                  {saving ? "Salvando..." : "Cadastrar filho"}
                </Button>
              </div>
            </form>
          </section>

          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Filhos vinculados ({filhos.length})</h3>
            {filhos.length === 0 ? (
              <p className="mt-3 text-sm text-slate-600">Nenhum filho cadastrado ainda.</p>
            ) : (
              <ul className="mt-3 space-y-2 text-sm">
                {filhos.map((f) => (
                  <li
                    key={f.alunoId}
                    className="rounded-lg border border-slate-100 bg-slate-50 px-3 py-2"
                  >
                    <div className="font-medium text-slate-900">{f.nome}</div>
                    <div className="text-xs text-slate-500">
                      {f.matricula ? `Matricula ${f.matricula}` : "Matricula —"}
                      {f.turmaNome ? ` · ${f.turmaNome}` : ""}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
      )}
    </div>
  );
}
