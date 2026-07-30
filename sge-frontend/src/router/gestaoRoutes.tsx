import { Navigate, type RouteObject } from "react-router-dom";
import { ProtectedRoute } from "../components/layout/ProtectedRoute";
import { SecretariaAcademicoLayout } from "../pages/secretaria/SecretariaAcademicoLayout";
import { SecretariaAcademicoDisciplinasPage } from "../pages/secretaria/SecretariaAcademicoDisciplinasPage";
import { SecretariaAcademicoProfessoresPage } from "../pages/secretaria/SecretariaAcademicoProfessoresPage";
import { SecretariaAcademicoTurmasPage } from "../pages/secretaria/SecretariaAcademicoTurmasPage";
import { SecretariaAcademicoVinculosPage } from "../pages/secretaria/SecretariaAcademicoVinculosPage";
import { SecretariaCadastroLayout } from "../pages/secretaria/SecretariaCadastroLayout";
import { SecretariaCadastroEscolaPage } from "../pages/secretaria/SecretariaCadastroEscolaPage";
import { SecretariaCadastroAlunosPage } from "../pages/secretaria/SecretariaCadastroAlunosPage";
import { SecretariaCadastroResponsaveisPage } from "../pages/secretaria/SecretariaCadastroResponsaveisPage";
import { SecretariaCadastroUsuariosPage } from "../pages/secretaria/SecretariaCadastroUsuariosPage";
import { SecretariaComunicacaoLayout } from "../pages/secretaria/SecretariaComunicacaoLayout";
import { SecretariaComunicacaoComunicadosPage } from "../pages/secretaria/SecretariaComunicacaoComunicadosPage";
import { SecretariaComunicacaoEventosPage } from "../pages/secretaria/SecretariaComunicacaoEventosPage";
import { SecretariaComunicacaoCalendarioPage } from "../pages/secretaria/SecretariaComunicacaoCalendarioPage";
import { SecretariaFinanceiroPage } from "../pages/secretaria/SecretariaFinanceiroPage";
import { SecretariaHorariosLayout } from "../pages/secretaria/SecretariaHorariosLayout";
import { SecretariaHorariosGradePage } from "../pages/secretaria/SecretariaHorariosGradePage";
import { SecretariaHorariosDisciplinaPage } from "../pages/secretaria/SecretariaHorariosDisciplinaPage";
import { SecretariaHorariosProfessorPage } from "../pages/secretaria/SecretariaHorariosProfessorPage";
import { SecretariaHorariosCalendarioPage } from "../pages/secretaria/SecretariaHorariosCalendarioPage";
import { SecretariaHorariosTurmaPage } from "../pages/secretaria/SecretariaHorariosTurmaPage";
import { SecretariaMatrizPage } from "../pages/secretaria/SecretariaMatrizPage";
import { SecretariaRematriculaPage } from "../pages/secretaria/SecretariaRematriculaPage";
import { SecretariaMatriculaNovaPage } from "../pages/secretaria/SecretariaMatriculaNovaPage";
import { SecretariaPatrimonioPage } from "../pages/secretaria/SecretariaPatrimonioPage";
import { GaleriaGestaoPage } from "../pages/galeria/GaleriaGestaoPage";
import { GestaoHojePage } from "../pages/secretaria/GestaoHojePage";

/** Rotas de gestao escolar reutilizadas em /secretaria e /direcao. */
export function gestaoEscolarRoutes(prefix: "secretaria" | "direcao"): RouteObject[] {
  return [
    {
      path: `${prefix}/hoje`,
      element: (
        <ProtectedRoute>
          <GestaoHojePage />
        </ProtectedRoute>
      ),
    },
    {
      path: `${prefix}/comunicacao`,
      element: (
        <ProtectedRoute>
          <SecretariaComunicacaoLayout />
        </ProtectedRoute>
      ),
      children: [
        { index: true, element: <Navigate replace to="comunicados" /> },
        { path: "comunicados", element: <SecretariaComunicacaoComunicadosPage /> },
        { path: "eventos", element: <SecretariaComunicacaoEventosPage /> },
        { path: "calendario", element: <SecretariaComunicacaoCalendarioPage /> },
      ],
    },
    {
      path: `${prefix}/academico`,
      element: (
        <ProtectedRoute>
          <SecretariaAcademicoLayout />
        </ProtectedRoute>
      ),
      children: [
        { index: true, element: <Navigate replace to="disciplinas" /> },
        { path: "disciplinas", element: <SecretariaAcademicoDisciplinasPage /> },
        { path: "professores", element: <SecretariaAcademicoProfessoresPage /> },
        { path: "turmas", element: <SecretariaAcademicoTurmasPage /> },
        { path: "vinculos", element: <SecretariaAcademicoVinculosPage /> },
      ],
    },
    {
      path: `${prefix}/rematricula`,
      element: (
        <ProtectedRoute>
          <SecretariaRematriculaPage />
        </ProtectedRoute>
      ),
    },
    {
      path: `${prefix}/matricula-nova`,
      element: (
        <ProtectedRoute>
          <SecretariaMatriculaNovaPage />
        </ProtectedRoute>
      ),
    },
    {
      path: `${prefix}/matriz`,
      element: (
        <ProtectedRoute>
          <SecretariaMatrizPage />
        </ProtectedRoute>
      ),
    },
    {
      path: `${prefix}/cadastro`,
      element: (
        <ProtectedRoute>
          <SecretariaCadastroLayout />
        </ProtectedRoute>
      ),
      children: [
        { index: true, element: <Navigate replace to="escola" /> },
        { path: "escola", element: <SecretariaCadastroEscolaPage /> },
        { path: "alunos", element: <SecretariaCadastroAlunosPage /> },
        { path: "responsaveis", element: <SecretariaCadastroResponsaveisPage /> },
        { path: "usuarios", element: <SecretariaCadastroUsuariosPage /> },
      ],
    },
    {
      path: `${prefix}/horarios`,
      element: (
        <ProtectedRoute>
          <SecretariaHorariosLayout />
        </ProtectedRoute>
      ),
      children: [
        { index: true, element: <Navigate replace to="grade" /> },
        { path: "grade", element: <SecretariaHorariosGradePage /> },
        { path: "turma", element: <SecretariaHorariosTurmaPage /> },
        { path: "disciplina", element: <SecretariaHorariosDisciplinaPage /> },
        { path: "professor", element: <SecretariaHorariosProfessorPage /> },
        { path: "calendario", element: <SecretariaHorariosCalendarioPage /> },
      ],
    },
    {
      path: `${prefix}/patrimonio`,
      element: (
        <ProtectedRoute>
          <SecretariaPatrimonioPage />
        </ProtectedRoute>
      ),
    },
    {
      path: `${prefix}/galeria`,
      element: (
        <ProtectedRoute>
          <GaleriaGestaoPage />
        </ProtectedRoute>
      ),
    },
    {
      path: `${prefix}/financeiro`,
      element: (
        <ProtectedRoute>
          <SecretariaFinanceiroPage />
        </ProtectedRoute>
      ),
    },
  ];
}
