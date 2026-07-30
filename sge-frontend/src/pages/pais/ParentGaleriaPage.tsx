import { GaleriaPanel } from "../../components/galeria/GaleriaPanel";
import { ParentFilhoSelector } from "../../components/pais/ParentFilhoSelector";
import { SectionNav } from "../../components/layout/SectionNav";
import { useParentFilhoStore } from "../../store/parentFilhoStore";
import { parentNav } from "./parentNav";

export function ParentGaleriaPage() {
  const filho = useParentFilhoStore((s) => s.filhoAtivo());
  const autorizaImagem = filho?.autorizaUsoImagem !== false;

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Portal dos Pais</h2>
        <p className="mt-1 text-sm text-slate-600">Fotos de eventos, atividades e momentos da escola.</p>
      </div>
      <SectionNav items={parentNav} />
      <ParentFilhoSelector />
      {!autorizaImagem ? (
        <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          O uso de imagem deste aluno não está autorizado (LGPD). Álbuns que exigem consentimento ficam ocultos.
          Atualize a autorização com a secretaria se desejar visualizar.
        </div>
      ) : null}
      <GaleriaPanel
        canManage={false}
        audiencia="PAIS"
        turmaId={filho?.turmaId ?? undefined}
        autorizadoImagem={autorizaImagem}
      />
    </div>
  );
}
