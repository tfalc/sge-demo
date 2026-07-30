import { useEffect, useMemo, useRef, useState } from "react";
import { NavLink, useLocation } from "react-router-dom";

export type SectionNavItem = {
  to: string;
  label: string;
  matchPrefix?: string;
};

type Props = {
  items: SectionNavItem[];
  /** Quantidade visível antes do botão "Mais" (mobile/desktop). */
  visibleCount?: number;
};

function isItemActive(pathname: string, item: SectionNavItem, navActive: boolean): boolean {
  if (item.matchPrefix) {
    return pathname === item.matchPrefix || pathname.startsWith(`${item.matchPrefix}/`);
  }
  return navActive;
}

function itemClass(active: boolean): string {
  return [
    "inline-flex min-h-11 min-w-[2.75rem] items-center justify-center rounded-full px-4 py-1.5 text-sm font-semibold transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue",
    active
      ? "bg-brand-blue text-white shadow-sm"
      : "bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-50",
  ].join(" ");
}

export function SectionNav({ items, visibleCount = 6 }: Props) {
  const location = useLocation();
  const [openMore, setOpenMore] = useState(false);
  const moreRef = useRef<HTMLDivElement>(null);

  const { primary, overflow, overflowActive } = useMemo(() => {
    if (items.length <= visibleCount) {
      return { primary: items, overflow: [] as SectionNavItem[], overflowActive: false };
    }
    const primaryItems = items.slice(0, visibleCount - 1);
    const overflowItems = items.slice(visibleCount - 1);
    const overflowIsActive = overflowItems.some((item) =>
      isItemActive(
        location.pathname,
        item,
        location.pathname === item.to || location.pathname.startsWith(`${item.to}/`),
      ),
    );
    return { primary: primaryItems, overflow: overflowItems, overflowActive: overflowIsActive };
  }, [items, visibleCount, location.pathname]);

  useEffect(() => {
    setOpenMore(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!openMore) return;

    function onPointerDown(event: PointerEvent) {
      if (moreRef.current && !moreRef.current.contains(event.target as Node)) {
        setOpenMore(false);
      }
    }

    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpenMore(false);
      }
    }

    document.addEventListener("pointerdown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [openMore]);

  return (
    <nav className="mb-6 border-b border-slate-200 pb-3" aria-label="Seções">
      <div className="flex flex-wrap items-center gap-2">
        {primary.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={!item.matchPrefix}
            className={({ isActive }) => itemClass(isItemActive(location.pathname, item, isActive))}
          >
            {item.label}
          </NavLink>
        ))}

        {overflow.length > 0 ? (
          <div className="relative" ref={moreRef}>
            <button
              type="button"
              aria-expanded={openMore}
              aria-haspopup="menu"
              className={itemClass(overflowActive || openMore)}
              onClick={() => setOpenMore((v) => !v)}
            >
              Mais
            </button>
            {openMore ? (
              <div
                role="menu"
                className="absolute left-0 z-20 mt-2 min-w-[12rem] rounded-xl border border-slate-200 bg-white p-2 shadow-lg"
              >
                {overflow.map((item) => (
                  <NavLink
                    key={item.to}
                    role="menuitem"
                    to={item.to}
                    end={!item.matchPrefix}
                    onClick={() => setOpenMore(false)}
                    className={({ isActive }) =>
                      [
                        "block rounded-lg px-3 py-2 text-sm font-semibold focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue",
                        isItemActive(location.pathname, item, isActive)
                          ? "bg-brand-blue text-white"
                          : "text-slate-700 hover:bg-slate-50",
                      ].join(" ")
                    }
                  >
                    {item.label}
                  </NavLink>
                ))}
              </div>
            ) : null}
          </div>
        ) : null}
      </div>
    </nav>
  );
}
