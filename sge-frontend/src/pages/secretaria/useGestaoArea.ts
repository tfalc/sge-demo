import { useLocation } from "react-router-dom";
import {
  academicoGestaoNav,
  cadastroGestaoNav,
  comunicacaoGestaoNav,
  horariosGestaoNav,
  primaryGestaoNav,
} from "./gestaoNav";
import { gestaoAreaLabel, gestaoBaseFromPathname } from "./gestaoArea";

export function useGestaoArea() {
  const { pathname } = useLocation();
  const basePath = gestaoBaseFromPathname(pathname);
  const areaLabel = gestaoAreaLabel(basePath);

  return {
    basePath,
    areaLabel,
    primaryNav: primaryGestaoNav(basePath),
    academicoNav: academicoGestaoNav(basePath),
    cadastroNav: cadastroGestaoNav(basePath),
    comunicacaoNav: comunicacaoGestaoNav(basePath),
    horariosNav: horariosGestaoNav(basePath),
  };
}
