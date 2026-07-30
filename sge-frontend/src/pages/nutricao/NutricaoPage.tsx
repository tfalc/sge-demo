import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import { listarAlunos } from "../../services/cadastroService";
import { getMe } from "../../services/authService";
import { createCardapio, deleteCardapioItem, getCardapio } from "../../services/comunicacaoService";
import {
  criarRestricao,
  excluirRestricao,
  listarRestricoes,
  type RestricaoAlimentar,
  type SeveridadeRestricao,
} from "../../services/nutricaoService";
import type { AlunoCadastro, CardapioItem, TipoRefeicao } from "../../types";
import { todayIso } from "../../utils/dateRange";
import { formatDate } from "../../utils/format";

const tipoRefeicaoLabel: Record<TipoRefeicao, string> = {
  ALMOCO: "Almoco",
  LANCHE: "Lanche",
};

export function NutricaoPage() {
  const [nutriNome, setNutriNome] = useState("");
  const [data, setData] = useState(todayIso());
  const [itens, setItens] = useState<CardapioItem[]>([]);
  const [tipo, setTipo] = useState<TipoRefeicao>("ALMOCO");
  const [descricao, setDescricao] = useState("");
  const [calorias, setCalorias] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [alunos, setAlunos] = useState<AlunoCadastro[]>([]);
  const [restricoes, setRestricoes] = useState<RestricaoAlimentar[]>([]);
  const [restricaoAlunoId, setRestricaoAlunoId] = useState("");
  const [restricaoDescricao, setRestricaoDescricao] = useState("");
  const [restricaoSeveridade, setRestricaoSeveridade] = useState<SeveridadeRestricao>("MODERADA");
  const [restricoesLoading, setRestricoesLoading] = useState(true);
  const [restricaoSaving, setRestricaoSaving] = useState(false);

  const loadCardapio = useCallback(async (dia: string) => {
    setLoading(true);
    setError(null);
    try {
      const list = await getCardapio(dia);
      setItens(list);
    } catch {
      setError("Nao foi possivel carregar cardapio.");
    } finally {
      setLoading(false);
    }
  }, []);

  const loadRestricoes = useCallback(async () => {
    setRestricoesLoading(true);
    try {
      const [a, r] = await Promise.all([listarAlunos(), listarRestricoes()]);
      setAlunos(a);
      setRestricoes(r);
      setRestricaoAlunoId((prev) => prev || (a[0]?.id ?? ""));
    } catch {
      setError("Nao foi possivel carregar restricoes alimentares.");
    } finally {
      setRestricoesLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const me = await getMe();
        if (!cancelled) setNutriNome(me.nome);
      } catch {
        if (!cancelled) setError("Falha ao carregar perfil.");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    void loadCardapio(data);
  }, [data, loadCardapio]);

  useEffect(() => {
    void loadRestricoes();
  }, [loadRestricoes]);

  async function handleRestricaoSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!restricaoAlunoId || !restricaoDescricao.trim()) return;

    setRestricaoSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await criarRestricao({
        alunoId: restricaoAlunoId,
        descricao: restricaoDescricao,
        severidade: restricaoSeveridade,
      });
      setRestricaoDescricao("");
      setSuccess("Restricao alimentar cadastrada.");
      await loadRestricoes();
    } catch {
      setError("Falha ao cadastrar restricao.");
    } finally {
      setRestricaoSaving(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await createCardapio({
        dataRefeicao: data,
        tipoRefeicao: tipo,
        descricao,
        calorias: calorias ? Number(calorias) : undefined,
      });
      setDescricao("");
      setCalorias("");
      setSuccess("Item de cardapio cadastrado.");
      await loadCardapio(data);
    } catch {
      setError("Falha ao cadastrar cardapio.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Nutricao Escolar</h2>
        <p className="mt-1 text-sm text-slate-600">
          {nutriNome ? `Ola, ${nutriNome}. ` : ""}
          Cadastre o cardapio diario visivel aos pais.
        </p>
      </div>

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-2">
        <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="text-base font-semibold text-slate-900">Novo item</h3>
          <form className="mt-4 space-y-3" onSubmit={(e) => void handleSubmit(e)}>
            <Input label="Data" type="date" value={data} onChange={(e) => setData(e.target.value)} />
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Refeicao</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={tipo}
                onChange={(e) => setTipo(e.target.value as TipoRefeicao)}
              >
                <option value="ALMOCO">Almoco</option>
                <option value="LANCHE">Lanche</option>
              </select>
            </label>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Descricao</span>
              <textarea
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                rows={3}
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                required
              />
            </label>
            <Input
              label="Calorias (opcional)"
              type="number"
              min="0"
              value={calorias}
              onChange={(e) => setCalorias(e.target.value)}
            />
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando..." : "Cadastrar"}
            </Button>
          </form>
        </section>

        <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="text-base font-semibold text-slate-900">
            Cardapio — {formatDate(data)}
          </h3>
          {loading ? (
            <p className="mt-4 text-sm text-slate-500">Carregando...</p>
          ) : itens.length === 0 ? (
            <p className="mt-4 text-sm text-slate-600">Nenhum item para esta data.</p>
          ) : (
            <div className="mt-4 space-y-3">
              {itens.map((item) => (
                <div key={item.id} className="flex items-start justify-between gap-2 rounded-lg border border-emerald-100 bg-emerald-50 p-3">
                  <div>
                    <div className="text-sm font-semibold text-emerald-900">
                      {tipoRefeicaoLabel[item.tipoRefeicao]}
                    </div>
                    <p className="mt-1 text-sm text-slate-800">{item.descricao}</p>
                    {item.calorias ? (
                      <p className="mt-1 text-xs text-slate-600">{item.calorias} kcal</p>
                    ) : null}
                  </div>
                  <ActionButton
                    type="button"
                    variant="danger"
                    onClick={() => void deleteCardapioItem(item.id).then(() => loadCardapio(data))}
                  >
                    Excluir
                  </ActionButton>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      <section className="rounded-xl border border-amber-200 bg-amber-50/50 p-5 shadow-sm">
        <h3 className="text-base font-semibold text-slate-900">Restricoes alimentares por aluno</h3>
        <p className="mt-1 text-sm text-slate-600">
          Alergias, intolerancias e orientacoes medicas para o cardapio escolar.
        </p>

        <form className="mt-4 grid gap-3 md:grid-cols-4" onSubmit={(e) => void handleRestricaoSubmit(e)}>
          <label className="block text-sm md:col-span-2">
            <span className="mb-1 block font-medium text-slate-700">Aluno</span>
            <select
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              value={restricaoAlunoId}
              onChange={(e) => setRestricaoAlunoId(e.target.value)}
              required
            >
              {alunos.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.nome} ({a.matricula})
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Severidade</span>
            <select
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              value={restricaoSeveridade}
              onChange={(e) => setRestricaoSeveridade(e.target.value as SeveridadeRestricao)}
            >
              <option value="LEVE">Leve</option>
              <option value="MODERADA">Moderada</option>
              <option value="GRAVE">Grave</option>
            </select>
          </label>
          <div className="flex items-end">
            <Button type="submit" disabled={restricaoSaving}>
              {restricaoSaving ? "Salvando..." : "Cadastrar"}
            </Button>
          </div>
          <label className="block text-sm md:col-span-4">
            <span className="mb-1 block font-medium text-slate-700">Descricao</span>
            <textarea
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              rows={2}
              value={restricaoDescricao}
              onChange={(e) => setRestricaoDescricao(e.target.value)}
              required
            />
          </label>
        </form>

        {restricoesLoading ? (
          <p className="mt-4 text-sm text-slate-500">Carregando restricoes...</p>
        ) : restricoes.length === 0 ? (
          <p className="mt-4 text-sm text-slate-600">Nenhuma restricao cadastrada.</p>
        ) : (
          <div className="mt-4 overflow-x-auto rounded-lg border border-amber-200 bg-white">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Aluno</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Descricao</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Severidade</th>
                  <th className="px-4 py-2 text-left font-semibold text-slate-700">Acoes</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {restricoes.map((r) => (
                  <tr key={r.id}>
                    <td className="px-4 py-2 font-medium text-slate-900">{r.alunoNome}</td>
                    <td className="px-4 py-2">{r.descricao}</td>
                    <td className="px-4 py-2">{r.severidade}</td>
                    <td className="px-4 py-2">
                      <ActionButton
                        type="button"
                        variant="danger"
                        onClick={() => void excluirRestricao(r.id).then(() => loadRestricoes())}
                      >
                        Excluir
                      </ActionButton>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
