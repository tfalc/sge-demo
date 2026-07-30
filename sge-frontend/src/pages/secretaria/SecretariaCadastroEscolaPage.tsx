import { useCallback, useEffect, useState } from "react";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { atualizarEscola, getEscola } from "../../services/cadastroService";
import type { EscolaCadastro } from "../../types";
import { CadastroAlerts, CadastroPanel } from "./CadastroPageShell";

export function SecretariaCadastroEscolaPage() {
  const [escola, setEscola] = useState<EscolaCadastro | null>(null);
  const [nome, setNome] = useState("");
  const [cnpj, setCnpj] = useState("");
  const [notaMinima, setNotaMinima] = useState("6");
  const [frequenciaMinima, setFrequenciaMinima] = useState("75");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const e = await getEscola();
      setEscola(e);
      setNome(e.nome);
      setCnpj(e.cnpj ?? "");
      setNotaMinima(String(e.notaMinimaAprovacao ?? 6));
      setFrequenciaMinima(String(e.frequenciaMinima ?? 75));
    } catch {
      setError("Falha ao carregar dados da escola.");
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
      await atualizarEscola({
        nome,
        cnpj: cnpj || undefined,
        notaMinimaAprovacao: Number(notaMinima),
        frequenciaMinima: Number(frequenciaMinima),
      });
      setSuccess("Dados da escola atualizados.");
      await load();
    } catch {
      setError("Falha ao atualizar escola.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <CadastroAlerts error={error} success={success} loading={loading}>
      {escola ? (
        <div className="grid items-start gap-6 xl:grid-cols-2">
          <CadastroPanel title="Instituicao">
            <p className="mb-4 text-sm text-slate-600">
              Este sistema opera com uma unica escola por instalacao. Ajuste aqui nome, CNPJ e criterios academicos.
            </p>
            <form className="grid gap-3" onSubmit={(e) => void handleSubmit(e)}>
              <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
              <Input label="CNPJ" value={cnpj} onChange={(e) => setCnpj(e.target.value)} />
              <Input
                label="Nota minima para aprovacao"
                type="number"
                min={0}
                max={10}
                step={0.1}
                value={notaMinima}
                onChange={(e) => setNotaMinima(e.target.value)}
                required
              />
              <Input
                label="Frequencia minima (%)"
                type="number"
                min={0}
                max={100}
                step={0.1}
                value={frequenciaMinima}
                onChange={(e) => setFrequenciaMinima(e.target.value)}
                required
              />
              <div>
                <Button type="submit" disabled={saving}>
                  {saving ? "Salvando..." : "Salvar escola"}
                </Button>
              </div>
            </form>
          </CadastroPanel>

          <CadastroPanel title="Identidade do pacote">
            <dl className="space-y-3 text-sm">
              {escola.municipio ? (
                <div>
                  <dt className="font-medium text-slate-700">Municipio</dt>
                  <dd className="text-slate-600">
                    {escola.municipio}
                    {escola.uf ? ` / ${escola.uf}` : ""}
                  </dd>
                </div>
              ) : null}
              {escola.slug ? (
                <div>
                  <dt className="font-medium text-slate-700">Identificador</dt>
                  <dd className="font-mono text-slate-600">{escola.slug}</dd>
                </div>
              ) : null}
              <div>
                <dt className="font-medium text-slate-700">Normativa curricular</dt>
                <dd className="text-slate-600">Consulte e aplique em Secretaria → Matriz.</dd>
              </div>
            </dl>
          </CadastroPanel>
        </div>
      ) : null}
    </CadastroAlerts>
  );
}
