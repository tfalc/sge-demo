import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import { getTurmas } from "../../services/academicoService";
import {
  createComunicado,
  deleteComunicado,
  getComunicados,
  updateComunicado,
} from "../../services/comunicacaoService";
import type { Comunicado, Turma } from "../../types";
import { formatDateTime } from "../../utils/dateRange";
import { ComunicacaoAlerts, ComunicacaoPanel } from "./ComunicacaoPageShell";

export function SecretariaComunicacaoComunicadosPage() {
  const [comunicados, setComunicados] = useState<Comunicado[]>([]);
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [titulo, setTitulo] = useState("");
  const [conteudo, setConteudo] = useState("");
  const [visivelPara, setVisivelPara] = useState("PAIS");
  const [turmaId, setTurmaId] = useState("");
  const [editComunicadoId, setEditComunicadoId] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [c, t] = await Promise.all([getComunicados(), getTurmas()]);
      setComunicados(c);
      setTurmas(t);
    } catch {
      setError("Nao foi possivel carregar comunicados.");
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
      if (editComunicadoId) {
        await updateComunicado(editComunicadoId, {
          titulo,
          conteudo,
          visivelPara,
          turmaId: turmaId || null,
        });
        setEditComunicadoId(null);
        setSuccess("Comunicado atualizado.");
      } else {
        await createComunicado({
          titulo,
          conteudo,
          visivelPara,
          turmaId: turmaId || null,
        });
        setSuccess("Comunicado publicado.");
      }
      setTitulo("");
      setConteudo("");
      setTurmaId("");
      await load();
    } catch {
      setError(editComunicadoId ? "Falha ao atualizar comunicado." : "Falha ao publicar comunicado.");
    } finally {
      setSaving(false);
    }
  }

  function startEdit(c: Comunicado) {
    setEditComunicadoId(c.id);
    setTitulo(c.titulo);
    setConteudo(c.conteudo);
    setVisivelPara(c.visivelPara);
    setTurmaId(c.turmaId ?? "");
  }

  function cancelEdicao() {
    setEditComunicadoId(null);
    setTitulo("");
    setConteudo("");
    setTurmaId("");
  }

  return (
    <ComunicacaoAlerts error={error} success={success} loading={loading}>
      <div className="grid items-start gap-6 xl:grid-cols-2">
        <ComunicacaoPanel title={editComunicadoId ? "Editar comunicado" : "Novo comunicado"}>
          <form className="space-y-3" onSubmit={(e) => void handleSubmit(e)}>
            <Input label="Titulo" value={titulo} onChange={(e) => setTitulo(e.target.value)} required />
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Conteudo</span>
              <textarea
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                rows={5}
                value={conteudo}
                onChange={(e) => setConteudo(e.target.value)}
                required
              />
            </label>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Visivel para</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={visivelPara}
                onChange={(e) => setVisivelPara(e.target.value)}
              >
                <option value="PAIS">Pais</option>
                <option value="PROFESSORES">Professores</option>
                <option value="TODOS">Todos</option>
              </select>
            </label>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Turma (opcional)</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={turmaId}
                onChange={(e) => setTurmaId(e.target.value)}
              >
                <option value="">Escola toda</option>
                {turmas.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nome} — {t.serieNome}
                  </option>
                ))}
              </select>
            </label>
            <div className="flex flex-wrap items-center gap-2">
              <Button type="submit" disabled={saving}>
                {saving ? "Salvando..." : editComunicadoId ? "Salvar alteracoes" : "Publicar"}
              </Button>
              {editComunicadoId ? (
                <ActionButton type="button" variant="neutral" onClick={cancelEdicao}>
                  Cancelar edicao
                </ActionButton>
              ) : null}
            </div>
          </form>
        </ComunicacaoPanel>

        <ComunicacaoPanel title={`Comunicados publicados (${comunicados.length})`}>
          {comunicados.length === 0 ? (
            <p className="text-sm text-slate-600">Nenhum comunicado publicado.</p>
          ) : (
            <div className="max-h-[32rem] space-y-2 overflow-y-auto">
              {comunicados.map((c) => (
                <div
                  key={c.id}
                  className="flex items-start justify-between gap-2 rounded-lg border border-slate-100 bg-slate-50 p-3 text-sm"
                >
                  <div className="min-w-0">
                    <div className="font-medium text-slate-900">{c.titulo}</div>
                    <p className="mt-1 line-clamp-2 text-slate-600">{c.conteudo}</p>
                    <div className="mt-1 text-xs text-slate-500">
                      {c.visivelPara} · {formatDateTime(c.publicadoEm)}
                    </div>
                  </div>
                  <span className="flex shrink-0 gap-2">
                    <ActionButton type="button" onClick={() => startEdit(c)}>
                      Editar
                    </ActionButton>
                    <ActionButton
                      type="button"
                      variant="danger"
                      onClick={() => {
                        void (async () => {
                          await deleteComunicado(c.id);
                          await load();
                        })();
                      }}
                    >
                      Excluir
                    </ActionButton>
                  </span>
                </div>
              ))}
            </div>
          )}
        </ComunicacaoPanel>
      </div>
    </ComunicacaoAlerts>
  );
}
