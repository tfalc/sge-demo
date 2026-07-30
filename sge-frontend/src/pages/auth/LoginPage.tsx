import { useMemo, useState, type FormEvent } from "react";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { useSchoolConfig } from "../../hooks/useSchoolConfig";
import { forgotPassword, getMe, login } from "../../services/authService";
import { apiErrorMessage } from "../../utils/apiError";
import { defaultHomeForPerfil } from "../../config/routeAccess";

type LoginLocationState = {
  from?: string;
};

const loginCardBackground = {
  background: `
    radial-gradient(ellipse 90% 65% at 50% 0%, rgba(255, 235, 59, 0.32), transparent 52%),
    linear-gradient(165deg, #7ad4fb 0%, #42bff7 35%, #28a7e8 70%, #1b98db 100%)
  `,
} as const;

const isDemoBuild = import.meta.env.VITE_DEMO_BUILD === "true";
const DEMO_DOMAIN = "@sge.com";
const DEMO_PASSWORD = "admin123";

const DEMO_PROFILES: { local: string; label: string }[] = [
  { local: "admin", label: "Admin (donos)" },
  { local: "secretaria", label: "Secretaria" },
  { local: "diretor", label: "Direção" },
  { local: "coord", label: "Coordenação" },
  { local: "prof", label: "Professor" },
  { local: "pai", label: "Pais" },
  { local: "aluno", label: "Aluno" },
  { local: "nutri", label: "Nutrição" },
  { local: "psico", label: "Psicologia" },
];

