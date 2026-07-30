import { useCallback, useEffect, useMemo, useState } from "react";
import { SectionNav } from "../../components/layout/SectionNav";
import { Button } from "../../components/ui/Button";
import {
  confirmarRematricula,
  getRematriculaPortal,
  revisarRematricula,
  salvarRematriculaRascunho,
} from "../../services/rematriculaService";
import type {
  AlunoRematriculaPortal,
  CampoFormularioRematricula,
  FormularioRematricula,
  RematriculaPortal,
  RematriculaRevisao,
} from "../../types";
import { parentNav } from "./parentNav";

type Etapa = "formulario" | "revisao" | "concluido";

function CampoInput({
  campo,
  valor,
  onChange,
  disabled,
}: {
  campo: CampoFormularioRematricula;
  valor: unknown;
  onChange: (v: unknown) => void;
  disabled?: boolean;
}) {
  const id = campo.id;

  if (campo.tipo === "BOOLEAN") {
    return (
      <label className="flex items-center gap-2 text-sm text-slate-800">
        <input
          type="checkbox"
          id={id}
          checked={Boolean(valor)}
          disabled={disabled}
          onChange={(e) => onChange(e.target.checked)}
        />
        {campo.rotulo}
        {campo.obrigatorio ? <span className="text-red-600">*</span> : null}
      </label>
    );
  }

  if (campo.tipo === "TEXTO_LONGO") {
    return (
      <label className="block text-sm">
        <span className="text-slate-700">
          {campo.rotulo}
          {campo.obrigatorio ? <span className="text-red-600"> *</span> : null}
        </span>
        <textarea
          id={id}
          rows={3}
          disabled={disabled}
          value={String(valor ?? "")}
          onChange={(e) => onChange(e.target.value)}
          className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
        />
      </label>
    );
  }

  if (campo.tipo === "SELECAO") {
    return (
      <label className="block text-sm">
        <span className="text-slate-700">
          {campo.rotulo}
          {campo.obrigatorio ? <span className="text-red-600"> *</span> : null}
        </span>
        <select
          id={id}
          disabled={disabled}
          value={String(valor ?? "")}
          onChange={(e) => onChange(e.target.value)}
          className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
        >
          <option value="">Selecione</option>
          {(campo.opcoes ?? []).map((op) => (
            <option key={op} value={op}>
              {op}
            </option>
          ))}
        </select>
      </label>
    );
  }

  const inputType = campo.tipo === "DATA" ? "date" : "text";

  return (
    <label className="block text-sm">
      <span className="text-slate-700">
        {campo.rotulo}
        {campo.obrigatorio ? <span className="text-red-600"> *</span> : null}
      </span>
      <input
        id={id}
        type={inputType}
        disabled={disabled}
        value={String(valor ?? "")}
        onChange={(e) => onChange(e.target.value)}
        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
      />
    </label>
  );
}

