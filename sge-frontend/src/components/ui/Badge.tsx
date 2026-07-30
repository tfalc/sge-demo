import type { ChargeStatus } from "../../types";

const statusStyles: Record<ChargeStatus, string> = {
  PENDENTE: "bg-amber-100 text-amber-900 ring-amber-200",
  PAGO: "bg-emerald-100 text-emerald-900 ring-emerald-200",
  VENCIDO: "bg-red-100 text-red-900 ring-red-200",
  CANCELADO: "bg-slate-100 text-slate-600 ring-slate-200",
};

const statusLabels: Record<ChargeStatus, string> = {
  PENDENTE: "Pendente",
  PAGO: "Pago",
  VENCIDO: "Vencido",
  CANCELADO: "Cancelado",
};

type Props = {
  status: ChargeStatus;
};

export function Badge({ status }: Props) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset ${statusStyles[status]}`}
    >
      {statusLabels[status]}
    </span>
  );
}
