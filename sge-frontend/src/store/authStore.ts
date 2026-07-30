import { create } from "zustand";
import { ACCESS_TOKEN_KEY } from "../services/api";

interface AuthState {
  accessToken: string | null;
  perfil: string | null;
  areasMenu: string[];
  setAccessToken: (token: string | null) => void;
  setPerfil: (perfil: string | null) => void;
  setAreasMenu: (areas: string[]) => void;
  hydrateFromStorage: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  perfil: null,
  areasMenu: [],
  setAccessToken: (token) => {
    if (token) {
      localStorage.setItem(ACCESS_TOKEN_KEY, token);
    } else {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    }
    set({ accessToken: token });
  },
  setPerfil: (perfil) => set({ perfil }),
  setAreasMenu: (areasMenu) => set({ areasMenu }),
  hydrateFromStorage: () => {
    set({ accessToken: localStorage.getItem(ACCESS_TOKEN_KEY) });
  },
}));