export function ParentRematriculaPage() {
  const [portal, setPortal] = useState<RematriculaPortal | null>(null);
  const [alunoId, setAlunoId] = useState("");
  const [respostas, setRespostas] = useState<Record<string, unknown>>({});
  const [revisao, setRevisao] = useState<RematriculaRevisao | null>(null);
  const [etapa, setEtapa] = useState<Etapa>("formulario");
  const [loading, setLoading] = useState(true);
  const [processando, setProcessando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [mensagem, setMensagem] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    setErro(null);
    try {
      const data = await getRematriculaPortal();
      setPortal(data);
      if (data.alunos.length > 0) {
        const primeiro = data.alunos[0];
        setAlunoId(primeiro.alunoId);
        setRespostas(primeiro.respostas ?? {});
        if (primeiro.statusSubmissao === "ENVIADO" || primeiro.statusSubmissao === "VALIDADO_SECRETARIA") {
          setEtapa("concluido");
        } else {
          setEtapa("formulario");
        }
      }
    } catch {
      setErro("Nao foi possivel carregar a rematricula.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const alunoSelecionado: AlunoRematriculaPortal | undefined = useMemo(
    () => portal?.alunos.find((a) => a.alunoId === alunoId),
    [portal, alunoId],
  );

  const formulario: FormularioRematricula | null = portal?.formulario ?? null;
  const bloqueado =
    alunoSelecionado?.statusSubmissao === "ENVIADO" ||
    alunoSelecionado?.statusSubmissao === "VALIDADO_SECRETARIA";

  function selecionarAluno(id: string) {
    const aluno = portal?.alunos.find((a) => a.alunoId === id);
    setAlunoId(id);
    setRespostas(aluno?.respostas ?? {});
    setRevisao(null);
    setErro(null);
    setMensagem(null);
    if (aluno?.statusSubmissao === "ENVIADO" || aluno?.statusSubmissao === "VALIDADO_SECRETARIA") {
      setEtapa("concluido");
    } else {
      setEtapa("formulario");
    }
  }

  function atualizarResposta(campoId: string, valor: unknown) {
    setRespostas((prev) => ({ ...prev, [campoId]: valor }));
  }

  async function handleSalvarRascunho() {
    if (!alunoId) return;
    setProcessando(true);
    setErro(null);
    try {
      await salvarRematriculaRascunho(alunoId, respostas);
      setMensagem("Rascunho salvo. Voce pode continuar depois.");
    } catch {
      setErro("Falha ao salvar rascunho.");
    } finally {
      setProcessando(false);
    }
  }

  async function handleValidar() {
    if (!alunoId) return;
    setProcessando(true);
    setErro(null);
    setMensagem(null);
    try {
      const resultado = await revisarRematricula(alunoId, respostas);
      if (resultado.erros.length > 0) {
        setErro(resultado.erros.join(" · "));
        return;
      }
      setRevisao(resultado);
      setEtapa("revisao");
    } catch (e) {
      setErro("Revise os campos obrigatorios antes de continuar.");
    } finally {
      setProcessando(false);
    }
  }

  async function handleConfirmar() {
    if (!alunoId) return;
    setProcessando(true);
    setErro(null);
    try {
      await confirmarRematricula(alunoId);
      setEtapa("concluido");
      setMensagem("Formulario enviado! A secretaria foi notificada para validacao.");
      await carregar();
    } catch {
      setErro("Nao foi possivel confirmar o envio.");
    } finally {
      setProcessando(false);
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais — Rematricula</h2>
        <p className="mt-1 text-sm text-slate-600">
          Preencha o formulario, valide as informacoes e confirme o envio para a secretaria.
        </p>
      </div>

      <SectionNav items={parentNav} />

      {loading ? <p className="text-sm text-slate-600">Carregando...</p> : null}

      {!loading && portal && !portal.habilitada ? (
        <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          O periodo de rematricula ainda nao foi aberto pela escola.
        </div>
      ) : null}

      {!loading && portal?.habilitada ? (
        <>
          {mensagem ? (
            <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
              {mensagem}
            </div>
          ) : null}
          {erro ? (
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{erro}</div>
          ) : null}

          {portal.alunos.length > 1 ? (
            <label className="block max-w-md text-sm">
              <span className="text-slate-700">Aluno</span>
              <select
                value={alunoId}
                onChange={(e) => selecionarAluno(e.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
              >
                {portal.alunos.map((a) => (
                  <option key={a.alunoId} value={a.alunoId}>
                    {a.alunoNome} {a.turmaNome ? `(${a.turmaNome})` : ""}
                  </option>
                ))}
              </select>
            </label>
          ) : null}

          <p className="text-sm font-medium text-brand-blue">{portal.titulo}</p>

          {etapa === "concluido" || bloqueado ? (
            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
              <p className="text-sm text-slate-800">
                Formulario de <strong>{alunoSelecionado?.alunoNome}</strong> ja foi enviado
                {alunoSelecionado?.statusSubmissao === "VALIDADO_SECRETARIA"
                  ? " e validado pela secretaria."
                  : " e aguarda validacao da secretaria."}
              </p>
            </div>
          ) : null}

          {etapa === "formulario" && !bloqueado && formulario ? (
            <div className="space-y-6">
              {formulario.secoes.map((secao) => (
                <section key={secao.id} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                  <h3 className="font-semibold text-slate-900">{secao.titulo}</h3>
                  <div className="mt-3 space-y-3">
                    {secao.campos.map((campo) => (
                      <CampoInput
                        key={campo.id}
                        campo={campo}
                        valor={respostas[campo.id]}
                        onChange={(v) => atualizarResposta(campo.id, v)}
                      />
                    ))}
                  </div>
                </section>
              ))}

              <div className="flex flex-wrap gap-2">
                <Button type="button" variant="neutral" disabled={processando} onClick={() => void handleSalvarRascunho()}>
                  Salvar rascunho
                </Button>
                <Button type="button" disabled={processando} onClick={() => void handleValidar()}>
                  {processando ? "Validando..." : "Validar"}
                </Button>
              </div>
            </div>
          ) : null}

          {etapa === "revisao" && revisao ? (
            <div className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
              <h3 className="font-semibold text-slate-900">Confira os dados antes de enviar</h3>
              <p className="text-sm text-slate-600">
                Aluno: <strong>{revisao.alunoNome}</strong>
              </p>
              {revisao.secoes.map((secao) => (
                <div key={secao.titulo}>
                  <p className="text-sm font-semibold text-slate-800">{secao.titulo}</p>
                  <dl className="mt-1 space-y-1 text-sm">
                    {secao.campos.map((c) => (
                      <div key={c.campoId} className="grid grid-cols-[1fr_auto] gap-2 border-b border-slate-50 py-1">
                        <dt className="text-slate-500">{c.rotulo}</dt>
                        <dd className="font-medium text-slate-900">{c.valorExibido}</dd>
                      </div>
                    ))}
                  </dl>
                </div>
              ))}
              <div className="flex flex-wrap gap-2 pt-2">
                <Button type="button" variant="neutral" onClick={() => setEtapa("formulario")}>
                  Voltar e editar
                </Button>
                <Button type="button" disabled={processando} onClick={() => void handleConfirmar()}>
                  {processando ? "Enviando..." : "Confirmar e enviar"}
                </Button>
              </div>
              <p className="text-xs text-slate-500">
                Ao confirmar, um PDF preenchido sera gerado e a secretaria recebera um alerta. Assinatura gov.br: fase
                futura.
              </p>
            </div>
          ) : null}
        </>
      ) : null}
    </div>
  );
}
