import type { InputHTMLAttributes } from "react";

type Variant = "default" | "onSkyBlue";

type Props = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  variant?: Variant;
};

const labelByVariant: Record<Variant, string> = {
  default: "mb-1 font-medium text-slate-700",
  onSkyBlue: "mb-1.5 font-bold !text-[#0c2d57]",
};

const inputByVariant: Record<Variant, string> = {
  default:
    "border-slate-300 bg-white text-slate-900 focus-visible:border-brand-blue focus-visible:ring-2 focus-visible:ring-brand-yellow/80",
  onSkyBlue:
    "border-2 border-slate-700/30 bg-white text-brand-blue shadow-inner placeholder:text-slate-400 focus-visible:border-brand-blue focus-visible:ring-2 focus-visible:ring-brand-yellow-poster/90",
};

export function Input({ label, id, className = "", variant = "default", ...props }: Props) {
  const inputId = id ?? label.toLowerCase().replace(/\s+/g, "-");
  return (
    <label className="block text-sm" htmlFor={inputId}>
      <div className={labelByVariant[variant]}>{label}</div>
      <input
        id={inputId}
        className={`w-full rounded-2xl border px-3 py-2.5 text-sm font-medium outline-none transition-shadow ${inputByVariant[variant]} ${className}`}
        {...props}
      />
    </label>
  );
}