export function LoginPage() {
  const { titulo, subtitulo, config } = useSchoolConfig();
  const navigate = useNavigate();
  const location = useLocation();
  const [demoLocal, setDemoLocal] = useState(DEMO_PROFILES[0].local);
  const [email, setEmail] = useState(isDemoBuild ? "" : "admin@sge.com");
  const [password, setPassword] = useState(isDemoBuild ? DEMO_PASSWORD : "admin123");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [forgotMsg, setForgotMsg] = useState<string | null>(null);
  const [showForgot, setShowForgot] = useState(false);
  const [forgotEmail, setForgotEmail] = useState("");

  const demoEmail = useMemo(() => `${demoLocal}${DEMO_DOMAIN}`, [demoLocal]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const loginEmail = isDemoBuild ? demoEmail : email;
    const loginPassword = isDemoBuild ? DEMO_PASSWORD : password;
    try {
      await login({ email: loginEmail, password: loginPassword });
      const from = (location.state as LoginLocationState | null)?.from?.trim();
      if (from) {
        navigate(from, { replace: true });
        return;
      }
      const me = await getMe();
      navigate(defaultHomeForPerfil(me.perfil, me.areasMenu), { replace: true });
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 401 || err.response?.status === 403) {
          setError("Credenciais inválidas. Confira o perfil selecionado.");
        } else {
          setError(apiErrorMessage(err, "Erro ao autenticar. Verifique se a API está no ar."));
        }
      } else {
        setError("Erro inesperado ao autenticar. Tente novamente.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex justify-center py-10">
      <div
        className="relative w-full max-w-md overflow-hidden rounded-3xl border-2 border-white/50 p-8 shadow-2xl"
        style={loginCardBackground}
      >
        <div className="relative z-10">
          <h1 className="text-center text-3xl font-black uppercase tracking-[0.2em] !text-brand-yellow-poster drop-shadow-[0_2px_4px_rgba(0,0,0,0.25)]">
            {titulo}
          </h1>
          <p className="mt-2 text-center text-xs font-semibold !text-[#0c2d57]">{subtitulo}</p>
          {config.municipio && config.uf ? (
            <p className="mt-1 text-center text-xs !text-[#0c2d57]/90">
              {config.municipio}/{config.uf} · normativa RJ
            </p>
          ) : null}

          <form className="mt-6 space-y-4" onSubmit={onSubmit}>
            {isDemoBuild ? (
              <>
                <p className="rounded-xl border-2 border-white/40 bg-black/15 px-3 py-2 text-center text-xs font-semibold !text-[#0c2d57]">
                  Demo online (plano free): o primeiro acesso pode demorar cerca de 1 minuto enquanto a API
                  &quot;acorda&quot;. Se falhar, aguarde e tente de novo.
                </p>
                <label className="block text-sm">
                  <span className="mb-1 block font-semibold !text-[#0c2d57]">Perfil</span>
                  <select
                    className="w-full rounded-lg border-2 border-white/60 bg-white/90 px-3 py-2.5 text-sm font-semibold text-slate-900 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
                    value={demoLocal}
                    onChange={(e) => setDemoLocal(e.target.value)}
                    aria-label="Selecionar perfil de demonstração"
                  >
                    {DEMO_PROFILES.map((p) => (
                      <option key={p.local} value={p.local}>
                        {p.label}
                      </option>
                    ))}
                  </select>
                </label>

                <div>
                  <span className="mb-1 block text-sm font-semibold !text-[#0c2d57]">E-mail</span>
                  <div className="flex overflow-hidden rounded-lg border-2 border-white/60 bg-white/90">
                    <input
                      className="min-w-0 flex-1 bg-transparent px-3 py-2.5 text-sm font-semibold text-slate-900 focus:outline-none"
                      value={demoLocal}
                      onChange={(e) =>
                        setDemoLocal(e.target.value.replace(/@.*$/, "").replace(/\s/g, "").toLowerCase())
                      }
                      autoComplete="username"
                      spellCheck={false}
                      aria-label="Parte local do e-mail"
                    />
                    <span className="flex items-center bg-slate-100/90 px-3 text-sm font-bold text-slate-600">
                      {DEMO_DOMAIN}
                    </span>
                  </div>
                  <p className="mt-1 text-xs font-medium !text-[#0c2d57]/80">
                    Conta: <span className="font-mono">{demoEmail}</span>
                  </p>
                </div>

                <div>
                  <span className="mb-1 block text-sm font-semibold !text-[#0c2d57]">Senha</span>
                  <p className="rounded-lg border-2 border-white/40 bg-black/15 px-3 py-2.5 text-sm font-semibold !text-[#0c2d57]">
                    Senha única da demo: <span className="font-mono">{DEMO_PASSWORD}</span>
                  </p>
                </div>
              </>
            ) : (
              <>
                <Input
                  variant="onSkyBlue"
                  label="E-mail"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="username"
                  spellCheck={false}
                />
                <Input
                  variant="onSkyBlue"
                  label="Senha"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
              </>
            )}

            {error ? (
              <div className="rounded-2xl border-2 border-red-300/80 bg-red-950/60 px-3 py-2 text-center text-sm font-bold !text-red-50">
                {error}
              </div>
            ) : null}

            <Button className="w-full" disabled={loading} type="submit" variant="loginCta">
              {loading ? "Entrando…" : "Entrar"}
            </Button>

            {!isDemoBuild ? (
              <button
                type="button"
                className="w-full text-center text-xs font-semibold !text-[#0c2d57] underline focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
                onClick={() => {
                  setShowForgot((v) => !v);
                  setForgotEmail(email);
                  setForgotMsg(null);
                }}
              >
                Esqueci minha senha
              </button>
            ) : null}

            {showForgot && !isDemoBuild ? (
              <div className="space-y-2 rounded-2xl border border-white/40 bg-black/20 p-3">
                <Input
                  variant="onSkyBlue"
                  label="E-mail para recuperação"
                  type="email"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                />
                <Button
                  type="button"
                  className="w-full"
                  variant="neutral"
                  onClick={() => {
                    void (async () => {
                      try {
                        const msg = await forgotPassword(forgotEmail);
                        setForgotMsg(msg);
                      } catch {
                        setForgotMsg("E-mail não encontrado.");
                      }
                    })();
                  }}
                >
                  Enviar (simulado)
                </Button>
                {forgotMsg ? (
                  <p className="text-center text-xs font-medium !text-[#0c2d57]">{forgotMsg}</p>
                ) : null}
              </div>
            ) : null}
          </form>
        </div>
      </div>
    </div>
  );
}
