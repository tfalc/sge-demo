import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import { getTurmas } from "../../services/academicoService";
import {
  AcademicoAlerts,
  AcademicoFormPanel,
  AcademicoListPanel,
  SelectField,
} from "./AcademicoPageShell";
import { useAcademicoAction } from "./useAcademicoAction";
import {
  atualizarTurma,
  criarTurma,
  excluirTurma,
  listarSeries,
} from "../../services/academicoEstruturaService";
import type { SerieCadastro, Turma } from "../../types";

export function SecretariaAcademicoTurmasPage() {
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [series, setSeries] = useState<SerieCadastro[]>([]);
  const [loading, setLoading] = useState(true);
  const [nome, setNome] = useState("");
  const [serieId, setSerieId] = useState("");
  const [editId, setEditId] = useState<string | null>(null);
  const [editNome, setEditNome] = useState("");
  const [editSerieId, setEditSerieId] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [t, s] = await Promise.all([getTurmas(), listarSeries()]);
      setTurmas(t);
      setSeries(s);
      if (s.length > 0) {
        setSerieId((current) => current || s[0].id);
      }
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
        <AcademicoFormPanel title="Nova turma">
          <form
            className="space-y-3"
            onSubmit={(e) => {
              e.preventDefault();
              void runAction(() => criarTurma({ nome, serieId }), "Turma cadastrada.").then(() => setNome(""));
            }}
          >
            <Input label="Nome da turma" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <SelectField label="Serie" value={serieId} onChange={setSerieId}>
              {series.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome} — {s.nivelNome}
                </option>
              ))}
            </SelectField>
            <Button type="submit" disabled={saving || !serieId}>
              Cadastrar turma
            </Button>
          </form>
        </AcademicoFormPanel>

        <AcademicoListPanel title="Turmas cadastradas" count={turmas.length}>
          {turmas.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhuma turma cadastrada.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {turmas.map((t) => (
                <li key={t.id} className="py-3 first:pt-0 last:pb-0">
                  {editId === t.id ? (
                    <form
                      className="space-y-3"
                      onSubmit={(e) => {
                        e.preventDefault();
                        void runAction(
                          () => atualizarTurma(t.id, { nome: editNome, serieId: editSerieId }),
                          "Turma atualizada.",
                        ).then(() => setEditId(null));
                      }}
                    >
                      <Input label="Nome" value={editNome} onChange={(e) => setEditNome(e.target.value)} required />
                      <SelectField label="Serie" value={editSerieId} onChange={setEditSerieId}>
                        {series.map((s) => (
                          <option key={s.id} value={s.id}>
                            {s.nome} — {s.nivelNome}
                          </option>
                        ))}
                      </SelectField>
                      <div className="flex flex-wrap gap-2">
                        <Button type="submit" disabled={saving} size="sm">
                          Salvar
                        </Button>
                        <ActionButton type="button" variant="neutral" onClick={() => setEditId(null)}>
                          Cancelar
                        </ActionButton>
                      </div>
                    </form>
                  ) : (
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <p className="font-medium text-slate-900">{t.nome}</p>
                        <p className="text-sm text-slate-500">{t.serieNome}</p>
                      </div>
                      <span className="flex shrink-0 gap-2">
                        <ActionButton
                          type="button"
                          onClick={() => {
                            setEditId(t.id);
                            setEditNome(t.nome);
                            setEditSerieId(
                              t.serieId ?? series.find((s) => s.nome === t.serieNome)?.id ?? series[0]?.id ?? "",
                            );
                          }}
                        >
                          Editar
                        </ActionButton>
                        <ActionButton
                          type="button"
                          variant="danger"
                          onClick={() => void runAction(() => excluirTurma(t.id), "Turma excluida.")}
                        >
                          Excluir
                        </ActionButton>
                      </span>
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </AcademicoListPanel>
      </div>
    </AcademicoAlerts>
  );
}
