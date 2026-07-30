import { NavLink, useLocation } from "react-router-dom";
import type { NavItem } from "../../config/navConfig";

type Props = {
  items: NavItem[];
  onLogout: () => void;
};

function segmentActiveClass() {
  return "bg-[#ffeb3b] !text-[#0c2d57] shadow-[inset_0_-2px_0_rgba(0,0,0,0.08)]";
}

function segmentInactiveClass() {
  return "bg-sky-300/30 !text-[#fff59d] hover:bg-sky-300/45";
}

function segmentClass(isActive: boolean) {
  return [
    "inline-flex min-h-10 items-center justify-center whitespace-nowrap px-2.5 text-center text-xs font-bold no-underline transition-colors sm:min-h-11 sm:px-3 sm:text-sm",
    isActive ? segmentActiveClass() : segmentInactiveClass(),
  ].join(" ");
}

function isActivePath(pathname: string, item: NavItem, navActive: boolean) {
  if (item.matchPrefix) {
    return pathname === item.matchPrefix || pathname.startsWith(`${item.matchPrefix}/`);
  }
  return navActive;
}

/**
 * Menu superior: todas as áreas liberadas ficam visíveis (quebra em linhas se preciso).
 * Compacto para caber Admin com todas as opções sem omitir itens.
 */
export function TopAreaNav({ items, onLogout }: Props) {
  const location = useLocation();
  const schoolItems = items.filter((i) => i.area !== "admin");
  const adminItems = items.filter((i) => i.area === "admin");
  const chain = [...schoolItems, ...adminItems];

  return (
    <nav className="min-w-0 flex-1 sm:flex-none" aria-label="Areas do sistema">
      <div className="flex max-w-full flex-wrap overflow-visible rounded-2xl border-2 border-white/50 bg-black/25 shadow-lg backdrop-blur-sm">
        {chain.map((item, idx) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              [
                segmentClass(isActivePath(location.pathname, item, isActive)),
                idx < chain.length - 1 ? "border-r border-white/25" : "",
              ].join(" ")
            }
          >
            {item.label}
          </NavLink>
        ))}

        <button
          type="button"
          onClick={onLogout}
          className={`${segmentClass(false)} cursor-pointer border-l border-white/25 sm:min-w-[4.5rem]`}
        >
          Sair
        </button>
      </div>
    </nav>
  );
}
