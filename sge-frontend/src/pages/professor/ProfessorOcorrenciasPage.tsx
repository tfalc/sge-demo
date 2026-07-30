import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { listarOcorrencias, registrarOcorrencia } from "../../services/ocorrenciaService";
import type { OcorrenciaDisciplinar, TipoOcorrencia } from "../../types";
import { useProfessorContext } from "./ProfessorContext";
import { ProfessorAlerts, ProfessorPanel } from "./ProfessorPageShell";
import { ProfessorTurmaSelectors } from "./ProfessorTurmaSelectors";

const TIPOS: { value: TipoOcorrencia; label: string }[] = [
  { value: "ADVERTENCIA", label: "Advertencia" },
  { value: "ATENCAO", label: "Atencao" },
  { value: "ELOGIO", label: "Elogio" },
  { value: "OUTRO", label: "Outro" },
];

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

function tipoLabel(tipo: TipoOcorrencia): string {
  return TIPOS.find((t) => t.value === tipo)?.label ?? tipo;
}

export function ProfessorOcorrenciasPage() {
  const { loading, error: ctxError, professorId, tdpId, alunos } = useProfessorContext();
  const [lista, setLista] = useState<OcorrenciaDisciplinar[]>([]);
  const [alunoId, setAlunoId] = useState("");
  const [dataOcorrencia, setDataOcorrencia] = useState(todayIso());
  const [tipo, setTipo] = useState<TipoOcorrencia>("ATENCAO");
  const [descricao, setDescricao] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    if (!tdpId) return;
    try {
      setLista(await listarOcorrencias(tdpId));
    } catch {
      setError("Falha ao carregar ocorrencias.");
    }
  }, [tdpId]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  useEffect(() => {
    if (alunos.length > 0 && !alunoId) setAlunoId(alunos[0].id);
  }, [alunos, alunoId]);

  async function handleRegistrar() {
    if (!tdpId || !alunoId || !descricao.trim()) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await registrarOcorrencia({
        alunoId,
        turmaDisciplinaProfessorId: tdpId,
        dataOcorrencia,
        tipo,
        descricao: descricao.trim(),
      });
      setDescricao("");
      setSuccess("Ocorrencia registrada. Coordenacao foi notificada.");
      await carregar();
    } catch {
      setError("Falha ao registrar ocorrencia.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <ProfessorAlerts error={error ?? ctxError} success={success} loading={loading}>
      {professorId ? (
        <div className="space-y-4">
          <ProfessorPanel title="Registrar ocorrencia">
            <p className="mb-4 text-sm text-slate-600">
              Registre advertencias, elogios ou situacoes que a coordenacao deve acompanhar.
            </p>
            <div className="space-y-4">
              <ProfessorTurmaSelectors />
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Aluno</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={alunoId}
                  onChange={(e) => setAlunoId(e.target.value)}
                >
                  {alunos.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nome}
                    </option>
                  ))}
                </select>
              </label>
              <Input
                label="Data"
                type="date"
                value={dataOcorrencia}
                onChange={(e) => setDataOcorrencia(e.target.value)}
              />
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Tipo</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={tipo}
                  onChange={(e) => setTipo(e.target.value as TipoOcorrencia)}
                >
                  {TIPOS.map((t) => (
                    <option key={t.value} value={t.value}>
                      {t.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Descricao</span>
                <textarea
                  className="min-h-24 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={descricao}
                  onChange={(e) => setDescricao(e.target.value)}
                  placeholder="Descreva a situacao..."
                />
              </label>
              <Button disabled={saving || !descricao.trim()} onClick={() => void handleRegistrar()}>
                {saving ? "Salvando..." : "Registrar ocorrencia"}
              </Button>
            </div>
          </ProfessorPanel>

          <ProfessorPanel title="Historico da turma/disciplina">
            {lista.length === 0 ? (
              <p className="text-sm text-slate-500">Nenhuma ocorrencia registrada.</p>
            ) : (
              <ul className="divide-y divide-slate-100 text-sm">
                {lista.map((o) => (
                  <li key={o.id} className="py-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-medium">{o.alunoNome}</span>
                      <span className="rounded bg-slate-100 px-2 py-0.5 text-xs">{tipoLabel(o.tipo)}</span>
                      <span className="text-slate-500">
                        {new Date(o.dataOcorrencia + "T12:00:00").toLocaleDateString("pt-BR")}
                      </span>
                    </div>
                    <p className="mt-1 text-slate-700">{o.descricao}</p>
                  </li>
                ))}
              </ul>
            )}
          </ProfessorPanel>
        </div>
      ) : null}
    </ProfessorAlerts>
  );
}
