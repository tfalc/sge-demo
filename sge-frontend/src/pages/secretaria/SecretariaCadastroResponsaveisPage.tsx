import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import {
  criarResponsavel,
  listarAlunos,
  listarResponsaveis,
  vincularResponsavel,
} from "../../services/cadastroService";
import type { AlunoCadastro, ResponsavelCadastro } from "../../types";
import { CadastroAlerts, CadastroPanel } from "./CadastroPageShell";

export function SecretariaCadastroResponsaveisPage() {
  const [alunos, setAlunos] = useState<AlunoCadastro[]>([]);
  const [responsaveis, setResponsaveis] = useState<ResponsavelCadastro[]>([]);
  const [respNome, setRespNome] = useState("");
  const [respEmail, setRespEmail] = useState("");
  const [respParentesco, setRespParentesco] = useState("Mae");
  const [respAlunoId, setRespAlunoId] = useState("");
  const [vinculoAlunoId, setVinculoAlunoId] = useState("");
  const [vinculoRespId, setVinculoRespId] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [a, r] = await Promise.all([listarAlunos(), listarResponsaveis()]);
      setAlunos(a);
      setResponsaveis(r);
      if (a.length > 0) {
        if (!respAlunoId) setRespAlunoId(a[0].id);
        if (!vinculoAlunoId) setVinculoAlunoId(a[0].id);
      }
      if (r.length > 0 && !vinculoRespId) setVinculoRespId(r[0].id);
    } catch {
      setError("Falha ao carregar responsaveis.");
    } finally {
      setLoading(false);
    }
  }, [respAlunoId, vinculoAlunoId, vinculoRespId]);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleResponsavelSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await criarResponsavel({
        nome: respNome,
        email: respEmail,
        grauParentesco: respParentesco,
        alunoId: respAlunoId || undefined,
      });
      setRespNome("");
      setRespEmail("");
      setSuccess("Responsavel cadastrado (senha padrao: admin123).");
      await load();
    } catch {
      setError("Falha ao cadastrar responsavel (e-mail duplicado?).");
    } finally {
      setSaving(false);
    }
  }

  async function handleVincular() {
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await vincularResponsavel(vinculoAlunoId, vinculoRespId);
      setSuccess("Responsavel vinculado ao aluno.");
      await load();
    } catch {
      setError("Falha ao vincular responsavel.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <CadastroAlerts error={error} success={success} loading={loading}>
      <div className="space-y-6">
        <div className="grid items-start gap-6 xl:grid-cols-2">
          <CadastroPanel title="Novo responsavel">
            <form className="grid gap-3" onSubmit={(e) => void handleResponsavelSubmit(e)}>
              <Input label="Nome" value={respNome} onChange={(e) => setRespNome(e.target.value)} required />
              <Input
                label="E-mail (login)"
                type="email"
                value={respEmail}
                onChange={(e) => setRespEmail(e.target.value)}
                required
              />
              <Input
                label="Parentesco"
                value={respParentesco}
                onChange={(e) => setRespParentesco(e.target.value)}
              />
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Vincular ao aluno (opcional)</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={respAlunoId}
                  onChange={(e) => setRespAlunoId(e.target.value)}
                >
                  <option value="">Cadastrar sem vinculo</option>
                  {alunos.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nome} — {a.matricula}
                    </option>
                  ))}
                </select>
              </label>
              <div>
                <Button type="submit" disabled={saving}>
                  {saving ? "Salvando..." : "Cadastrar responsavel"}
                </Button>
              </div>
            </form>
          </CadastroPanel>

          <CadastroPanel title="Vincular responsavel existente">
            <div className="grid gap-3">
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Aluno</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={vinculoAlunoId}
                  onChange={(e) => setVinculoAlunoId(e.target.value)}
                >
                  {alunos.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nome}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Responsavel</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={vinculoRespId}
                  onChange={(e) => setVinculoRespId(e.target.value)}
                >
                  {responsaveis.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.nome}
                    </option>
                  ))}
                </select>
              </label>
              <div>
                <Button type="button" disabled={saving} onClick={() => void handleVincular()}>
                  Vincular
                </Button>
              </div>
            </div>
          </CadastroPanel>
        </div>

        <CadastroPanel title={`Responsaveis cadastrados (${responsaveis.length})`}>
          {responsaveis.length === 0 ? (
            <p className="text-sm text-slate-600">Nenhum responsavel cadastrado.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-semibold">Nome</th>
                    <th className="px-3 py-2 text-left font-semibold">E-mail</th>
                    <th className="px-3 py-2 text-left font-semibold">Parentesco</th>
                    <th className="px-3 py-2 text-left font-semibold">Filhos</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {responsaveis.map((r) => (
                    <tr key={r.id}>
                      <td className="px-3 py-2 font-medium">{r.nome}</td>
                      <td className="px-3 py-2">{r.email ?? "—"}</td>
                      <td className="px-3 py-2">{r.grauParentesco ?? "—"}</td>
                      <td className="px-3 py-2">
                        {(r.alunos ?? []).map((a) => a.nome).join(", ") || "—"}
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
