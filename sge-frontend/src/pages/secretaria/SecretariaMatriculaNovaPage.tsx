import { useCallback, useEffect, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { getTurmas } from "../../services/academicoService";
import { listarResponsaveis } from "../../services/cadastroService";
import {
  aprovarProcessoMatricula,
  atualizarProcessoMatricula,
  concluirProcessoMatricula,
  criarProcessoMatricula,
  downloadDocumentoMatricula,
  enviarProcessoMatricula,
  excluirDocumentoMatricula,
  listarAnosLetivosMatricula,
  listarProcessosMatricula,
  obterProcessoMatricula,
  rejeitarProcessoMatricula,
  uploadDocumentoMatricula,
} from "../../services/matriculaNovaService";
import type {
  MatriculaProcessoResumo,
  ResponsavelCadastro,
  StatusMatriculaProcesso,
  TipoDocumentoMatricula,
  Turma,
} from "../../types";
import { useGestaoArea } from "./useGestaoArea";

const STATUS_LABEL: Record<StatusMatriculaProcesso, string> = {
  RASCUNHO: "Rascunho",
  EM_ANALISE: "Em analise",
  APROVADO: "Aprovado",
  REJEITADO: "Rejeitado",
  CONCLUIDO: "Concluido",
};

const TIPOS_DOC: { value: TipoDocumentoMatricula; label: string }[] = [
  { value: "RG", label: "RG" },
  { value: "CPF", label: "CPF" },
  { value: "COMPROVANTE_RESIDENCIA", label: "Comprovante residencia" },
  { value: "CERTIDAO_NASCIMENTO", label: "Certidao nascimento" },
  { value: "FOTO", label: "Foto" },
  { value: "OUTRO", label: "Outro" },
];

const emptyForm = {
  candidatoNome: "",
  matriculaSugerida: "",
  turmaPretendidaId: "",
  responsavelId: "",
  responsavelNome: "",
  responsavelEmail: "",
  responsavelTelefone: "",
  observacoes: "",
};

export function SecretariaMatriculaNovaPage() {
  const { areaLabel, primaryNav } = useGestaoArea();
  const [processos, setProcessos] = useState<MatriculaProcessoResumo[]>([]);
  const [selecionadoId, setSelecionadoId] = useState<string | null>(null);
  const [detalhe, setDetalhe] = useState<MatriculaProcessoResumo | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [filtroStatus, setFiltroStatus] = useState<StatusMatriculaProcesso | "">("");
  const [anos, setAnos] = useState<{ id: string; ano: number }[]>([]);
  const [anoLetivoId, setAnoLetivoId] = useState("");
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [responsaveis, setResponsaveis] = useState<ResponsavelCadastro[]>([]);
  const [tipoDoc, setTipoDoc] = useState<TipoDocumentoMatricula>("RG");
  const [motivoRejeicao, setMotivoRejeicao] = useState("");
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  const carregarLista = useCallback(async () => {
    setProcessos(await listarProcessosMatricula(filtroStatus || undefined));
  }, [filtroStatus]);

  const carregarDetalhe = useCallback(async (id: string) => {
    const d = await obterProcessoMatricula(id);
    setDetalhe(d);
    setForm({
      candidatoNome: d.candidatoNome,
      matriculaSugerida: d.matriculaSugerida ?? "",
      turmaPretendidaId: d.turmaPretendidaId ?? "",
      responsavelId: d.responsavelId ?? "",
      responsavelNome: d.responsavelNome ?? "",
      responsavelEmail: d.responsavelEmail ?? "",
      responsavelTelefone: d.responsavelTelefone ?? "",
      observacoes: d.observacoes ?? "",
    });
  }, []);

  useEffect(() => {
    void (async () => {
      setLoading(true);
      try {
        const [a, t, r] = await Promise.all([
          listarAnosLetivosMatricula(),
          getTurmas(),
          listarResponsaveis(),
        ]);
        setAnos(a);
        if (a.length > 0) setAnoLetivoId(a[0].id);
        setTurmas(t);
        setResponsaveis(r);
        await carregarLista();
      } catch {
        setErro("Falha ao carregar matricula nova.");
      } finally {
        setLoading(false);
      }
    })();
  }, [carregarLista]);

  useEffect(() => {
    if (selecionadoId) void carregarDetalhe(selecionadoId).catch(() => setErro("Falha ao carregar processo."));
    else setDetalhe(null);
  }, [selecionadoId, carregarDetalhe]);

  async function handleNovo() {
    if (!anoLetivoId || !form.candidatoNome.trim()) {
      setErro("Informe ano letivo e nome do candidato.");
      return;
    }
    setSalvando(true);
    setErro(null);
    try {
      const criado = await criarProcessoMatricula({
        anoLetivoId,
        turmaPretendidaId: form.turmaPretendidaId || undefined,
        responsavelId: form.responsavelId || undefined,
        candidatoNome: form.candidatoNome,
        matriculaSugerida: form.matriculaSugerida || undefined,
        responsavelNome: form.responsavelNome || undefined,
        responsavelEmail: form.responsavelEmail || undefined,
        responsavelTelefone: form.responsavelTelefone || undefined,
        observacoes: form.observacoes || undefined,
      });
      setSelecionadoId(criado.id);
      setMensagem("Processo criado em rascunho.");
      await carregarLista();
    } catch {
      setErro("Falha ao criar processo.");
    } finally {
      setSalvando(false);
    }
  }

  async function handleSalvar() {
    if (!selecionadoId) return;
    setSalvando(true);
    setErro(null);
    try {
      await atualizarProcessoMatricula(selecionadoId, {
        turmaPretendidaId: form.turmaPretendidaId || undefined,
        responsavelId: form.responsavelId || undefined,
        candidatoNome: form.candidatoNome,
        matriculaSugerida: form.matriculaSugerida || undefined,
        responsavelNome: form.responsavelNome || undefined,
        responsavelEmail: form.responsavelEmail || undefined,
        responsavelTelefone: form.responsavelTelefone || undefined,
        observacoes: form.observacoes || undefined,
      });
      setMensagem("Dados salvos.");
      await carregarDetalhe(selecionadoId);
      await carregarLista();
    } catch {
      setErro("Falha ao salvar.");
    } finally {
      setSalvando(false);
    }
  }

  async function acao(fn: () => Promise<MatriculaProcessoResumo>, msg: string) {
    if (!selecionadoId) return;
    setSalvando(true);
    setErro(null);
    try {
      await fn();
      setMensagem(msg);
      await carregarDetalhe(selecionadoId);
      await carregarLista();
    } catch {
      setErro("Operacao falhou.");
    } finally {
      setSalvando(false);
    }
  }

  async function handleUpload(file: File | null) {
    if (!selecionadoId || !file) return;
    setSalvando(true);
    try {
      await uploadDocumentoMatricula(selecionadoId, tipoDoc, file);
      setMensagem("Documento enviado.");
      await carregarDetalhe(selecionadoId);
    } catch {
      setErro("Falha no upload.");
    } finally {
      setSalvando(false);
    }
  }

  const editavel =
    detalhe?.status === "RASCUNHO" || detalhe?.status === "EM_ANALISE" || !detalhe;

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Matricula nova</h2>
        <p className="mt-1 text-sm text-slate-600">
          Processo de ingresso com documentos (GED). Apos aprovacao, conclua para criar o aluno no cadastro.
        </p>
      </div>

      <SectionNav items={primaryNav} />

      {mensagem ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {mensagem}
        </div>
      ) : null}
      {erro ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{erro}</div>
      ) : null}

      <div className="flex flex-wrap items-end gap-3">
        <label className="text-sm">
          <span className="mb-1 block font-medium text-slate-700">Filtrar status</span>
          <select
            className="rounded-lg border border-slate-300 px-3 py-2"
            value={filtroStatus}
            onChange={(e) => setFiltroStatus(e.target.value as StatusMatriculaProcesso | "")}
          >
            <option value="">Todos</option>
            {(Object.keys(STATUS_LABEL) as StatusMatriculaProcesso[]).map((s) => (
              <option key={s} value={s}>
                {STATUS_LABEL[s]}
              </option>
            ))}
          </select>
        </label>
        <Button
          variant="neutral"
          size="sm"
          className="!rounded-lg !py-2 !text-sm"
          onClick={() => {
            setSelecionadoId(null);
            setForm(emptyForm);
            setMensagem(null);
          }}
        >
          Novo candidato
        </Button>
      </div>

      {loading ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="font-semibold text-slate-900">Processos ({processos.length})</h3>
            <ul className="mt-3 max-h-96 divide-y divide-slate-100 overflow-y-auto text-sm">
              {processos.map((p) => (
                <li key={p.id}>
                  <button
                    type="button"
                    className={`w-full px-2 py-3 text-left hover:bg-slate-50 ${
                      selecionadoId === p.id ? "bg-slate-100" : ""
                    }`}
                    onClick={() => setSelecionadoId(p.id)}
                  >
                    <span className="font-medium">{p.candidatoNome}</span>
                    <span className="ml-2 rounded bg-slate-200 px-2 py-0.5 text-xs">
                      {STATUS_LABEL[p.status]}
                    </span>
                    {p.turmaPretendidaNome ? (
                      <span className="mt-1 block text-slate-500">{p.turmaPretendidaNome}</span>
                    ) : null}
                  </button>
                </li>
              ))}
            </ul>
          </section>

          <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="font-semibold text-slate-900">
              {detalhe ? `Processo — ${STATUS_LABEL[detalhe.status]}` : "Novo processo"}
            </h3>

            {!selecionadoId ? (
              <label className="block text-sm">
                <span className="mb-1 block font-medium">Ano letivo</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={anoLetivoId}
                  onChange={(e) => setAnoLetivoId(e.target.value)}
                >
                  {anos.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.ano}
                    </option>
                  ))}
                </select>
              </label>
            ) : null}

            <Input
              label="Nome do candidato"
              value={form.candidatoNome}
              disabled={!editavel}
              onChange={(e) => setForm((f) => ({ ...f, candidatoNome: e.target.value }))}
            />
            <Input
              label="Matricula sugerida (opcional)"
              value={form.matriculaSugerida}
              disabled={!editavel}
              onChange={(e) => setForm((f) => ({ ...f, matriculaSugerida: e.target.value }))}
            />
            <label className="block text-sm">
              <span className="mb-1 block font-medium">Turma pretendida</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={form.turmaPretendidaId}
                disabled={!editavel}
                onChange={(e) => setForm((f) => ({ ...f, turmaPretendidaId: e.target.value }))}
              >
                <option value="">Selecione...</option>
                {turmas.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nome} — {t.serieNome}
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm">
              <span className="mb-1 block font-medium">Responsavel cadastrado (opcional)</span>
              <select
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={form.responsavelId}
                disabled={!editavel}
                onChange={(e) => setForm((f) => ({ ...f, responsavelId: e.target.value }))}
              >
                <option value="">Nenhum / informar abaixo</option>
                {responsaveis.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.nome} — {r.email ?? r.usuarioEmail}
                  </option>
                ))}
              </select>
            </label>
            <Input
              label="Nome responsavel (referencia)"
              value={form.responsavelNome}
              disabled={!editavel}
              onChange={(e) => setForm((f) => ({ ...f, responsavelNome: e.target.value }))}
            />
            <Input
              label="E-mail responsavel"
              value={form.responsavelEmail}
              disabled={!editavel}
              onChange={(e) => setForm((f) => ({ ...f, responsavelEmail: e.target.value }))}
            />
            <Input
              label="Telefone"
              value={form.responsavelTelefone}
              disabled={!editavel}
              onChange={(e) => setForm((f) => ({ ...f, responsavelTelefone: e.target.value }))}
            />
            <label className="block text-sm">
              <span className="mb-1 block font-medium">Observacoes</span>
              <textarea
                className="min-h-20 w-full rounded-lg border border-slate-300 px-3 py-2"
                value={form.observacoes}
                disabled={!editavel}
                onChange={(e) => setForm((f) => ({ ...f, observacoes: e.target.value }))}
              />
            </label>

            <div className="flex flex-wrap gap-2">
              {!selecionadoId ? (
                <Button disabled={salvando} onClick={() => void handleNovo()}>
                  Criar rascunho
                </Button>
              ) : editavel ? (
                <Button disabled={salvando} onClick={() => void handleSalvar()}>
                  Salvar
                </Button>
              ) : null}
              {detalhe?.status === "RASCUNHO" ? (
                <Button
                  variant="neutral"
                  disabled={salvando}
                  onClick={() => void acao(() => enviarProcessoMatricula(selecionadoId!), "Enviado para analise.")}
                >
                  Enviar para analise
                </Button>
              ) : null}
              {detalhe?.status === "EM_ANALISE" ? (
                <>
                  <Button
                    disabled={salvando}
                    onClick={() => void acao(() => aprovarProcessoMatricula(selecionadoId!), "Aprovado.")}
                  >
                    Aprovar
                  </Button>
                  <div className="flex w-full flex-wrap items-end gap-2">
                    <Input
                      label="Motivo rejeicao"
                      value={motivoRejeicao}
                      onChange={(e) => setMotivoRejeicao(e.target.value)}
                    />
                    <Button
                      variant="neutral"
                      disabled={salvando || !motivoRejeicao.trim()}
                      onClick={() =>
                        void acao(
                          () => rejeitarProcessoMatricula(selecionadoId!, motivoRejeicao),
                          "Processo rejeitado.",
                        )
                      }
                    >
                      Rejeitar
                    </Button>
                  </div>
                </>
              ) : null}
              {detalhe?.status === "APROVADO" ? (
                <Button
                  disabled={salvando}
                  onClick={() =>
                    void acao(() => concluirProcessoMatricula(selecionadoId!), "Aluno criado no cadastro.")
                  }
                >
                  Concluir e criar aluno
                </Button>
              ) : null}
            </div>

            {detalhe?.motivoRejeicao ? (
              <p className="text-sm text-red-700">Motivo: {detalhe.motivoRejeicao}</p>
            ) : null}
            {detalhe?.alunoId ? (
              <p className="text-sm text-emerald-700">Aluno cadastrado (ID vinculado ao processo).</p>
            ) : null}

            {selecionadoId && detalhe?.status !== "CONCLUIDO" && detalhe?.status !== "REJEITADO" ? (
              <div className="border-t border-slate-100 pt-4">
                <h4 className="font-medium text-slate-900">Documentos (GED)</h4>
                <div className="mt-2 flex flex-wrap items-end gap-2">
                  <label className="text-sm">
                    <span className="mb-1 block">Tipo</span>
                    <select
                      className="rounded-lg border border-slate-300 px-3 py-2"
                      value={tipoDoc}
                      onChange={(e) => setTipoDoc(e.target.value as TipoDocumentoMatricula)}
                    >
                      {TIPOS_DOC.map((t) => (
                        <option key={t.value} value={t.value}>
                          {t.label}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="text-sm">
                    <span className="mb-1 block">Arquivo</span>
                    <input
                      type="file"
                      accept=".pdf,.jpg,.jpeg,.png"
                      onChange={(e) => void handleUpload(e.target.files?.[0] ?? null)}
                    />
                  </label>
                </div>
              </div>
            ) : null}

            {detalhe?.documentos && detalhe.documentos.length > 0 ? (
              <ul className="divide-y divide-slate-100 text-sm">
                {detalhe.documentos.map((doc) => (
                  <li key={doc.id} className="flex flex-wrap items-center justify-between gap-2 py-2">
                    <span>
                      {doc.tipo} — {doc.nomeArquivo}
                    </span>
                    <div className="flex gap-2">
                      <Button
                        variant="neutral"
                        onClick={() =>
                          void downloadDocumentoMatricula(selecionadoId!, doc.id).then((blob) => {
                            const url = URL.createObjectURL(blob);
                            const a = document.createElement("a");
                            a.href = url;
                            a.download = doc.nomeArquivo;
                            a.click();
                            URL.revokeObjectURL(url);
                          })
                        }
                      >
                        Baixar
                      </Button>
                      {editavel ? (
                        <Button
                          variant="neutral"
                          onClick={() =>
                            void excluirDocumentoMatricula(selecionadoId!, doc.id).then(() =>
                              carregarDetalhe(selecionadoId!),
                            )
                          }
                        >
                          Excluir
                        </Button>
                      ) : null}
                    </div>
                  </li>
                ))}
              </ul>
            ) : null}
          </section>
        </div>
      )}
    </div>
  );
}
