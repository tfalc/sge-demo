import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { AcademicoAlerts, AcademicoFormPanel, AcademicoListPanel } from "./AcademicoPageShell";
import { useAcademicoAction } from "./useAcademicoAction";
import { criarProfessor, listarProfessores } from "../../services/academicoEstruturaService";
import type { ProfessorCadastro } from "../../types";

export function SecretariaAcademicoProfessoresPage() {
  const [professores, setProfessores] = useState<ProfessorCadastro[]>([]);
  const [loading, setLoading] = useState(true);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setProfessores(await listarProfessores());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const { saving, error, success, runAction } = useAcademicoAction(load);

  return (
    <AcademicoAlerts error={error} success={success} loading={loading}>
      <div className="grid gap-6 xl:grid-cols-[360px_1fr]">
        <AcademicoFormPanel title="Novo professor">
          <form
            className="space-y-3"
            onSubmit={(e) => {
              e.preventDefault();
              void runAction(
                () => criarProfessor({ nome, email }),
                "Professor cadastrado (senha padrao: admin123).",
              ).then(() => {
                setNome("");
                setEmail("");
              });
            }}
          >
            <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input
              label="E-mail (login)"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Button type="submit" disabled={saving}>
              Cadastrar professor
            </Button>
          </form>
        </AcademicoFormPanel>

        <AcademicoListPanel title="Professores cadastrados" count={professores.length}>
          {professores.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhum professor cadastrado.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {professores.map((p) => (
                <li key={p.id} className="py-3 first:pt-0 last:pb-0">
                  <p className="font-medium text-slate-900">{p.nome}</p>
                  <p className="text-sm text-slate-500">{p.email}</p>
                </li>
              ))}
            </ul>
          )}
        </AcademicoListPanel>
      </div>
    </AcademicoAlerts>
  );
}
