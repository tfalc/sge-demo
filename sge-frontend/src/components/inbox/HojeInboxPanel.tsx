import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState, PageSkeleton } from "../ui/EmptyState";
import { SectionNav, type SectionNavItem } from "../layout/SectionNav";
import { getMe } from "../../services/authService";
import { getCharges } from "../../services/financeiroService";
import { listarNotificacoes, type Notificacao } from "../../services/notificacaoService";
import type { Charge } from "../../types";

type InboxItem = {
  id: string;
  titulo: string;
  detalhe: string;
  to: string;
  tom: "amber" | "red" | "blue";
};

type Props = {
  title?: string;
  subtitle?: string;
  /** Só passe se a página/layout ainda não renderizou o SectionNav. */
  sectionNav?: SectionNavItem[];
  homePath: string;
};

function toneClass(tom: InboxItem["tom"]) {
  if (tom === "red") return "border-red-200 bg-red-50";
  if (tom === "amber") return "border-amber-200 bg-amber-50";
  return "border-sky-200 bg-sky-50";
}

export function HojeInboxPanel({ title, subtitle, sectionNav, homePath }: Props) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [items, setItems] = useState<InboxItem[]>([]);
  const [perfil, setPerfil] = useState<string>("");

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const me = await getMe();
      setPerfil(me.perfil);
      const next: InboxItem[] = [];

      const notificacoes = await listarNotificacoes().catch(() => [] as Notificacao[]);
      for (const n of notificacoes.filter((x) => !x.lida).slice(0, 8)) {
        next.push({
          id: `n-${n.id}`,
          titulo: n.titulo,
          detalhe: n.mensagem,
          to: n.link || homePath,
          tom: "blue",
        });
      }

      if (me.perfil === "PAI" && me.responsavelId) {
        const charges = await getCharges(me.responsavelId).catch(() => [] as Charge[]);
        for (const c of charges.filter((x) => x.status === "VENCIDO" || x.status === "PENDENTE")) {
          next.push({
            id: `c-${c.id}`,
            titulo: c.status === "VENCIDO" ? "Cobrança vencida" : "Cobrança pendente",
            detalhe: `${c.alunoNome} · ${c.competencia} · R$ ${Number(c.valor).toFixed(2)}`,
            to: "/pais/cobrancas",
            tom: c.status === "VENCIDO" ? "red" : "amber",
          });
        }
      }

      if (me.perfil === "PROFESSOR") {
        next.push({
          id: "p-freq",
          titulo: "Registrar frequência",
          detalhe: "Atualize a folha do bimestre ou a chamada do dia.",
          to: "/professor/frequencia",
          tom: "amber",
        });
        next.push({
          id: "p-plano",
          titulo: "Plano de aula",
          detalhe: "Registre o plano/ata da próxima aula.",
          to: "/professor/ata",
          tom: "blue",
        });
      }

      if (me.perfil === "DIRETOR" || me.perfil === "ADMIN" || me.perfil === "COORDENADOR") {
        next.push({
          id: "g-coord",
          titulo: "Revisar indicadores pedagógicos",
          detalhe: "Verifique médias, risco e colegiados.",
          to: "/coordenacao",
          tom: "amber",
        });
      }

      // Dedup por titulo+detalhe simples
      const seen = new Set<string>();
      setItems(
        next.filter((i) => {
          const key = `${i.titulo}|${i.detalhe}`;
          if (seen.has(key)) return false;
          seen.add(key);
          return true;
        }),
      );
    } catch {
      setError("Não foi possível montar a inbox. Verifique a conexão e tente novamente.");
    } finally {
      setLoading(false);
    }
  }, [homePath]);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className="space-y-6">
      {title ? (
        <div>
          <h2 className="text-xl font-semibold text-slate-900">{title}</h2>
          {subtitle ? <p className="mt-1 text-sm text-slate-600">{subtitle}</p> : null}
          {perfil ? <p className="mt-1 text-xs text-slate-500">Perfil: {perfil}</p> : null}
        </div>
      ) : null}

      {sectionNav ? <SectionNav items={sectionNav} /> : null}

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          {error}
          <button type="button" className="ml-2 font-semibold underline" onClick={() => void load()}>
            Tentar de novo
          </button>
        </div>
      ) : null}

      {loading ? (
        <PageSkeleton />
      ) : items.length === 0 ? (
        <EmptyState
          title="Nada pendente por agora"
          description="Quando houver cobranças, notificações ou tarefas do dia, elas aparecem aqui."
          actionLabel="Atualizar"
          onAction={() => void load()}
        />
      ) : (
        <ul className="space-y-3">
          {items.map((item) => (
            <li key={item.id}>
              <Link
                to={item.to}
                className={`block rounded-xl border px-4 py-3 transition hover:shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue ${toneClass(item.tom)}`}
              >
                <p className="font-semibold text-slate-900">{item.titulo}</p>
                <p className="mt-1 text-sm text-slate-700">{item.detalhe}</p>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
