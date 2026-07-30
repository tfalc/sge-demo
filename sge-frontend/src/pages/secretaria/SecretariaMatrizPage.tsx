import { useCallback, useEffect, useState } from "react";

import { NormativaEscolaPanel } from "../../components/school/NormativaEscolaPanel";

import { SectionNav } from "../../components/layout/SectionNav";

import { getTurmas } from "../../services/academicoService";

import {

  listarMatrizes,

  obterMatriz,

  validarTurmaMatriz,

  type MatrizCurricular,

  type ValidacaoTurma,

} from "../../services/matrizService";

import type { Turma } from "../../types";

import { useGestaoArea } from "./useGestaoArea";



function labelAulas(matriz: MatrizCurricular | null) {

  if (!matriz) return "";

  if (matriz.modoValidacao === "DIRETRIZES" && matriz.aulasSemanaisTotalMin != null) {

    return `${matriz.aulasSemanaisTotalMin}–${matriz.aulasSemanaisTotalMax ?? matriz.aulasSemanaisTotal} aulas/semana (flexivel)`;

  }

  return `${matriz.aulasSemanaisTotal} aulas/semana (normativo)`;

}



export function SecretariaMatrizPage() {
  const { areaLabel, primaryNav } = useGestaoArea();
  const [matrizes, setMatrizes] = useState<MatrizCurricular[]>([]);

  const [matrizId, setMatrizId] = useState("");

  const [detalhe, setDetalhe] = useState<MatrizCurricular | null>(null);

  const [turmas, setTurmas] = useState<Turma[]>([]);

  const [turmaId, setTurmaId] = useState("");

  const [validacao, setValidacao] = useState<ValidacaoTurma | null>(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);



  const load = useCallback(async () => {

    setLoading(true);

    setError(null);

    try {

      const [m, t] = await Promise.all([listarMatrizes(), getTurmas()]);

      setMatrizes(m);

      setTurmas(t);

      if (m.length > 0) {

        setMatrizId(m[0].id);

      }

      if (t.length > 0) {

        setTurmaId(t[0].id);

      }

    } catch {

      setError("Nao foi possivel carregar matrizes curriculares.");

    } finally {

      setLoading(false);

    }

  }, []);



  useEffect(() => {

    void load();

  }, [load]);



  useEffect(() => {

    if (!matrizId) return;

    void obterMatriz(matrizId)

      .then(setDetalhe)

      .catch(() => setDetalhe(null));

  }, [matrizId]);



  async function onValidar() {

    if (!turmaId || !matrizId) return;

    setError(null);

    try {

      const v = await validarTurmaMatriz(turmaId, matrizId);

      setValidacao(v);

    } catch {

      setError("Validacao indisponivel para esta turma.");

      setValidacao(null);

    }

  }



  const isDiretrizes = detalhe?.modoValidacao === "DIRETRIZES";



  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Matriz curricular</h2>
          <p className="mt-1 text-sm text-slate-600">
            Modo <strong>normativo</strong> (Res. 4746) ou <strong>diretrizes</strong> (faixas BNCC/RJ).
          </p>
        </div>
        <NormativaEscolaPanel onAplicada={() => void load()} />
      </header>

      <SectionNav items={primaryNav} />



      {error ? (

        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">

          {error}

        </div>

      ) : null}



      {loading ? (

        <p className="text-sm text-slate-500">Carregando...</p>

      ) : (

        <>

          <section className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">

            <label className="block text-sm font-semibold text-slate-700">Matriz de referencia</label>

            <select

              className="mt-2 w-full rounded-xl border border-slate-300 px-3 py-2 text-sm"

              value={matrizId}

              onChange={(e) => setMatrizId(e.target.value)}

            >

              {matrizes.map((m) => (

                <option key={m.id} value={m.id}>

                  {m.nome}

                  {m.serieNome ? ` — ${m.serieNome}` : ""}

                  {m.modoValidacao === "DIRETRIZES" ? " [flexivel]" : " [normativo]"}

                </option>

              ))}

            </select>



            {detalhe ? (

              <div className="mt-4 space-y-3">

                <div className="flex flex-wrap gap-2 text-xs text-slate-600">

                  <span

                    className={

                      isDiretrizes

                        ? "rounded-full bg-sky-100 px-2 py-0.5 font-semibold text-sky-900"

                        : "rounded-full bg-slate-100 px-2 py-0.5 font-semibold text-slate-800"

                    }

                  >

                    {isDiretrizes ? "Diretrizes RJ" : "Normativo RJ"}

                  </span>

                  <span>{labelAulas(detalhe)}</span>

                  <span>·</span>

                  <span>{detalhe.minutosAula} min/aula</span>

                  {detalhe.normativaRef ? (

                    <>

                      <span>·</span>

                      <span>{detalhe.normativaRef}</span>

                    </>

                  ) : null}

                </div>

                <table className="w-full text-left text-sm">

                  <thead>

                    <tr className="border-b border-slate-200 text-slate-500">

                      <th className="py-2 pr-2">Componente</th>

                      <th className="py-2 pr-2">Area</th>

                      <th className="py-2 text-right">

                        {isDiretrizes ? "Faixa aulas/sem." : "Aulas/sem."}

                      </th>

                    </tr>

                  </thead>

                  <tbody>

                    {(detalhe.componentes ?? []).map((c) => (

                      <tr key={c.id} className="border-b border-slate-100">

                        <td className="py-2 pr-2 font-medium">{c.componente}</td>

                        <td className="py-2 pr-2 text-slate-600">{c.area}</td>

                        <td className="py-2 text-right">

                          {isDiretrizes && c.aulasSemanaisMin != null

                            ? `${c.aulasSemanaisMin}–${c.aulasSemanaisMax ?? c.aulasSemanais}`

                            : c.aulasSemanais}

                        </td>

                      </tr>

                    ))}

                  </tbody>

                </table>

              </div>

            ) : null}

          </section>



          <section className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">

            <h2 className="text-sm font-semibold text-slate-800">Validar turma</h2>

            <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-end">

              <div className="flex-1">

                <label className="block text-xs font-medium text-slate-600">Turma</label>

                <select

                  className="mt-1 w-full rounded-xl border border-slate-300 px-3 py-2 text-sm"

                  value={turmaId}

                  onChange={(e) => setTurmaId(e.target.value)}

                >

                  {turmas.map((t) => (

                    <option key={t.id} value={t.id}>

                      {t.nome}

                    </option>

                  ))}

                </select>

              </div>

              <button

                type="button"

                className="rounded-xl bg-[#0c2d57] px-4 py-2 text-sm font-semibold text-white hover:bg-[#0a2447]"

                onClick={() => void onValidar()}

              >

                Validar

              </button>

            </div>



            {validacao ? (

              <div className="mt-4 space-y-3">

                <div className="flex flex-wrap items-center gap-2">

                  <span className="font-medium">{validacao.turmaNome}</span>

                  <span

                    className={

                      validacao.conforme

                        ? "rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-900"

                        : "rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-900"

                    }

                  >

                    {validacao.conforme ? "Conforme" : "Pendencias"}

                  </span>

                  <span className="text-xs text-slate-500">

                    Grade: {validacao.aulasSemanaisNaGrade}/

                    {validacao.modoValidacao === "DIRETRIZES" &&

                    validacao.aulasSemanaisMinimas != null

                      ? `${validacao.aulasSemanaisMinimas}–${validacao.aulasSemanaisMaximas}`

                      : validacao.aulasSemanaisEsperadas}{" "}

                    aulas

                  </span>

                </div>

                <table className="w-full text-left text-sm">

                  <thead>

                    <tr className="border-b border-slate-200 text-slate-500">

                      <th className="py-2 pr-2">Componente</th>

                      <th className="py-2 pr-2">Vinculo</th>

                      <th className="py-2 pr-2">Grade</th>

                      <th className="py-2">Status</th>

                    </tr>

                  </thead>

                  <tbody>

                    {validacao.itens.map((item) => (

                      <tr key={item.componente} className="border-b border-slate-100">

                        <td className="py-2 pr-2">{item.componente}</td>

                        <td className="py-2 pr-2 text-slate-600">

                          {item.vinculoDisciplina ? item.disciplinaNome : item.observacao ?? "Sem vinculo"}

                        </td>

                        <td className="py-2 pr-2">

                          {item.aulasNaGrade}/

                          {validacao.modoValidacao === "DIRETRIZES"

                            ? `${item.aulasMinimas}–${item.aulasMaximas}`

                            : item.aulasEsperadas}

                        </td>

                        <td className="py-2">

                          <span

                            className={

                              item.conforme

                                ? "font-medium text-emerald-700"

                                : "font-medium text-amber-700"

                            }

                          >

                            {item.conforme ? "OK" : "Ajustar"}

                          </span>

                        </td>

                      </tr>

                    ))}

                  </tbody>

                </table>

              </div>

            ) : null}

          </section>

        </>

      )}

    </div>

  );

}


