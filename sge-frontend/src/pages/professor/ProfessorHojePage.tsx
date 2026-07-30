import { HojeInboxPanel } from "../../components/inbox/HojeInboxPanel";

/** Conteúdo da aba Hoje — o SectionNav fica no ProfessorLayout. */
export function ProfessorHojePage() {
  return <HojeInboxPanel title="Hoje" subtitle="Tarefas do diário, frequência e planos de aula." homePath="/professor/hoje" />;
}
