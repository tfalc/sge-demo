import { useEffect, useState } from "react";
import { getSchoolConfig, type SchoolConfig } from "../services/schoolService";

const fallback: SchoolConfig = {
  packageId: "escola-demo",
  nome: "Escola Modelo Demo",
  nomeCurto: "Escola Demo",
  siglaSge: "SGE",
  municipio: "Cidade Exemplo",
  uf: "SP",
  branding: {
    titulo_sistema: "SGE — Gestao Escolar",
    subtitulo: "Escola Modelo Demo · Ambiente de demonstracao",
  },
};

let cached: SchoolConfig | null = null;
let inflight: Promise<SchoolConfig> | null = null;

export function useSchoolConfig() {
  const [config, setConfig] = useState<SchoolConfig>(cached ?? fallback);
  const [loading, setLoading] = useState(!cached);

  useEffect(() => {
    if (cached) {
      setConfig(cached);
      setLoading(false);
      return;
    }
    if (!inflight) {
      inflight = getSchoolConfig()
        .then((c) => {
          cached = c;
          return c;
        })
        .catch(() => fallback);
    }
    void inflight.then((c) => {
      setConfig(c);
      setLoading(false);
    });
  }, []);

  const branding = config.branding ?? fallback.branding!;
  const titulo = config.siglaSge ?? config.nomeCurto ?? "SGE";
  const subtitulo = branding.subtitulo ?? branding.titulo_sistema ?? "Sistema de Gestao Escolar";

  return { config, loading, titulo, subtitulo, branding };
}
