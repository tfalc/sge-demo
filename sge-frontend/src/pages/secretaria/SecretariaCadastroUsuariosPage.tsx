import { useCallback, useEffect, useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { atualizarUsuario, listarUsuarios, type PerfilUsuario, type UsuarioAdmin } from "../../services/adminService";
import { CadastroAlerts, CadastroPanel } from "./CadastroPageShell";

const perfis: PerfilUsuario[] = [
  "ADMIN",
  "DIRETOR",
  "COORDENADOR",
  "PROFESSOR",
  "SECRETARIA",
  "PAI",
  "ALUNO",
  "NUTRICIONISTA",
  "PSICOLOGA",
];

export function SecretariaCadastroUsuariosPage() {
  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setUsuarios(await listarUsuarios());
    } catch {
      setError("Falha ao carregar usuarios.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleSalvar(usuario: UsuarioAdmin, perfil: PerfilUsuario, ativo: boolean) {
    setSavingId(usuario.id);
    setError(null);
    setSuccess(null);
    try {
      await atualizarUsuario(usuario.id, { perfil, ativo });
      setSuccess(`Usuario ${usuario.email} atualizado.`);
      await load();
    } catch {
      setError("Falha ao atualizar usuario.");
    } finally {
      setSavingId(null);
    }
  }

  return (
    <CadastroAlerts error={error} success={success} loading={loading}>
      <CadastroPanel title="Usuarios do sistema">
        <p className="mb-4 text-sm text-slate-600">
          Altere perfil de acesso e status ativo. Novos usuarios sao criados pelo seed ou cadastro de responsaveis.
        </p>

        {usuarios.length === 0 ? (
          <p className="text-sm text-slate-600">Nenhum usuario cadastrado.</p>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Nome</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">E-mail</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Perfil</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Ativo</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Acoes</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {usuarios.map((u) => (
                  <UsuarioRow
                    key={u.id}
                    usuario={u}
                    saving={savingId === u.id}
                    onSalvar={handleSalvar}
                  />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CadastroPanel>
    </CadastroAlerts>
  );
}

function UsuarioRow({
  usuario,
  saving,
  onSalvar,
}: {
  usuario: UsuarioAdmin;
  saving: boolean;
  onSalvar: (u: UsuarioAdmin, perfil: PerfilUsuario, ativo: boolean) => Promise<void>;
}) {
  const [perfil, setPerfil] = useState<PerfilUsuario>(usuario.perfil);
  const [ativo, setAtivo] = useState(usuario.ativo);

  useEffect(() => {
    setPerfil(usuario.perfil);
    setAtivo(usuario.ativo);
  }, [usuario.perfil, usuario.ativo]);

  const dirty = perfil !== usuario.perfil || ativo !== usuario.ativo;

  return (
    <tr className="hover:bg-slate-50/80">
      <td className="px-4 py-3 font-medium text-slate-900">{usuario.nome ?? "—"}</td>
      <td className="px-4 py-3 text-slate-700">{usuario.email}</td>
      <td className="px-4 py-3">
        <select
          className="w-full min-w-[140px] rounded-lg border border-slate-300 px-2 py-1.5 text-sm"
          value={perfil}
          onChange={(e) => setPerfil(e.target.value as PerfilUsuario)}
        >
          {perfis.map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </select>
      </td>
      <td className="px-4 py-3">
        <label className="inline-flex items-center gap-2 text-sm">
          <input type="checkbox" checked={ativo} onChange={(e) => setAtivo(e.target.checked)} />
          {ativo ? "Sim" : "Nao"}
        </label>
      </td>
      <td className="px-4 py-3">
        <Button
          size="sm"
          disabled={!dirty || saving}
          onClick={() => void onSalvar(usuario, perfil, ativo)}
        >
          {saving ? "Salvando..." : "Salvar"}
        </Button>
      </td>
    </tr>
  );
}
