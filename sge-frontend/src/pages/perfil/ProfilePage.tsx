import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { SectionNav } from "../../components/layout/SectionNav";
import { atualizarPerfil, getMe, trocarSenha } from "../../services/authService";
import { useAuthStore } from "../../store/authStore";
import { resolveSectionNavs } from "../../utils/sectionNavResolver";

export function ProfilePage() {
  const perfilAuth = useAuthStore((s) => s.perfil);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [telefone, setTelefone] = useState("");
  const [perfil, setPerfil] = useState("");
  const [senhaAtual, setSenhaAtual] = useState("");
  const [senhaNova, setSenhaNova] = useState("");
  const [loading, setLoading] = useState(true);
  const [savingPerfil, setSavingPerfil] = useState(false);
  const [savingSenha, setSavingSenha] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const me = await getMe();
      setNome(me.nome);
      setEmail(me.email);
      setTelefone(me.telefone ?? "");
      setPerfil(me.perfil);
    } catch {
      setError("Nao foi possivel carregar seu perfil.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function handlePerfilSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSavingPerfil(true);
    setError(null);
    setSuccess(null);
    try {
      await atualizarPerfil({ nome, email, telefone: telefone || undefined });
      setSuccess("Perfil atualizado.");
      await load();
    } catch {
      setError("Falha ao atualizar perfil. Verifique se o e-mail ja esta em uso.");
    } finally {
      setSavingPerfil(false);
    }
  }

  async function handleSenhaSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSavingSenha(true);
    setError(null);
    setSuccess(null);
    try {
      await trocarSenha({ senhaAtual, senhaNova });
      setSuccess("Senha alterada com sucesso.");
      setSenhaAtual("");
      setSenhaNova("");
    } catch {
      setError("Senha atual incorreta ou nova senha invalida.");
    } finally {
      setSavingSenha(false);
    }
  }

  const sectionNavs = resolveSectionNavs(perfilAuth ?? perfil);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Meu perfil</h2>
        <p className="mt-1 text-sm text-slate-600">
          Atualize seus dados e senha de acesso{perfil ? ` (${perfil})` : ""}.
        </p>
      </div>

      {sectionNavs.primary ? <SectionNav items={sectionNavs.primary} /> : null}
      {sectionNavs.secondary ? <SectionNav items={sectionNavs.secondary} /> : null}

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
        <>
          <form
            onSubmit={(e) => void handlePerfilSubmit(e)}
            className="space-y-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <h3 className="text-base font-semibold text-slate-900">Dados pessoais</h3>
            <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input
              label="E-mail"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Input label="Telefone" value={telefone} onChange={(e) => setTelefone(e.target.value)} />
            <Button type="submit" disabled={savingPerfil}>
              {savingPerfil ? "Salvando..." : "Salvar perfil"}
            </Button>
          </form>

          <form
            onSubmit={(e) => void handleSenhaSubmit(e)}
            className="space-y-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <h3 className="text-base font-semibold text-slate-900">Alterar senha</h3>
            <Input
              label="Senha atual"
              type="password"
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
              required
            />
            <Input
              label="Nova senha"
              type="password"
              value={senhaNova}
              onChange={(e) => setSenhaNova(e.target.value)}
              required
              minLength={6}
            />
            <Button type="submit" disabled={savingSenha}>
              {savingSenha ? "Alterando..." : "Alterar senha"}
            </Button>
          </form>
        </>
      )}
    </div>
  );
}
