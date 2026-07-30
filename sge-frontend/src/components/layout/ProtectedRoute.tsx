import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { canAccessPath, defaultHomeForPerfil } from "../../config/routeAccess";
import { ACCESS_TOKEN_KEY } from "../../services/api";
import { useAuthStore } from "../../store/authStore";

type Props = {
  children: ReactNode;
};

/**
 * Exige JWT e perfil compatível com o prefixo da rota (complementa regras do backend).
 */
export function ProtectedRoute({ children }: Props) {
  const location = useLocation();
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  const perfil = useAuthStore((s) => s.perfil);
  const areasMenu = useAuthStore((s) => s.areasMenu);

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (perfil && !canAccessPath(location.pathname, perfil, areasMenu)) {
    return <Navigate to={defaultHomeForPerfil(perfil, areasMenu)} replace />;
  }

  return <>{children}</>;
}
