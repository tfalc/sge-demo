import { useEffect } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { NotificationBell } from "./components/layout/NotificationBell";
import { TopAreaNav } from "./components/layout/TopAreaNav";
import { navItemsForPerfil } from "./config/navConfig";
import { useSchoolConfig } from "./hooks/useSchoolConfig";
import { getMe, logout } from "./services/authService";
import { useAuthStore } from "./store/authStore";
import { persistSectionPath } from "./utils/sectionNavResolver";

export default function App() {
  const { titulo, subtitulo } = useSchoolConfig();
  const location = useLocation();
  const navigate = useNavigate();
  const accessToken = useAuthStore((s) => s.accessToken);
  const perfil = useAuthStore((s) => s.perfil);
  const areasMenu = useAuthStore((s) => s.areasMenu);
  const setPerfil = useAuthStore((s) => s.setPerfil);
  const setAreasMenu = useAuthStore((s) => s.setAreasMenu);

  const isLoginRoute = location.pathname === "/login";
  const showTabs = Boolean(accessToken) && !isLoginRoute;

  useEffect(() => {
    if (!accessToken || (perfil && areasMenu.length > 0)) return;
    void getMe()
      .then((me) => {
        setPerfil(me.perfil);
        setAreasMenu(me.areasMenu ?? []);
      })
      .catch(() => undefined);
  }, [accessToken, perfil, areasMenu.length, setPerfil, setAreasMenu]);

  useEffect(() => {
    persistSectionPath(location.pathname);
  }, [location.pathname]);

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  const navItems = navItemsForPerfil(perfil, areasMenu);

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <header className="relative z-30 px-3 sm:px-4 lg:px-5">
        <div className="relative mx-auto max-w-7xl overflow-visible rounded-b-2xl shadow-lg">
          <div
            className="pointer-events-none absolute inset-0 overflow-hidden rounded-b-2xl bg-gradient-to-br from-brand-blue-deep via-brand-blue-mid to-brand-blue"
            aria-hidden
          />
          <div
            className="pointer-events-none absolute inset-0 overflow-hidden rounded-b-2xl bg-gradient-to-tr from-transparent via-brand-blue-bright/25 to-brand-yellow/10"
            aria-hidden
          />
          <div className="relative flex flex-col gap-3 px-3 py-4 sm:px-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="text-center sm:text-left">
                <div className="text-2xl font-bold tracking-tight !text-[#ffeb3b] drop-shadow-[0_1px_2px_rgba(0,0,0,0.45)]">
                  {titulo}
                </div>
                <div className="text-sm font-semibold !text-[#fff9c4] drop-shadow-[0_1px_1px_rgba(0,0,0,0.35)]">
                  {subtitulo}
                </div>
              </div>
              {showTabs ? <NotificationBell /> : null}
            </div>

            {showTabs ? (
              <div className="flex w-full items-start justify-end gap-3">
                <TopAreaNav items={navItems} onLogout={handleLogout} />
              </div>
            ) : null}
          </div>
          {showTabs ? (
            <div
              className="relative h-1 rounded-b-2xl bg-gradient-to-r from-[#ffeb3b] via-[#7dd3fc] to-[#0c2d57]"
              aria-hidden
            />
          ) : null}
        </div>
      </header>

      <main className="mx-auto w-full max-w-7xl px-3 py-6 sm:px-4 lg:px-5">
        <Outlet />
      </main>
    </div>
  );
}
