import { useEffect } from "react";
import { getMe } from "../../services/authService";
import { useParentFilhoStore } from "../../store/parentFilhoStore";

/** Seletor global de filho no portal dos pais (persistido em localStorage). */
export function ParentFilhoSelector() {
  const filhos = useParentFilhoStore((s) => s.filhos);
  const filhoAtivoId = useParentFilhoStore((s) => s.filhoAtivoId);
  const setFilhos = useParentFilhoStore((s) => s.setFilhos);
  const setFilhoAtivoId = useParentFilhoStore((s) => s.setFilhoAtivoId);

  useEffect(() => {
    if (filhos.length > 0) return;
    void getMe()
      .then((me) => setFilhos(me.filhos ?? []))
      .catch(() => undefined);
  }, [filhos.length, setFilhos]);

  if (filhos.length <= 1) return null;

  return (
    <label className="mb-4 block max-w-md text-sm">
      <span className="mb-1 block font-medium text-slate-700">Filho em acompanhamento</span>
      <select
        className="w-full rounded-lg border border-slate-300 px-3 py-2.5 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue"
        value={filhoAtivoId ?? filhos[0]?.alunoId ?? ""}
        onChange={(e) => setFilhoAtivoId(e.target.value)}
        aria-label="Selecionar filho"
      >
        {filhos.map((f) => (
          <option key={f.alunoId} value={f.alunoId}>
            {f.nome}
            {f.turmaNome ? ` (${f.turmaNome})` : ""}
          </option>
        ))}
      </select>
    </label>
  );
}
