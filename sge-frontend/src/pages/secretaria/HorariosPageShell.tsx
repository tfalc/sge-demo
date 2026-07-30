import type { ReactNode } from "react";

export function HorariosAlerts({
  error,
  success,
  loading,
  children,
}: {
  error?: string | null;
  success?: string | null;
  loading?: boolean;
  children: ReactNode;
}) {
  return (
    <>
      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}
      {loading ? <p className="text-sm text-slate-500">Carregando...</p> : children}
    </>
  );
}

export function HorarioPanel({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="flex h-full flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <h3 className="text-base font-semibold text-slate-900">{title}</h3>
      <div className="mt-4 flex-1">{children}</div>
    </section>
  );
}
