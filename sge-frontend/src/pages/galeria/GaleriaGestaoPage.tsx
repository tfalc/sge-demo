import { GaleriaPanel } from "../../components/galeria/GaleriaPanel";
import { SectionNav } from "../../components/layout/SectionNav";
import { useGestaoArea } from "../secretaria/useGestaoArea";
import { useAuthStore } from "../../store/authStore";

export function GaleriaGestaoPage() {
  const { areaLabel, primaryNav } = useGestaoArea();
  const perfil = useAuthStore((s) => s.perfil);
  const canManage = perfil === "ADMIN";

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">{areaLabel} — Galeria</h2>
        <p className="mt-1 text-sm text-slate-600">
          {canManage
            ? "Crie álbuns e publique fotos para pais, alunos e professores."
            : "Consulte os álbuns publicados pela escola."}
        </p>
      </div>
      <SectionNav items={primaryNav} />
      <GaleriaPanel canManage={canManage} gestao />
    </div>
  );
}
