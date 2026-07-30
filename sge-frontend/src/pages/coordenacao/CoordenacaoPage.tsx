import { useCallback, useEffect, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Button } from "../../components/ui/Button";
import { getTurmas } from "../../services/academicoService";
import {
  getAnaliseAluno,
  getTurmaDesempenho,
  getTurmaFrequencia,
} from "../../services/relatoriosService";
import type { AnaliseAluno, Turma, TurmaDesempenho, TurmaFrequencia } from "../../types";
import { downloadCsv } from "../../utils/csvExport";
import { CoordenacaoColegiadosPanel } from "./CoordenacaoColegiadosPanel";
import { CoordenacaoGaleriaPanel } from "./CoordenacaoGaleriaPanel";
import { CoordenacaoSupervisaoPanel } from "./CoordenacaoSupervisaoPanel";

function situacaoLabel(s?: AnaliseAluno["situacao"]) {
  if (s === "OTIMO") return "Dentro do esperado";
  if (s === "CRITICO") return "Atencao urgente";
  return "Requer acompanhamento";
}

function situacaoClass(s?: AnaliseAluno["situacao"]) {
  if (s === "OTIMO") return "bg-emerald-100 text-emerald-900";
  if (s === "CRITICO") return "bg-red-100 text-red-900";
  return "bg-amber-100 text-amber-900";
}

