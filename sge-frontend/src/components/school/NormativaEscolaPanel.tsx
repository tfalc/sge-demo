import { useState } from "react";
import { Button } from "../ui/Button";
import { Modal } from "../ui/Modal";
import {
  aplicarNormativa,
  getNormativa,
  previewAplicarNormativa,
  type NormativaAlteracao,
  type NormativaEscola,
  type NormativaPreview,
} from "../../services/schoolService";

type Props = {
  onAplicada?: () => void;
};

export function NormativaEscolaPanel({ onAplicada }: Props) {
  const [open, setOpen] = useState(false);
  const [normativa, setNormativa] = useState<NormativaEscola | null>(null);
  const [preview, setPreview] = useState<NormativaPreview | null>(null);
  const [loading, setLoading] = useState(false);
  const [applying, setApplying] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [step, setStep] = useState<"consulta" | "preview">("consulta");

  async function abrirConsulta() {
    setOpen(true);
    setStep("consulta");
    setError(null);
    setSuccess(null);
    setLoading(true);
    try {
      const n = await getNormativa();
      setNormativa(n);
    } catch {
      setError("Nao foi possivel carregar a normativa do pacote da escola.");
    } finally {
      setLoading(false);
    }
  }

  async function irParaPreview() {
    setLoading(true);
    setError(null);
    try {
      const p = await previewAplicarNormativa();
      setPreview(p);
      setStep("preview");
    } catch {
      setError("Nao foi possivel gerar o preview de aplicacao.");
    } finally {
      setLoading(false);
    }
  }

  async function confirmarAplicacao() {
    setApplying(true);
    setError(null);
    try {
      const result = await aplicarNormativa();
      const criadas = result.matrizesCriadas ?? result.matrizesSincronizadas ?? 0;
      const preservadas = result.matrizesPreservadas ?? 0;
      setSuccess(
        `Normativa aplicada. Matrizes novas: ${String(criadas)}; preservadas: ${String(preservadas)}; planos: ${String(result.planosAtualizados)}.`,
      );
      setStep("consulta");
      onAplicada?.();
      const n = await getNormativa();
      setNormativa(n);
    } catch {
      setError("Falha ao aplicar normativa.");
    } finally {
      setApplying(false);
    }
  }

  return (
    <>
      <div className="flex flex-wrap gap-2">
        <Button type="button" variant="neutral" onClick={() => void abrirConsulta()}>
          Consultar normativa
        </Button>
      </div>

      <Modal
        open={open}
        title={step === "consulta" ? "Normativa vigente" : "Aplicar normativa"}
        onClose={() => setOpen(false)}
        footer={
          step === "consulta" ? (
            <div className="flex justify-end gap-2">
              <Button type="button" variant="neutral" onClick={() => setOpen(false)}>
                Fechar
              </Button>
              <Button type="button" onClick={() => void irParaPreview()} disabled={loading}>
                Aplicar ao sistema
              </Button>
            </div>
          ) : (
            <div className="flex justify-end gap-2">
              <Button type="button" variant="neutral" onClick={() => setStep("consulta")}>
                Voltar
              </Button>
              <Button type="button" onClick={() => void confirmarAplicacao()} disabled={applying}>
                {applying ? "Aplicando..." : "Confirmar aplicacao"}
              </Button>
            </div>
          )
        }
      >
        {error ? (
          <div className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
            {error}
          </div>
        ) : null}
        {success ? (
          <div className="mb-3 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
            {success}
          </div>
        ) : null}

        {loading ? <p className="text-sm text-slate-500">Carregando...</p> : null}

        {step === "consulta" && normativa && !loading ? (
          <div className="max-h-[60vh] space-y-4 overflow-y-auto text-sm">
            <p className="text-slate-600">
              Fonte: <span className="font-mono text-xs">{normativa.fonte}</span>
            </p>
            {normativa.avisoPreservacao ? (
              <p className="rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-sky-900">
                {normativa.avisoPreservacao}
              </p>
            ) : null}
            {normativa.resumo?.map((linha) => (
              <p key={linha} className="text-slate-700">
                • {linha}
              </p>
            ))}
            <NormativaLista titulo="Nacional" itens={normativa.normativa?.nacional} />
            <NormativaLista titulo="Estadual (RJ)" itens={normativa.normativa?.estadual} />
            <NormativaLista
              titulo="Municipal (referencia)"
              itens={normativa.normativa?.municipal_referencia}
            />
            {normativa.matrizesPacote?.length ? (
              <div>
                <h4 className="font-semibold text-slate-800">Matrizes no pacote</h4>
                <ul className="mt-2 space-y-1 text-slate-600">
                  {normativa.matrizesPacote.map((m) => (
                    <li key={m.codigo}>
                      <span className="font-medium">{m.nome}</span> — {m.modoValidacao}
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
          </div>
        ) : null}

        {step === "preview" && preview && !loading ? (
          <div className="space-y-3 text-sm">
            <p className="text-slate-600">
              Aplicacao alinha regras da escola e planos ao pacote{" "}
              <strong>{preview.packageId}</strong>. Matrizes ja salvas permanecem intactas; so entram
              matrizes ausentes, vigentes apos a consulta.
            </p>
            {preview.preservacaoMatrizes ? (
              <p className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700">
                {preview.preservacaoMatrizes}
              </p>
            ) : null}
            {preview.alteracoes.length === 0 ? (
              <p className="text-emerald-700">Tudo ja esta alinhado ao pacote.</p>
            ) : (
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b text-slate-500">
                    <th className="py-1 pr-2">Area</th>
                    <th className="py-1 pr-2">Campo</th>
                    <th className="py-1 pr-2">Atual</th>
                    <th className="py-1">Novo</th>
                  </tr>
                </thead>
                <tbody>
                  {preview.alteracoes.map((a: NormativaAlteracao, idx) => (
                    <tr key={`${a.area}-${a.campo}-${idx}`} className="border-b border-slate-100">
                      <td className="py-1 pr-2">{a.area}</td>
                      <td className="py-1 pr-2">{a.campo}</td>
                      <td className="py-1 pr-2 text-slate-600">{String(a.atual)}</td>
                      <td className="py-1 font-medium">{String(a.novo)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        ) : null}
      </Modal>
    </>
  );
}

function NormativaLista({ titulo, itens }: { titulo: string; itens?: string[] }) {
  if (!itens?.length) return null;
  return (
    <div>
      <h4 className="font-semibold text-slate-800">{titulo}</h4>
      <ul className="mt-1 list-inside list-disc text-slate-600">
        {itens.map((i) => (
          <li key={i}>{i}</li>
        ))}
      </ul>
    </div>
  );
}
