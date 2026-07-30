import { useCallback, useEffect, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { ActionButton } from "../../components/ui/ActionButton";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import {
  atualizarPatrimonioItem,
  criarPatrimonioItem,
  excluirPatrimonioItem,
  listarPatrimonio,
  type PatrimonioItem,
  type PatrimonioStatus,
} from "../../services/patrimonioService";
import { formatCurrency, formatDate } from "../../utils/format";
import { useGestaoArea } from "./useGestaoArea";

const statusOptions: PatrimonioStatus[] = ["ATIVO", "MANUTENCAO", "BAIXADO"];

export function SecretariaPatrimonioPage() {
  const { areaLabel, primaryNav } = useGestaoArea();
  const [itens, setItens] = useState<PatrimonioItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [nome, setNome] = useState("");
  const [categoria, setCategoria] = useState("");
  const [localizacao, setLocalizacao] = useState("");
  const [numeroPatrimonio, setNumeroPatrimonio] = useState("");
  const [dataAquisicao, setDataAquisicao] = useState("");
  const [valorAquisicao, setValorAquisicao] = useState("");
  const [status, setStatus] = useState<PatrimonioStatus>("ATIVO");
  const [observacoes, setObservacoes] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setItens(await listarPatrimonio());
    } catch {
      setError("Nao foi possivel carregar inventario patrimonial.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await criarPatrimonioItem({
        nome,
        categoria: categoria || undefined,
        localizacao: localizacao || undefined,
        numeroPatrimonio: numeroPatrimonio || undefined,
        dataAquisicao: dataAquisicao || undefined,
        valorAquisicao: valorAquisicao ? Number(valorAquisicao) : undefined,
        status,
        observacoes: observacoes || undefined,
      });
      setNome("");
      setCategoria("");
      setLocalizacao("");
      setNumeroPatrimonio("");
      setDataAquisicao("");
      setValorAquisicao("");
      setObservacoes("");
      setStatus("ATIVO");
      setSuccess("Item cadastrado.");
      await load();
    } catch {
      setError("Falha ao cadastrar item.");
    } finally {
      setSaving(false);
    }
  }

  async function handleStatusChange(item: PatrimonioItem, novoStatus: PatrimonioStatus) {
    try {
      await atualizarPatrimonioItem(item.id, { status: novoStatus });
      await load();
    } catch {
      setError("Falha ao atualizar status.");
    }
  }

  async function handleExcluir(id: string) {
    if (!window.confirm("Excluir este item do inventario?")) return;
    try {
      await excluirPatrimonioItem(id);
      setSuccess("Item excluido.");
      await load();
    } catch {
      setError("Falha ao excluir item.");
    }
  }

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Patrimonio</h2>
        <p className="mt-1 text-sm text-slate-600">
          Inventario basico de bens moveis e equipamentos da escola.
        </p>
      </div>

      <SectionNav items={primaryNav} />

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-base font-semibold text-slate-900">Novo item</h3>
        <form className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3" onSubmit={(e) => void handleSubmit(e)}>
          <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
          <Input label="Categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)} />
          <Input label="Localizacao" value={localizacao} onChange={(e) => setLocalizacao(e.target.value)} />
          <Input
            label="N. patrimonio"
            value={numeroPatrimonio}
            onChange={(e) => setNumeroPatrimonio(e.target.value)}
          />
          <Input
            label="Data aquisicao"
            type="date"
            value={dataAquisicao}
            onChange={(e) => setDataAquisicao(e.target.value)}
          />
          <Input
            label="Valor (R$)"
            type="number"
            min="0"
            step="0.01"
            value={valorAquisicao}
            onChange={(e) => setValorAquisicao(e.target.value)}
          />
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Status</span>
            <select
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              value={status}
              onChange={(e) => setStatus(e.target.value as PatrimonioStatus)}
            >
              {statusOptions.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </label>
          <Input
            label="Observacoes"
            value={observacoes}
            onChange={(e) => setObservacoes(e.target.value)}
          />
          <div className="flex items-end sm:col-span-2 lg:col-span-3">
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando..." : "Cadastrar item"}
            </Button>
          </div>
        </form>
      </section>

      <section className="space-y-3">
        <h3 className="text-base font-semibold text-slate-900">Inventario ({itens.length})</h3>
        {loading ? (
          <p className="text-sm text-slate-500">Carregando...</p>
        ) : itens.length === 0 ? (
          <div className="rounded-lg border border-slate-200 bg-white px-4 py-6 text-center text-sm text-slate-600">
            Nenhum item cadastrado.
          </div>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Nome</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Categoria</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Local</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">N. patrimonio</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Aquisicao</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Valor</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Status</th>
                  <th className="px-4 py-3 text-left font-semibold text-slate-700">Acoes</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {itens.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80">
                    <td className="px-4 py-3 font-medium text-slate-900">{item.nome}</td>
                    <td className="px-4 py-3">{item.categoria ?? "—"}</td>
                    <td className="px-4 py-3">{item.localizacao ?? "—"}</td>
                    <td className="px-4 py-3">{item.numeroPatrimonio ?? "—"}</td>
                    <td className="px-4 py-3">
                      {item.dataAquisicao ? formatDate(item.dataAquisicao) : "—"}
                    </td>
                    <td className="px-4 py-3">
                      {item.valorAquisicao != null ? formatCurrency(item.valorAquisicao) : "—"}
                    </td>
                    <td className="px-4 py-3">
                      <select
                        className="rounded border border-slate-300 px-2 py-1 text-xs"
                        value={item.status}
                        onChange={(e) =>
                          void handleStatusChange(item, e.target.value as PatrimonioStatus)
                        }
                      >
                        {statusOptions.map((s) => (
                          <option key={s} value={s}>
                            {s}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      <ActionButton type="button" variant="danger" onClick={() => void handleExcluir(item.id)}>
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
