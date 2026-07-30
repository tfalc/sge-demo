import { useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import {
  getAtaAula,
  getHistoricoAtas,
  getPresencasDaAula,
  lancarPresencas,
  salvarAtaAula,
} from "../../services/academicoService";
import type { AtaAulaResumo } from "../../types";
import { useProfessorContext } from "./ProfessorContext";
import { PresencaChamadaTable } from "./PresencaChamadaTable";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";
import { ProfessorTurmaSelectors } from "./ProfessorTurmaSelectors";

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export function ProfessorAtaPage() {
  const { loading, error: ctxError, professorId, tdpId, alunos } = useProfessorContext();
  const [dataAula, setDataAula] = useState(todayIso());
  const [conteudo, setConteudo] = useState("");
  const [tarefaCasa, setTarefaCasa] = useState("");
  const [observacoes, setObservacoes] = useState("");
  const [presencasInput, setPresencasInput] = useState<Record<string, boolean>>({});
  const [justificativasInput, setJustificativasInput] = useState<Record<string, string>>({});
  const [historico, setHistorico] = useState<AtaAulaResumo[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!tdpId) return;
    void getHistoricoAtas(tdpId).then(setHistorico).catch(() => setHistorico([]));
  }, [tdpId, success]);

  useEffect(() => {
    if (!tdpId || !dataAula) return;
    let cancelled = false;
    (async () => {
      try {
        const [ata, presencas] = await Promise.all([
          getAtaAula(tdpId, dataAula),
          getPresencasDaAula(tdpId, dataAula),
        ]);
        if (cancelled) return;
        setConteudo(ata?.conteudo ?? "");
        setTarefaCasa(ata?.tarefaCasa ?? "");
        setObservacoes(ata?.observacoes ?? "");
        const map: Record<string, boolean> = {};
        const just: Record<string, string> = {};
        alunos.forEach((a) => {
          map[a.id] = true;
        });
        presencas.forEach((p) => {
          map[p.alunoId] = p.presente;
          if (p.justificativa) just[p.alunoId] = p.justificativa;
        });
        setPresencasInput(map);
        setJustificativasInput(just);
      } catch {
        if (!cancelled) setError("Falha ao carregar ata ou chamada do dia.");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [tdpId, dataAula, alunos]);

  async function handleSalvar() {
    if (!tdpId || !dataAula) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await salvarAtaAula({
        turmaDisciplinaProfessorId: tdpId,
        dataAula,
        conteudo,
        tarefaCasa,
        observacoes,
      });
      if (alunos.length > 0) {
        await lancarPresencas({
          turmaDisciplinaProfessorId: tdpId,
          dataAula,
          presencas: alunos.map((aluno) => ({
            alunoId: aluno.id,
            presente: presencasInput[aluno.id] ?? true,
            justificativa:
              !(presencasInput[aluno.id] ?? true) && justificativasInput[aluno.id]?.trim()
                ? justificativasInput[aluno.id].trim()
                : undefined,
          })),
        });
      }
      setSuccess("Ata de aula e frequencia registradas.");
    } catch {
      setError("Falha ao salvar ata de aula.");
    } finally {
      setSaving(false);
    }
  }

  const displayError = error ?? ctxError;

  return (
    <ProfessorAlerts error={displayError} success={success} loading={loading}>
      {professorId ? (
        <div className="space-y-4">
          <ProfessorPanel title="Ata de aula">
            <p className="mb-4 text-sm text-slate-600">
              Registre o que foi ministrado, tarefa de casa e a chamada da turma na mesma ata.
            </p>
            <div className="space-y-4">
              <ProfessorTurmaSelectors />
              <Input label="Data da aula" type="date" value={dataAula} onChange={(e) => setDataAula(e.target.value)} />
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Conteudo ministrado</span>
                <textarea
                  className="min-h-24 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={conteudo}
                  onChange={(e) => setConteudo(e.target.value)}
                  placeholder="Topicos abordados na aula..."
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Tarefa de casa</span>
                <textarea
                  className="min-h-20 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={tarefaCasa}
                  onChange={(e) => setTarefaCasa(e.target.value)}
                  placeholder="Opcional"
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Observacoes</span>
                <textarea
                  className="min-h-20 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={observacoes}
                  onChange={(e) => setObservacoes(e.target.value)}
                  placeholder="Opcional"
                />
              </label>
            </div>
          </ProfessorPanel>

          <ProfessorPanel title="Chamada da turma">
            <PresencaChamadaTable
              alunos={alunos}
              presencasInput={presencasInput}
              justificativasInput={justificativasInput}
              onPresencaChange={(alunoId, presente) =>
                setPresencasInput((prev) => ({ ...prev, [alunoId]: presente }))
              }
              onJustificativaChange={(alunoId, value) =>
                setJustificativasInput((prev) => ({ ...prev, [alunoId]: value }))
              }
            />
          </ProfessorPanel>

          <Button disabled={saving || alunos.length === 0} onClick={() => void handleSalvar()}>
            {saving ? "Salvando..." : "Salvar ata de aula"}
          </Button>

          {historico.length > 0 ? (
            <ProfessorPanel title="Historico de atas (ultimos 60 dias)">
              <ul className="divide-y divide-slate-100 text-sm">
                {historico.map((item) => (
                  <li key={item.id} className="flex flex-wrap items-start justify-between gap-2 py-3">
                    <div>
                      <button
                        type="button"
                        className="font-medium text-brand-blue hover:underline"
                        onClick={() => setDataAula(item.dataAula)}
                      >
                        {new Date(item.dataAula + "T12:00:00").toLocaleDateString("pt-BR")}
                      </button>
                      <p className="mt-1 text-slate-600">{item.conteudoResumo ?? "Sem conteudo registrado."}</p>
                      {item.temTarefa ? (
                        <span className="mt-1 inline-block rounded bg-amber-50 px-2 py-0.5 text-xs text-amber-800">
                          Com tarefa
                        </span>
                      ) : null}
                    </div>
                  </li>
                ))}
              </ul>
              <p className="mt-2 text-xs text-slate-500">Clique na data para abrir a ata daquele dia.</p>
            </ProfessorPanel>
          ) : null}
        </div>
      ) : null}
    </ProfessorAlerts>
  );
}
