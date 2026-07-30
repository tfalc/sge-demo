import { useEffect, useState } from "react";
import { downloadFotoGaleria } from "../../services/galeriaService";

type Props = {
  fotoId: string;
  audiencia?: string;
  turmaId?: string;
  gestao?: boolean;
  autorizadoImagem?: boolean;
  alt: string;
  className?: string;
};

export function GaleriaFotoImage({
  fotoId,
  audiencia,
  turmaId,
  gestao,
  autorizadoImagem = true,
  alt,
  className,
}: Props) {
  const [src, setSrc] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let objectUrl: string | null = null;
    setFailed(false);
    void downloadFotoGaleria(fotoId, { audiencia, turmaId, gestao, autorizadoImagem })
      .then((blob) => {
        objectUrl = URL.createObjectURL(blob);
        setSrc(objectUrl);
      })
      .catch(() => setFailed(true));

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [fotoId, audiencia, turmaId, gestao, autorizadoImagem]);

  if (failed) {
    return (
      <div className={`flex items-center justify-center bg-slate-100 text-xs text-slate-500 ${className ?? ""}`}>
        Imagem indisponível
      </div>
    );
  }

  if (!src) {
    return <div className={`animate-pulse bg-slate-200 ${className ?? ""}`} aria-hidden />;
  }

  return <img src={src} alt={alt} className={className} loading="lazy" />;
}
