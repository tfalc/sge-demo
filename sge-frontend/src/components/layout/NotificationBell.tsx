import { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import {
  listarNotificacoes,
  marcarNotificacaoLida,
  marcarTodasNotificacoesLidas,
  resumoNotificacoes,
  type Notificacao,
} from "../../services/notificacaoService";

const POLL_MS = 60_000;

export function NotificationBell() {
  const navigate = useNavigate();
  const [aberto, setAberto] = useState(false);
  const [naoLidas, setNaoLidas] = useState(0);
  const [itens, setItens] = useState<Notificacao[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [painelPos, setPainelPos] = useState({ top: 0, right: 16 });
  const botaoRef = useRef<HTMLButtonElement>(null);
  const painelRef = useRef<HTMLDivElement>(null);

  const atualizarPosicao = useCallback(() => {
    const botao = botaoRef.current;
    if (!botao) return;
    const rect = botao.getBoundingClientRect();
    setPainelPos({
      top: rect.bottom + 8,
      right: Math.max(16, window.innerWidth - rect.right),
    });
  }, []);

  const atualizarResumo = useCallback(async () => {
    try {
      const resumo = await resumoNotificacoes();
      setNaoLidas(resumo.naoLidas);
    } catch {
      /* sessao expirada ou offline */
    }
  }, []);

  const carregarLista = useCallback(async () => {
    setCarregando(true);
    try {
      const lista = await listarNotificacoes();
      setItens(lista.slice(0, 20));
      await atualizarResumo();
    } finally {
      setCarregando(false);
    }
  }, [atualizarResumo]);

  useEffect(() => {
    void atualizarResumo();
    const timer = window.setInterval(() => void atualizarResumo(), POLL_MS);
    return () => window.clearInterval(timer);
  }, [atualizarResumo]);

  useEffect(() => {
    if (!aberto) return;
    atualizarPosicao();
    void carregarLista();
  }, [aberto, carregarLista, atualizarPosicao]);

  useEffect(() => {
    if (!aberto) return;
    function handleReposicionar() {
      atualizarPosicao();
    }
    window.addEventListener("resize", handleReposicionar);
    window.addEventListener("scroll", handleReposicionar, true);
    return () => {
      window.removeEventListener("resize", handleReposicionar);
      window.removeEventListener("scroll", handleReposicionar, true);
    };
  }, [aberto, atualizarPosicao]);

  useEffect(() => {
    function handleClickFora(event: MouseEvent) {
      const alvo = event.target as Node;
      if (
        painelRef.current?.contains(alvo) ||
        botaoRef.current?.contains(alvo)
      ) {
        return;
      }
      setAberto(false);
    }
    if (aberto) {
      document.addEventListener("mousedown", handleClickFora);
    }
    return () => document.removeEventListener("mousedown", handleClickFora);
  }, [aberto]);

  async function handleClickNotificacao(item: Notificacao) {
    if (!item.lida) {
      await marcarNotificacaoLida(item.id);
      setNaoLidas((n) => Math.max(0, n - 1));
      setItens((prev) =>
        prev.map((n) => (n.id === item.id ? { ...n, lida: true } : n)),
      );
    }
    setAberto(false);
    if (item.link) {
      navigate(item.link);
    }
  }

  async function handleMarcarTodas() {
    await marcarTodasNotificacoesLidas();
    setNaoLidas(0);
    setItens((prev) => prev.map((n) => ({ ...n, lida: true })));
  }

  function togglePainel() {
    setAberto((v) => {
      const proximo = !v;
      if (proximo) {
        requestAnimationFrame(() => atualizarPosicao());
      }
      return proximo;
    });
  }

  const painel = aberto
    ? createPortal(
        <div
          ref={painelRef}
          className="fixed z-[9999] w-[min(20rem,calc(100vw-2rem))] overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xl"
          style={{ top: painelPos.top, right: painelPos.right }}
          role="dialog"
          aria-label="Notificacoes"
        >
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
            <span className="text-sm font-bold text-slate-800">Notificacoes</span>
            {naoLidas > 0 ? (
              <button
                type="button"
                onClick={() => void handleMarcarTodas()}
                className="text-xs font-semibold text-sky-700 hover:underline"
              >
                Marcar todas lidas
              </button>
            ) : null}
          </div>

          <div className="max-h-[min(20rem,calc(100vh-8rem))] overflow-y-auto">
            {carregando ? (
              <p className="px-4 py-6 text-center text-sm text-slate-500">Carregando...</p>
            ) : itens.length === 0 ? (
              <p className="px-4 py-6 text-center text-sm text-slate-500">Nenhuma notificacao.</p>
            ) : (
              <ul>
                {itens.map((item) => (
                  <li key={item.id}>
                    <button
                      type="button"
                      onClick={() => void handleClickNotificacao(item)}
                      className={[
                        "w-full border-b border-slate-50 px-4 py-3 text-left transition hover:bg-slate-50",
                        item.lida ? "opacity-70" : "bg-sky-50/40",
                      ].join(" ")}
                    >
                      <div className="text-sm font-semibold text-slate-800">{item.titulo}</div>
                      <div className="mt-0.5 text-xs leading-relaxed text-slate-600">{item.mensagem}</div>
                      <div className="mt-1 text-[10px] text-slate-400">
                        {new Date(item.criadoEm).toLocaleString("pt-BR")}
                      </div>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>,
        document.body,
      )
    : null;

  return (
    <>
      <button
        ref={botaoRef}
        type="button"
        onClick={togglePainel}
        className="relative flex h-10 w-10 shrink-0 items-center justify-center rounded-full border-2 border-white/50 bg-black/25 text-lg text-[#ffeb3b] shadow-lg backdrop-blur-sm transition hover:bg-black/40"
        aria-label={`Notificacoes${naoLidas > 0 ? `, ${naoLidas} nao lidas` : ""}`}
        aria-expanded={aberto}
      >
        <span aria-hidden>🔔</span>
        {naoLidas > 0 ? (
          <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-xs font-bold text-white">
            {naoLidas > 9 ? "9+" : naoLidas}
          </span>
        ) : null}
      </button>
      {painel}
    </>
  );
}
