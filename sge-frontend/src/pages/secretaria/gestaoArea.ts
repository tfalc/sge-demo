export type GestaoBasePath = "/secretaria" | "/direcao";

export function gestaoBaseFromPathname(pathname: string): GestaoBasePath {
  if (pathname === "/direcao" || pathname.startsWith("/direcao/")) {
    return "/direcao";
  }
  return "/secretaria";
}

export function gestaoAreaLabel(base: GestaoBasePath): string {
  return base === "/direcao" ? "Direção" : "Secretaria";
}
