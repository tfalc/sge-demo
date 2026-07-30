import type { ButtonHTMLAttributes, PropsWithChildren } from "react";

type Variant = "brand" | "neutral" | "danger" | "loginCta";
type Size = "md" | "sm";

type Props = PropsWithChildren<
  ButtonHTMLAttributes<HTMLButtonElement> & {
    variant?: Variant;
    size?: Size;
  }
>;

const variantClass: Record<Variant, string> = {
  brand:
    "rounded-full bg-brand-yellow font-semibold text-brand-blue shadow hover:bg-brand-yellow-bright focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-yellow",
  neutral: "rounded-full bg-slate-900 font-medium text-white hover:bg-slate-800",
  danger:
    "rounded-full bg-red-600 font-semibold text-white shadow hover:bg-red-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600",
  loginCta:
    "rounded-full border-2 border-white/80 bg-brand-yellow-poster font-black !text-[#0c2d57] shadow-lg hover:bg-brand-yellow-sun focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white",
};

const sizeClass: Record<Size, string> = {
  md: "px-4 py-3 text-sm",
  sm: "px-3 py-1.5 text-xs",
};

export function Button({
  children,
  className = "",
  type = "button",
  variant = "brand",
  size = "md",
  ...props
}: Props) {
  return (
    <button
      className={`inline-flex items-center justify-center transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${variantClass[variant]} ${sizeClass[size]} ${className}`}
      type={type}
      {...props}
    >
      {children}
    </button>
  );
}
