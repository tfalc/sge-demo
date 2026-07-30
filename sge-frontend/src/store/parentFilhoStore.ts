import { create } from "zustand";
import type { FilhoResumo } from "../types";

const STORAGE_KEY = "sge:filhoAtivoId";

type ParentFilhoState = {
  filhos: FilhoResumo[];
  filhoAtivoId: string | null;
  setFilhos: (filhos: FilhoResumo[]) => void;
  setFilhoAtivoId: (id: string) => void;
  filhoAtivo: () => FilhoResumo | null;
};

export const useParentFilhoStore = create<ParentFilhoState>((set, get) => ({
  filhos: [],
  filhoAtivoId: localStorage.getItem(STORAGE_KEY),
  setFilhos: (filhos) => {
    const stored = localStorage.getItem(STORAGE_KEY);
    const stillValid = filhos.some((f) => f.alunoId === stored);
    const nextId = stillValid ? stored : filhos[0]?.alunoId ?? null;
    if (nextId) localStorage.setItem(STORAGE_KEY, nextId);
    else localStorage.removeItem(STORAGE_KEY);
    set({ filhos, filhoAtivoId: nextId });
  },
  setFilhoAtivoId: (id) => {
    localStorage.setItem(STORAGE_KEY, id);
    set({ filhoAtivoId: id });
  },
  filhoAtivo: () => {
    const { filhos, filhoAtivoId } = get();
    return filhos.find((f) => f.alunoId === filhoAtivoId) ?? filhos[0] ?? null;
  },
}));
