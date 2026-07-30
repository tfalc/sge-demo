import { useCallback, useEffect, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { Button } from "../../components/ui/Button";
import {
  atualizarRematriculaConfig,
  downloadRematriculaModeloPdf,
  downloadRematriculaPdfPreenchido,
  detalheRematriculaSubmissao,
  getRematriculaConfig,
  listarRematriculasPendentes,
  uploadRematriculaModeloPdf,
  validarRematriculaSecretaria,
} from "../../services/rematriculaService";
import type {
  CampoFormularioRematricula,
  FormularioRematricula,
  RematriculaConfig,
  RematriculaRevisao,
  RematriculaSubmissaoResumo,
  SecaoFormularioRematricula,
} from "../../types";
import { useGestaoArea } from "./useGestaoArea";

function novoId(prefixo: string) {
  return `${prefixo}-${crypto.randomUUID().slice(0, 8)}`;
}

export function SecretariaRematriculaPage() {
  const { areaLabel, primaryNav } = useGestaoArea();
  const [config, setConfig] = useState<RematriculaConfig | null>(null);
  const [formulario, setFormulario] = useState<FormularioRematricula>({ secoes: [] });
  const [titulo, setTitulo] = useState("");
  const [habilitada, setHabilitada] = useState(false);
  const [pendentes, setPendentes] = useState<RematriculaSubmissaoResumo[]>([]);
  const [detalhe, setDetalhe] = useState<RematriculaRevisao | null>(null);
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    setErro(null);
    try {
      const [cfg, lista] = await Promise.all([getRematriculaConfig(), listarRematriculasPendentes()]);
      setConfig(cfg);
      setFormulario(cfg.formulario);
      setTitulo(cfg.titulo);
      setHabilitada(cfg.habilitada);
      setPendentes(lista);
    } catch {
      setErro("Nao foi possivel carregar a rematricula.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  async function handleSalvarConfig() {
    setSalvando(true);
    setMensagem(null);
    setErro(null);
    try {
      const atualizado = await atualizarRematriculaConfig({ titulo, habilitada, formulario });
      setConfig(atualizado);
      setMensagem(habilitada ? "Periodo de rematricula publicado para os pais." : "Configuracao salva.");
    } catch {
      setErro("Falha ao salvar configuracao.");
    } finally {
      setSalvando(false);
    }
  }

  async function handleUploadPdf(file: File | null) {
    if (!file) return;
    setUploading(true);
    setErro(null);
    try {
      const atualizado = await uploadRematriculaModeloPdf(file);
      setConfig(atualizado);
      setMensagem("PDF recebido. Revise as sugestoes de campos abaixo.");
    } catch {
      setErro("Falha ao enviar PDF.");
    } finally {
      setUploading(false);
    }
  }

  function adicionarSecao() {
    setFormulario((prev) => ({
      secoes: [
        ...prev.secoes,
        {
          id: novoId("sec"),
          titulo: "Nova secao",
          ordem: prev.secoes.length + 1,
          campos: [],
        },
      ],
    }));
  }

  function atualizarSecao(index: number, patch: Partial<SecaoFormularioRematricula>) {
    setFormulario((prev) => ({
      secoes: prev.secoes.map((s, i) => (i === index ? { ...s, ...patch } : s)),
    }));
  }

  function adicionarCampo(secaoIndex: number) {
    setFormulario((prev) => ({
      secoes: prev.secoes.map((s, i) => {
        if (i !== secaoIndex) return s;
        const campo: CampoFormularioRematricula = {
          id: novoId("campo"),
          rotulo: "Novo campo",
          tipo: "TEXTO",
          obrigatorio: false,
          ordem: s.campos.length + 1,
          opcoes: null,
        };
        return { ...s, campos: [...s.campos, campo] };
      }),
    }));
  }

  function adicionarCampoDeSugestao(texto: string) {
    if (formulario.secoes.length === 0) {
      setFormulario({
        secoes: [
          {
            id: novoId("sec"),
            titulo: "Campos do PDF",
            ordem: 1,
            campos: [
              {
                id: novoId("campo"),
                rotulo: texto,
                tipo: "TEXTO",
                obrigatorio: false,
                ordem: 1,
                opcoes: null,
              },
            ],
          },
        ],
      });
      return;
    }
    const ultima = formulario.secoes.length - 1;
    adicionarCampo(ultima);
    setFormulario((prev) => {
      const secoes = [...prev.secoes];
      const secao = secoes[ultima];
      const campos = [...secao.campos];
      campos[campos.length - 1] = { ...campos[campos.length - 1], rotulo: texto };
      secoes[ultima] = { ...secao, campos };
      return { secoes };
    });
  }

  function atualizarCampo(secaoIndex: number, campoIndex: number, patch: Partial<CampoFormularioRematricula>) {
    setFormulario((prev) => ({
      secoes: prev.secoes.map((s, si) => {
        if (si !== secaoIndex) return s;
        return {
          ...s,
          campos: s.campos.map((c, ci) => (ci === campoIndex ? { ...c, ...patch } : c)),
        };
      }),
    }));
  }

  async function handleValidarSubmissao(id: string) {
    try {
      await validarRematriculaSecretaria(id);
      setMensagem("Rematricula marcada como validada.");
      await carregar();
      setDetalhe(null);
    } catch {
      setErro("Falha ao validar submissao.");
    }
  }

  async function handleVerDetalhe(id: string) {
    try {
      setDetalhe(await detalheRematriculaSubmissao(id));
    } catch {
      setErro("Falha ao carregar detalhe.");
    }
  }

  async function handleDownloadModelo() {
    try {
      const blob = await downloadRematriculaModeloPdf();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = config?.pdfModeloNome ?? "modelo-rematricula.pdf";
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setErro("Modelo PDF nao disponivel.");
    }
  }

  async function handleDownloadPreenchido(id: string) {
    try {
      const blob = await downloadRematriculaPdfPreenchido(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "rematricula-preenchida.pdf";
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setErro("PDF preenchido nao disponivel.");
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Rematricula</h2>
        <p className="mt-1 text-sm text-slate-600">
          Envie o PDF modelo, ajuste secoes e campos, publique o periodo e valide formularios enviados pelos pais.
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

      {loading ? <p className="text-sm text-slate-600">Carregando...</p> : null}

      {!loading ? (
        <div className="grid gap-6 lg:grid-cols-2">
          <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="font-semibold text-slate-900">1. PDF modelo</h3>
            <p className="text-sm text-slate-600">
              O arquivo e processado apenas no servidor da escola. Linhas extraidas aparecem como sugestoes de campos.
            </p>
            <input
              type="file"
              accept="application/pdf"
              disabled={uploading}
              onChange={(e) => void handleUploadPdf(e.target.files?.[0] ?? null)}
              className="block w-full text-sm"
            />
            {config?.possuiModeloPdf ? (
              <Button type="button" variant="neutral" size="sm" onClick={() => void handleDownloadModelo()}>
                Baixar PDF modelo
              </Button>
            ) : null}

            {config?.sugestoesExtracao?.length ? (
              <div className="max-h-40 overflow-y-auto rounded border border-slate-100 bg-slate-50 p-2 text-xs">
                <p className="mb-2 font-medium text-slate-700">Sugestoes extraidas do PDF:</p>
                <ul className="space-y-1">
                  {config.sugestoesExtracao.slice(0, 15).map((linha) => (
                    <li key={linha} className="flex items-center justify-between gap-2">
                      <span className="truncate text-slate-600">{linha}</span>
                      <button
                        type="button"
                        className="shrink-0 text-brand-blue hover:underline"
                        onClick={() => adicionarCampoDeSugestao(linha)}
                      >
                        + campo
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
          </section>

          <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="font-semibold text-slate-900">2. Publicar periodo</h3>
            <label className="block text-sm">
              <span className="text-slate-700">Titulo exibido aos pais</span>
              <input
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
              />
            </label>
            <label className="flex items-center gap-2 text-sm text-slate-700">
              <input type="checkbox" checked={habilitada} onChange={(e) => setHabilitada(e.target.checked)} />
              Rematricula visivel no portal dos pais
            </label>
            {config?.anoLetivo ? (
              <p className="text-xs text-slate-500">Ano letivo vinculado: {config.anoLetivo}</p>
            ) : null}
          </section>
        </div>
      ) : null}

      {!loading ? (
        <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900">3. Formulario digital</h3>
            <Button type="button" variant="neutral" size="sm" onClick={adicionarSecao}>
              + Secao
            </Button>
          </div>

          {formulario.secoes.map((secao, si) => (
            <div key={secao.id} className="rounded-lg border border-slate-100 bg-slate-50 p-3">
              <input
                value={secao.titulo}
                onChange={(e) => atualizarSecao(si, { titulo: e.target.value })}
                className="mb-2 w-full rounded border border-slate-300 px-2 py-1 text-sm font-medium"
              />
              <div className="space-y-2">
                {secao.campos.map((campo, ci) => (
                  <div key={campo.id} className="grid gap-2 rounded bg-white p-2 md:grid-cols-4">
                    <input
                      value={campo.rotulo}
                      onChange={(e) => atualizarCampo(si, ci, { rotulo: e.target.value })}
                      className="rounded border border-slate-300 px-2 py-1 text-sm md:col-span-2"
                      placeholder="Rotulo"
                    />
                    <select
                      value={campo.tipo}
                      onChange={(e) =>
                        atualizarCampo(si, ci, {
                          tipo: e.target.value as CampoFormularioRematricula["tipo"],
                        })
                      }
                      className="rounded border border-slate-300 px-2 py-1 text-sm"
                    >
                      <option value="TEXTO">Texto</option>
                      <option value="TEXTO_LONGO">Texto longo</option>
                      <option value="BOOLEAN">Sim/Nao</option>
                      <option value="DATA">Data</option>
                      <option value="SELECAO">Selecao</option>
                    </select>
                    <label className="flex items-center gap-1 text-xs">
                      <input
                        type="checkbox"
                        checked={campo.obrigatorio}
                        onChange={(e) => atualizarCampo(si, ci, { obrigatorio: e.target.checked })}
                      />
                      Obrigatorio
                    </label>
                    {campo.tipo === "SELECAO" ? (
                      <input
                        value={(campo.opcoes ?? []).join(", ")}
                        onChange={(e) =>
                          atualizarCampo(si, ci, {
                            opcoes: e.target.value.split(",").map((o) => o.trim()).filter(Boolean),
                          })
                        }
                        className="rounded border border-slate-300 px-2 py-1 text-sm md:col-span-4"
                        placeholder="Opcoes separadas por virgula"
                      />
                    ) : null}
                  </div>
                ))}
              </div>
              <Button type="button" variant="neutral" size="sm" className="mt-2" onClick={() => adicionarCampo(si)}>
                + Campo
              </Button>
            </div>
          ))}

          <Button type="button" disabled={salvando} onClick={() => void handleSalvarConfig()}>
            {salvando ? "Salvando..." : "Salvar configuracao"}
          </Button>
        </section>
      ) : null}

      {!loading ? (
        <section className="space-y-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="font-semibold text-slate-900">4. Aguardando validacao ({pendentes.length})</h3>
          {pendentes.length === 0 ? (
            <p className="text-sm text-slate-600">Nenhum formulario enviado ainda.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {pendentes.map((item) => (
                <li key={item.id} className="flex flex-wrap items-center justify-between gap-2 py-3">
                  <div>
                    <p className="font-medium text-slate-900">{item.alunoNome}</p>
                    <p className="text-xs text-slate-500">
                      {item.turmaNome ?? "Sem turma"} · enviado em{" "}
                      {item.enviadoEm ? new Date(item.enviadoEm).toLocaleString("pt-BR") : "-"}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <Button type="button" variant="neutral" size="sm" onClick={() => void handleVerDetalhe(item.id)}>
                      Ver respostas
                    </Button>
                    <Button type="button" variant="neutral" size="sm" onClick={() => void handleDownloadPreenchido(item.id)}>
                      PDF
                    </Button>
                    <Button type="button" size="sm" onClick={() => void handleValidarSubmissao(item.id)}>
                      Validar
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}

          {detalhe ? (
            <div className="mt-4 rounded-lg border border-brand-blue/20 bg-blue-50/50 p-4">
              <h4 className="font-medium text-slate-900">{detalhe.alunoNome}</h4>
              {detalhe.secoes.map((secao) => (
                <div key={secao.titulo} className="mt-3">
                  <p className="text-sm font-semibold text-slate-800">{secao.titulo}</p>
                  <ul className="mt-1 space-y-1 text-sm text-slate-700">
                    {secao.campos.map((c) => (
                      <li key={c.campoId}>
                        <span className="text-slate-500">{c.rotulo}:</span> {c.valorExibido}
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          ) : null}
        </section>
      ) : null}
    </div>
  );
}
