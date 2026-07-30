import type { ButtonHTMLAttributes } from "react";
import { Button } from "./Button";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "brand" | "neutral" | "danger";
};

/** Botão compacto para ações em listas (editar, excluir, remover). */
export function ActionButton({ variant = "brand", className = "", ...props }: Props) {
  return <Button variant={variant} size="sm" className={className} {...props} />;
}