export function CoordenacaoPage() {
  const [aba, setAba] = useState<"indicadores" | "supervisao" | "colegiados" | "galeria">("indicadores");
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [turmaId, setTurmaId] = useState("");
  const [desempenho, setDesempenho] = useState<TurmaDesempenho | null>(null);
  const [frequencia, setFrequencia] = useState<TurmaFrequencia | null>(null);
  const [analise, setAnalise] = useState<AnaliseAluno | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTurma = useCallback(async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      const [d, f] = await Promise.all([getTurmaDesempenho(id), getTurmaFrequencia(id)]);
      setDesempenho(d);
      setFrequencia(f);
      setAnalise(null);
    } catch {
      setError("Nao foi possivel carregar relatorios da turma.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void (async () => {
      try {
        const t = await getTurmas();
        setTurmas(t);
        if (t.length > 0) {
          setTurmaId(t[0].id);
          await loadTurma(t[0].id);
        }
      } catch {
        setError("Falha ao carregar turmas.");
        setLoading(false);
      }
    })();
  }, [loadTurma]);

  async function handleAnalise(alunoId: string) {
    try {
      const a = await getAnaliseAluno(alunoId);
      setAnalise(a);
    } catch {
      setError("Falha ao gerar analise do aluno.");
    }
  }

  function handleExportCsv() {
    if (!desempenho || !frequencia) return;
    const freqByAluno = new Map(frequencia.alunos.map((a) => [a.alunoId, a]));
    downloadCsv(
      `turma-${turmaId}-relatorio.csv`,
      ["Aluno", "Media", "Em risco (notas)", "Frequencia %", "Em risco (freq)"],
      desempenho.alunos.map((a) => {
        const f = freqByAluno.get(a.alunoId);
        return [
          a.alunoNome,
          a.mediaGeral.toFixed(2),
          a.emRisco ? "Sim" : "Nao",
          f ? f.percentual.toFixed(1) : "",
          f?.emRisco ? "Sim" : "Nao",
        ];
      }),
    );
  }

  const chartNotas =
    desempenho?.alunos.map((a) => ({
      nome: a.alunoNome.split(" ")[0],
      media: a.mediaGeral,
    })) ?? [];

  const chartFreq =
    frequencia?.alunos.map((a) => ({
      nome: a.alunoNome.split(" ")[0],
      percentual: a.percentual,
    })) ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Coordenacao Pedagogica</h2>
        <p className="mt-1 text-sm text-slate-600">
          Dashboard de desempenho, supervisao do diario e analise inteligente embutida.
        </p>
      </div>

      <div className="flex gap-2 border-b border-slate-200">
        <button
          type="button"
          className={`px-4 py-2 text-sm font-medium ${
            aba === "indicadores"
              ? "border-b-2 border-brand-blue text-brand-blue"
              : "text-slate-600 hover:text-slate-900"
          }`}
          onClick={() => setAba("indicadores")}
        >
          Indicadores
        </button>
        <button
          type="button"
          className={`px-4 py-2 text-sm font-medium ${
            aba === "supervisao"
              ? "border-b-2 border-brand-blue text-brand-blue"
              : "text-slate-600 hover:text-slate-900"
          }`}
          onClick={() => setAba("supervisao")}
        >
          Diario e ocorrencias
        </button>
        <button
          type="button"
          className={`px-4 py-2 text-sm font-medium ${
            aba === "colegiados"
              ? "border-b-2 border-brand-blue text-brand-blue"
              : "text-slate-600 hover:text-slate-900"
          }`}
          onClick={() => setAba("colegiados")}
        >
          Colegiados
        </button>
        <button
          type="button"
          className={`px-4 py-2 text-sm font-medium ${
            aba === "galeria"
              ? "border-b-2 border-brand-blue text-brand-blue"
              : "text-slate-600 hover:text-slate-900"
          }`}
          onClick={() => setAba("galeria")}
        >
          Galeria
        </button>
      </div>

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}

      {aba !== "galeria" ? (
      <label className="block max-w-xs text-sm">
        <span className="mb-1 block font-medium text-slate-700">Turma</span>
        <select
          className="w-full rounded-lg border border-slate-300 px-3 py-2"
          value={turmaId}
          onChange={(e) => {
            setTurmaId(e.target.value);
            void loadTurma(e.target.value);
          }}
        >
          {turmas.map((t) => (
            <option key={t.id} value={t.id}>
              {t.nome} — {t.serieNome}
            </option>
          ))}
        </select>
      </label>
      ) : null}

      {loading && aba !== "galeria" ? (
        <p className="text-sm text-slate-500">Carregando...</p>
      ) : aba === "galeria" ? (
        <CoordenacaoGaleriaPanel />
      ) : aba === "colegiados" ? (
        <CoordenacaoColegiadosPanel turmas={turmas} turmaId={turmaId} />
      ) : aba === "supervisao" && turmaId ? (
        <CoordenacaoSupervisaoPanel turmaId={turmaId} />
      ) : desempenho && frequencia ? (
        <>
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div className="grid flex-1 gap-4 sm:grid-cols-3">
              <SummaryCard label="Media da turma" value={desempenho.mediaTurma.toFixed(2)} />
              <SummaryCard label="Risco (notas)" value={String(desempenho.alunosEmRisco)} />
              <SummaryCard label="Risco (frequencia)" value={String(frequencia.alunosEmRisco)} />
            </div>
            <Button className="shrink-0" variant="neutral" onClick={handleExportCsv}>
              Exportar CSV
            </Button>
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <ChartCard title="Medias por aluno">
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={chartNotas}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nome" fontSize={12} />
                  <YAxis domain={[0, 10]} fontSize={12} />
                  <Tooltip />
                  <Bar dataKey="media" fill="#0c2d57" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
            <ChartCard title="Frequencia (%)">
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={chartFreq}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nome" fontSize={12} />
                  <YAxis domain={[0, 100]} fontSize={12} />
                  <Tooltip />
                  <Bar dataKey="percentual" fill="#28a7e8" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>

          <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Analise inteligente individual</h3>
            <p className="mt-1 text-xs text-slate-500">
              Motor embutido no SGE — indice, tags e sugestoes para coordenacao e familia.
            </p>
            <div className="mt-3 flex flex-wrap gap-2">
              {desempenho.alunos.map((a) => (
                <Button
                  key={a.alunoId}
                  className="!px-3 !py-1.5 text-xs"
                  variant={a.emRisco ? "neutral" : "brand"}
                  onClick={() => void handleAnalise(a.alunoId)}
                >
                  {a.alunoNome}
                  {a.emRisco ? " ⚠" : ""}
                </Button>
              ))}
            </div>
            {analise ? (
              <div className="mt-4 space-y-4 rounded-lg bg-slate-50 p-4 text-sm text-slate-800">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="text-base font-semibold">{analise.alunoNome}</p>
                  {analise.score != null ? (
                    <span className="rounded-full bg-[#0c2d57] px-2.5 py-0.5 text-xs font-bold text-white">
                      {analise.score}/100
                    </span>
                  ) : null}
                  {analise.situacao ? (
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${situacaoClass(analise.situacao)}`}
                    >
                      {situacaoLabel(analise.situacao)}
                    </span>
                  ) : null}
                </div>

                {analise.tags?.length ? (
                  <div className="flex flex-wrap gap-1">
                    {analise.tags.map((tag) => (
                      <span
                        key={tag}
                        className="rounded-md border border-slate-200 bg-white px-2 py-0.5 text-xs text-slate-600"
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                ) : null}

                <div className="grid gap-4 md:grid-cols-2">
                  {analise.pontosFortes?.length ? (
                    <AnaliseLista titulo="Pontos fortes" itens={analise.pontosFortes} />
                  ) : null}
                  {analise.pontosAtencao?.length ? (
                    <AnaliseLista titulo="Pontos de atencao" itens={analise.pontosAtencao} />
                  ) : null}
                  {analise.sugestoesCoordenacao?.length ? (
                    <AnaliseLista titulo="Coordenacao" itens={analise.sugestoesCoordenacao} />
                  ) : null}
                  {analise.sugestoesFamilia?.length ? (
                    <AnaliseLista titulo="Familia" itens={analise.sugestoesFamilia} />
                  ) : null}
                </div>

                <details className="rounded-lg border border-slate-200 bg-white p-3">
                  <summary className="cursor-pointer font-medium text-slate-700">
                    Relatorio completo
                  </summary>
                  <pre className="mt-2 whitespace-pre-wrap font-sans text-xs text-slate-600">
                    {analise.relatorio}
                  </pre>
                </details>
              </div>
            ) : null}
          </section>
        </>
      ) : null}
    </div>
  );
}

function AnaliseLista({ titulo, itens }: { titulo: string; itens: string[] }) {
  return (
    <div>
      <h4 className="font-semibold text-slate-800">{titulo}</h4>
      <ul className="mt-1 list-inside list-disc text-slate-600">
        {itens.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

function SummaryCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-sm text-slate-600">{label}</p>
      <p className="mt-1 text-2xl font-bold text-slate-900">{value}</p>
    </div>
  );
}

function ChartCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <h3 className="mb-3 text-sm font-semibold text-slate-800">{title}</h3>
      {children}
    </div>
  );
}
