import { useCallback, useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { SectionNav } from "../../components/layout/SectionNav";
import { Button } from "../../components/ui/Button";
import { MENU_AREAS } from "../../config/navConfig";
import {
  getAcessosMenu,
  putAcessosMenu,
  restaurarAcessosMenuDefaults,
  type AcessosMenuMatriz,
} from "../../services/acessosMenuService";
import { getMe } from "../../services/authService";
import { useAuthStore } from "../../store/authStore";

const AREA_LABELS: Record<string, string> = {
  pais: "Pais",
  aluno: "Aluno",
  professor: "Professor",
  secretaria: "Secretaria",
  direcao: "Direção",
  coordenacao: "Coord.",
  nutricao: "Nutrição",
  psicologia: "Psicologia",
};

const PERFIL_LABELS: Record<string, string> = {
  ADMIN: "Admin (donos)",
  DIRETOR: "Direção",
  COORDENADOR: "Coordenação",
  PROFESSOR: "Professor",
  SECRETARIA: "Secretaria",
  PAI: "Pais",
  ALUNO: "Aluno",
  NUTRICIONISTA: "Nutrição",
  PSICOLOGA: "Psicologia",
};

const adminNav = [
  { to: "/admin/acessos", label: "Acessos de menu" },
  { to: "/perfil", label: "Perfil" },
];

export function AdminAcessosMenuPage() {
  const perfil = useAuthStore((s) => s.perfil);
  const setAreasMenu = useAuthStore((s) => s.setAreasMenu);

  const [matriz, setMatriz] = useState<AcessosMenuMatriz | null>(null);
  const [draft, setDraft] = useState<Record<string, string[]>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAcessosMenu();
      setMatriz(data);
      setDraft({ ...data.acessos });
    } catch {
      setError("Não foi possível carregar a matriz de acessos.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  if (perfil && perfil !== "ADMIN") {
    return <Navigate to="/secretaria/hoje" replace />;
  }

  function toggle(perfilKey: string, area: string) {
    setDraft((prev) => {
      const current = new Set(prev[perfilKey] ?? []);
      if (current.has(area)) {
        current.delete(area);
      } else {
        current.add(area);
      }
      return { ...prev, [perfilKey]: Array.from(current) };
    });
    setSuccess(null);
  }

  async function handleSalvar() {
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const data = await putAcessosMenu(draft);
      setMatriz(data);
      setDraft({ ...data.acessos });
      const me = await getMe();
      setAreasMenu(me.areasMenu ?? []);
      setSuccess("Acessos salvos. O menu superior já reflete as alterações para o seu usuário.");
    } catch {
      setError("Falha ao salvar. Confirme que você está logado como Admin.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDefaults() {
    if (!window.confirm("Restaurar os acessos padrão de todos os perfis?")) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const data = await restaurarAcessosMenuDefaults();
      setMatriz(data);
      setDraft({ ...data.acessos });
      const me = await getMe();
      setAreasMenu(me.areasMenu ?? []);
      setSuccess("Defaults restaurados.");
    } catch {
      setError("Falha ao restaurar defaults.");
    } finally {
      setSaving(false);
    }
  }

  const areas = matriz?.areas ?? MENU_AREAS;
  const perfis = matriz?.perfis ?? Object.keys(PERFIL_LABELS);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Administração — Acessos de menu</h2>
        <p className="mt-1 text-sm text-slate-600">
          Área dos donos da escola. Defina quais menus superiores cada perfil vê e pode abrir. Isto não
          faz parte da Direção escolar — compete só à administração do sistema.
        </p>
      </div>

      <SectionNav items={adminNav} />

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
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-3 py-2 text-left font-semibold text-slate-700">Perfil</th>
                {areas.map((area) => (
                  <th key={area} className="px-2 py-2 text-center font-semibold text-slate-700">
                    {AREA_LABELS[area] ?? area}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {perfis.map((perfilKey) => (
                <tr key={perfilKey}>
                  <td className="whitespace-nowrap px-3 py-2 font-medium text-slate-900">
                    {PERFIL_LABELS[perfilKey] ?? perfilKey}
                  </td>
                  {areas.map((area) => {
                    const checked = (draft[perfilKey] ?? []).includes(area);
                    return (
                      <td key={area} className="px-2 py-2 text-center">
                        <input
                          type="checkbox"
                          className="h-4 w-4"
                          checked={checked}
                          onChange={() => toggle(perfilKey, area)}
                          aria-label={`${perfilKey} — ${area}`}
                        />
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="flex flex-wrap gap-3">
        <Button type="button" disabled={saving || loading} onClick={() => void handleSalvar()}>
          {saving ? "Salvando..." : "Salvar acessos"}
        </Button>
        <Button
          type="button"
          variant="neutral"
          disabled={saving || loading}
          onClick={() => void handleDefaults()}
        >
          Restaurar defaults
        </Button>
      </div>
    </div>
  );
}
