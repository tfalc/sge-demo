import { GaleriaPanel } from "../../components/galeria/GaleriaPanel";

export function ProfessorGaleriaPage() {
  return (
    <GaleriaPanel
      title="Galeria escolar"
      subtitle="Publique fotos de aulas, projetos e eventos para a comunidade escolar."
      canManage
      gestao
    />
  );
}
