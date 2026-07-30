import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import {
  AcademicoAlerts,
  AcademicoFormPanel,
  AcademicoListPanel,
} from "./AcademicoPageShell";
import { useAcademicoAction } from "./useAcademicoAction";
import {
  atualizarDisciplina,
  criarDisciplina,
  excluirDisciplina,
  listarDisciplinas,
} from "../../services/academicoEstruturaService";
import type { DisciplinaCadastro } from "../../types";

export function SecretariaAcademicoDisciplinasPage() {
  const [disciplinas, setDisciplinas] = useState<DisciplinaCadastro[]>([]);
  const [loading, setLoading] = useState(true);
  const [nome, setNome] = useState("");
  const [codigo, setCodigo] = useState("");
  const [editId, setEditId] = useState<string | null>(null);
  const [editNome, setEditNome] = useState("");
  const [editCodigo, setEditCodigo] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setDisciplinas(await listarDisciplinas());
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
        <AcademicoFormPanel title="Nova disciplina">
          <form
            className="space-y-3"
            onSubmit={(e) => {
              e.preventDefault();
              void runAction(
                () => criarDisciplina({ nome, codigo: codigo || undefined }),
                "Disciplina cadastrada.",
              ).then(() => {
                setNome("");
                setCodigo("");
              });
            }}
          >
            <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input label="Codigo" value={codigo} onChange={(e) => setCodigo(e.target.value)} />
            <Button type="submit" disabled={saving}>
              Cadastrar disciplina
            </Button>
          </form>
        </AcademicoFormPanel>

        <AcademicoListPanel title="Disciplinas cadastradas" count={disciplinas.length}>
          {disciplinas.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhuma disciplina cadastrada.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {disciplinas.map((d) => (
                <li key={d.id} className="flex flex-wrap items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                  {editId === d.id ? (
                    <form
                      className="flex w-full flex-wrap items-end gap-3"
                      onSubmit={(e) => {
                        e.preventDefault();
                        void runAction(
                          () =>
                            atualizarDisciplina(d.id, {
                              nome: editNome,
                              codigo: editCodigo || undefined,
                            }),
                          "Disciplina atualizada.",
                        ).then(() => setEditId(null));
                      }}
                    >
                      <Input label="Nome" value={editNome} onChange={(e) => setEditNome(e.target.value)} required />
                      <Input label="Codigo" value={editCodigo} onChange={(e) => setEditCodigo(e.target.value)} />
                      <Button type="submit" disabled={saving} size="sm">
                        Salvar
                      </Button>
                      <ActionButton type="button" variant="neutral" onClick={() => setEditId(null)}>
                        Cancelar
                      </ActionButton>
                    </form>
                  ) : (
                    <>
                      <div>
                        <p className="font-medium text-slate-900">{d.nome}</p>
                        {d.codigo ? <p className="text-sm text-slate-500">Codigo: {d.codigo}</p> : null}
                      </div>
                      <span className="flex shrink-0 gap-2">
                        <ActionButton
                          type="button"
                          onClick={() => {
                            setEditId(d.id);
                            setEditNome(d.nome);
                            setEditCodigo(d.codigo ?? "");
                          }}
                        >
                          Editar
                        </ActionButton>
                        <ActionButton
                          type="button"
                          variant="danger"
                          onClick={() => void runAction(() => excluirDisciplina(d.id), "Disciplina excluida.")}
                        >
                          Excluir
                        </ActionButton>
                      </span>
                    </>
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
