import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { ActionButton } from "../../components/ui/ActionButton";
import { Input } from "../../components/ui/Input";
import { listarUsuarios, type UsuarioAdmin } from "../../services/adminService";
import {
  atualizarEncaminhamentoColegiado,
  concluirReuniaoColegiado,
  criarEncaminhamentoColegiado,
  criarReuniaoColegiado,
  listarReunioesColegiado,
  obterPainelColegiado,
  obterReuniaoColegiado,
  type EncaminhamentoColegiado,
  type PainelColegiadoDados,
  type ReuniaoColegiadoDetalhe,
  type ReuniaoColegiadoResumo,
} from "../../services/colegiadoService";
import type { Turma } from "../../types";

type Props = {
  turmas: Turma[];
  turmaId: string;
};

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export function CoordenacaoColegiadosPanel({ turmas, turmaId }: Props) {
  const [reunioes, setReunioes] = useState<ReuniaoColegiadoResumo[]>([]);
  const [selecionadaId, setSelecionadaId] = useState<string | null>(null);
  const [detalhe, setDetalhe] = useState<ReuniaoColegiadoDetalhe | null>(null);
  const [painel, setPainel] = useState<PainelColegiadoDados | null>(null);
  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [titulo, setTitulo] = useState("");
  const [dataReuniao, setDataReuniao] = useState(todayIso());
  const [horaReuniao, setHoraReuniao] = useState("19:00");
  const [pautaNova, setPautaNova] = useState("");
  const [turmaNovaId, setTurmaNovaId] = useState(turmaId);
  const [participantesSel, setParticipantesSel] = useState<string[]>([]);

  const [encDescricao, setEncDescricao] = useState("");
  const [encResponsavelId, setEncResponsavelId] = useState("");
  const [encPrazo, setEncPrazo] = useState("");

  const carregarLista = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const lista = await listarReunioesColegiado(turmaId || undefined);
      setReunioes(lista);
    } catch {
      setError("Falha ao carregar reunioes de colegiado.");
    } finally {
      setLoading(false);
    }
  }, [turmaId]);

  const carregarDetalhe = useCallback(async (id: string) => {
    try {
      const [r, p] = await Promise.all([obterReuniaoColegiado(id), obterPainelColegiado(id)]);
      setDetalhe(r);
      setPainel(p);
      setSelecionadaId(id);
    } catch {
      setError("Falha ao carregar reuniao.");
    }
  }, []);

  useEffect(() => {
    void carregarLista();
  }, [carregarLista]);

  useEffect(() => {
    setTurmaNovaId(turmaId);
  }, [turmaId]);

  useEffect(() => {
    void listarUsuarios()
      .then(setUsuarios)
      .catch(() => undefined);
  }, []);

  async function handleCriarReuniao(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const criada = await criarReuniaoColegiado({
        titulo,
        turmaId: turmaNovaId || undefined,
        dataReuniao,
        horaReuniao,
        pauta: pautaNova || undefined,
        participanteUsuarioIds: participantesSel,
      });
      setTitulo("");
      setPautaNova("");
      setParticipantesSel([]);
      setSuccess("Reuniao agendada.");
      await carregarLista();
      await carregarDetalhe(criada.id);
    } catch {
      setError("Falha ao criar reuniao.");
    } finally {
      setSaving(false);
    }
  }

  async function handleConcluir() {
    if (!selecionadaId) return;
    setSaving(true);
    setError(null);
    try {
      await concluirReuniaoColegiado(selecionadaId);
      setSuccess("Reuniao concluida e ata gerada.");
      await carregarLista();
      await carregarDetalhe(selecionadaId);
    } catch {
      setError("Falha ao concluir reuniao.");
    } finally {
      setSaving(false);
    }
  }

  async function handleEncaminhamento(e: React.FormEvent) {
    e.preventDefault();
    if (!selecionadaId || !encDescricao.trim()) return;
    setSaving(true);
    setError(null);
    try {
      await criarEncaminhamentoColegiado(selecionadaId, {
        descricao: encDescricao,
        responsavelUsuarioId: encResponsavelId || undefined,
        prazo: encPrazo || undefined,
      });
      setEncDescricao("");
      setEncPrazo("");
      setSuccess("Encaminhamento registrado.");
      await carregarDetalhe(selecionadaId);
    } catch {
      setError("Falha ao criar encaminhamento.");
    } finally {
      setSaving(false);
    }
  }

  async function handleStatusEnc(enc: EncaminhamentoColegiado, status: "CONCLUIDO" | "PENDENTE") {
    try {
      await atualizarEncaminhamentoColegiado(enc.id, status);
      if (selecionadaId) await carregarDetalhe(selecionadaId);
    } catch {
      setError("Falha ao atualizar encaminhamento.");
    }
  }

  function toggleParticipante(id: string) {
    setParticipantesSel((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  }

  return (
    <div className="space-y-4">
      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      <div className="grid gap-6 xl:grid-cols-2">
        <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="text-base font-semibold text-slate-900">Nova reuniao</h3>
          <form className="mt-4 space-y-3" onSubmit={(e) => void handleCriarReuniao(e)}>
            <Input label="Titulo" value={titulo} onChange={(e) => setTitulo(e.target.value)} required />
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Turma (opcional)</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                value={turmaNovaId}
                onChange={(e) => setTurmaNovaId(e.target.value)}
              >
                <option value="">Geral / todas</option>
                {turmas.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nome} — {t.serieNome}
                  </option>
                ))}
              </select>
            </label>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input
                label="Data"
                type="date"
                value={dataReuniao}
                onChange={(e) => setDataReuniao(e.target.value)}
                required
              />
              <Input
                label="Hora"
                type="time"
                value={horaReuniao}
                onChange={(e) => setHoraReuniao(e.target.value)}
              />
            </div>
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Pauta</span>
              <textarea
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                rows={3}
                value={pautaNova}
                onChange={(e) => setPautaNova(e.target.value)}
              />
            </label>
            {usuarios.length > 0 ? (
              <div className="text-sm">
                <p className="mb-2 font-medium text-slate-700">Participantes</p>
                <div className="max-h-32 space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
                  {usuarios
                    .filter((u) => u.ativo && u.perfil !== "PAI" && u.perfil !== "ALUNO")
                    .map((u) => (
                      <label key={u.id} className="flex items-center gap-2">
                        <input
                          type="checkbox"
                          checked={participantesSel.includes(u.id)}
                          onChange={() => toggleParticipante(u.id)}
                        />
                        <span>
                          {u.nome ?? u.email} ({u.perfil})
                        </span>
                      </label>
                    ))}
                </div>
              </div>
            ) : null}
            <Button type="submit" size="sm" disabled={saving}>
              {saving ? "Salvando..." : "Agendar reuniao"}
            </Button>
          </form>

          <h4 className="mt-6 text-sm font-semibold text-slate-800">Reunioes</h4>
          {loading ? (
            <p className="mt-2 text-sm text-slate-500">Carregando...</p>
          ) : reunioes.length === 0 ? (
            <p className="mt-2 text-sm text-slate-600">Nenhuma reuniao cadastrada.</p>
          ) : (
            <ul className="mt-2 divide-y divide-slate-100 text-sm">
              {reunioes.map((r) => (
                <li key={r.id}>
                  <button
                    type="button"
                    className={`w-full px-2 py-2 text-left hover:bg-slate-50 ${
                      selecionadaId === r.id ? "bg-slate-100 font-medium" : ""
                    }`}
                    onClick={() => void carregarDetalhe(r.id)}
                  >
                    <span>{r.titulo}</span>
                    <span className="ml-2 text-slate-500">
                      {new Date(r.dataReuniao + "T12:00:00").toLocaleDateString("pt-BR")} — {r.status}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="space-y-4">
          {!detalhe ? (
            <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-12 text-center text-sm text-slate-600">
              Selecione ou crie uma reuniao para ver pauta, dados e encaminhamentos.
            </div>
          ) : (
            <>
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <h3 className="text-base font-semibold text-slate-900">{detalhe.titulo}</h3>
                    <p className="text-sm text-slate-600">
                      {detalhe.turmaNome ? `${detalhe.turmaNome} — ` : ""}
                      {new Date(detalhe.dataReuniao + "T12:00:00").toLocaleDateString("pt-BR")}
                      {detalhe.horaReuniao ? ` as ${detalhe.horaReuniao}` : ""} — {detalhe.status}
                    </p>
                  </div>
                  {detalhe.status === "AGENDADA" ? (
                    <Button size="sm" disabled={saving} onClick={() => void handleConcluir()}>
                      Concluir e gerar ata
                    </Button>
                  ) : null}
                </div>
                {detalhe.pauta ? (
                  <p className="mt-3 text-sm text-slate-700 whitespace-pre-wrap">{detalhe.pauta}</p>
                ) : null}
                {detalhe.ataTexto ? (
                  <pre className="mt-4 max-h-48 overflow-auto rounded bg-slate-50 p-3 text-xs text-slate-800 whitespace-pre-wrap">
                    {detalhe.ataTexto}
                  </pre>
                ) : null}
              </div>

              {painel ? (
                <div className="rounded-xl border border-blue-200 bg-blue-50/40 p-5 shadow-sm">
                  <h4 className="text-sm font-semibold text-slate-900">Painel de dados da pauta</h4>
                  {painel.mensagem ? (
                    <p className="mt-2 text-sm text-slate-600">{painel.mensagem}</p>
                  ) : (
                    <div className="mt-3 grid gap-3 sm:grid-cols-2 text-sm">
                      <div>
                        <p className="font-medium text-slate-700">Em risco (notas)</p>
                        <ul className="mt-1 list-disc pl-4 text-slate-600">
                          {painel.alunosEmRiscoNota.length === 0 ? (
                            <li>Nenhum</li>
                          ) : (
                            painel.alunosEmRiscoNota.map((a) => (
                              <li key={a.alunoId}>
                                {a.alunoNome} (media {a.mediaGeral.toFixed(1)})
                              </li>
                            ))
                          )}
                        </ul>
                      </div>
                      <div>
                        <p className="font-medium text-slate-700">Em risco (frequencia)</p>
                        <ul className="mt-1 list-disc pl-4 text-slate-600">
                          {painel.alunosEmRiscoFrequencia.length === 0 ? (
                            <li>Nenhum</li>
                          ) : (
                            painel.alunosEmRiscoFrequencia.map((a) => (
                              <li key={a.alunoId}>
                                {a.alunoNome} ({a.percentual.toFixed(1)}%)
                              </li>
                            ))
                          )}
                        </ul>
                      </div>
                    </div>
                  )}
                  {painel.ocorrenciasRecentes?.length > 0 ? (
                    <p className="mt-3 text-xs text-slate-600">
                      {painel.ocorrenciasRecentes.length} ocorrencia(s) recente(s) na turma.
                    </p>
                  ) : null}
                </div>
              ) : null}

              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                <h4 className="text-sm font-semibold text-slate-900">Encaminhamentos</h4>
                {detalhe.encaminhamentos.length === 0 ? (
                  <p className="mt-2 text-sm text-slate-600">Nenhum encaminhamento.</p>
                ) : (
                  <ul className="mt-2 space-y-2 text-sm">
                    {detalhe.encaminhamentos.map((enc) => (
                      <li
                        key={enc.id}
                        className="flex flex-wrap items-center justify-between gap-2 rounded border border-slate-100 p-2"
                      >
                        <div>
                          <p className="font-medium">{enc.descricao}</p>
                          <p className="text-xs text-slate-500">
                            {enc.responsavelNome ?? "—"}
                            {enc.prazo ? ` — prazo ${enc.prazo}` : ""} — {enc.status}
                          </p>
                        </div>
                        {enc.status === "PENDENTE" ? (
                          <ActionButton
                            type="button"
                            onClick={() => void handleStatusEnc(enc, "CONCLUIDO")}
                          >
                            Concluir
                          </ActionButton>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                )}

                {detalhe.status !== "CANCELADA" ? (
                  <form className="mt-4 grid gap-2" onSubmit={(e) => void handleEncaminhamento(e)}>
                    <Input
                      label="Novo encaminhamento"
                      value={encDescricao}
                      onChange={(e) => setEncDescricao(e.target.value)}
                      required
                    />
                    <label className="block text-sm">
                      <span className="mb-1 block font-medium text-slate-700">Responsavel</span>
                      <select
                        className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                        value={encResponsavelId}
                        onChange={(e) => setEncResponsavelId(e.target.value)}
                      >
                        <option value="">Selecione...</option>
                        {usuarios
                          .filter((u) => u.ativo)
                          .map((u) => (
                            <option key={u.id} value={u.id}>
                              {u.nome ?? u.email}
                            </option>
                          ))}
                      </select>
                    </label>
                    <Input
                      label="Prazo"
                      type="date"
                      value={encPrazo}
                      onChange={(e) => setEncPrazo(e.target.value)}
                    />
                    <Button type="submit" size="sm" disabled={saving}>
                      Adicionar
                    </Button>
                  </form>
                ) : null}
              </div>
            </>
          )}
        </section>
      </div>
    </div>
  );
}
