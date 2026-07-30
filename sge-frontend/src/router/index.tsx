import { Navigate, createBrowserRouter } from "react-router-dom";
import App from "../App";
import { ProtectedRoute } from "../components/layout/ProtectedRoute";
import { LoginPage } from "../pages/auth/LoginPage";
import { NutricaoPage } from "../pages/nutricao/NutricaoPage";
import { ParentAcademicPage } from "../pages/pais/ParentAcademicPage";
import { ParentChargesPage } from "../pages/pais/ParentChargesPage";
import { ParentCommunicationPage } from "../pages/pais/ParentCommunicationPage";
import { ParentHealthPage } from "../pages/pais/ParentHealthPage";
import { ParentHorariosPage } from "../pages/pais/ParentHorariosPage";
import { AlunoCommunicationPage } from "../pages/aluno/AlunoCommunicationPage";
import { AlunoDesempenhoPage } from "../pages/aluno/AlunoDesempenhoPage";
import { AlunoHorariosPage } from "../pages/aluno/AlunoHorariosPage";
import { ProfilePage } from "../pages/perfil/ProfilePage";
import { ProfessorLayout } from "../pages/professor/ProfessorLayout";
import { ProfessorInicioPage } from "../pages/professor/ProfessorInicioPage";
import { ProfessorAtaPage } from "../pages/professor/ProfessorAtaPage";
import { ProfessorNotasPage } from "../pages/professor/ProfessorNotasPage";
import { ProfessorFrequenciaPage } from "../pages/professor/ProfessorFrequenciaPage";
import { ProfessorHorariosPage } from "../pages/professor/ProfessorHorariosPage";
import { ProfessorDiarioPage } from "../pages/professor/ProfessorDiarioPage";
import { ProfessorOcorrenciasPage } from "../pages/professor/ProfessorOcorrenciasPage";
import { CoordenacaoPage } from "../pages/coordenacao/CoordenacaoPage";
import { DirecaoPage } from "../pages/direcao/DirecaoPage";
import { PsicologiaPage } from "../pages/psicologia/PsicologiaPage";
import { ParentFilhosPage } from "../pages/pais/ParentFilhosPage";
import { ParentRematriculaPage } from "../pages/pais/ParentRematriculaPage";
import { ParentGaleriaPage } from "../pages/pais/ParentGaleriaPage";
import { ParentHojePage } from "../pages/pais/ParentHojePage";
import { AlunoGaleriaPage } from "../pages/aluno/AlunoGaleriaPage";
import { AlunoHojePage } from "../pages/aluno/AlunoHojePage";
import { ProfessorGaleriaPage } from "../pages/professor/ProfessorGaleriaPage";
import { ProfessorHojePage } from "../pages/professor/ProfessorHojePage";
import { AdminAcessosMenuPage } from "../pages/admin/AdminAcessosMenuPage";
import { gestaoEscolarRoutes } from "./gestaoRoutes";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { index: true, element: <Navigate replace to="/login" /> },
      { path: "login", element: <LoginPage /> },
      {
        path: "admin/acessos",
        element: (
          <ProtectedRoute>
            <AdminAcessosMenuPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "admin",
        element: <Navigate replace to="/admin/acessos" />,
      },
      {
        path: "pais/hoje",
        element: (
          <ProtectedRoute>
            <ParentHojePage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/cobrancas",
        element: (
          <ProtectedRoute>
            <ParentChargesPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/desempenho",
        element: (
          <ProtectedRoute>
            <ParentAcademicPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/comunicacao",
        element: (
          <ProtectedRoute>
            <ParentCommunicationPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/saude",
        element: (
          <ProtectedRoute>
            <ParentHealthPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/horarios",
        element: (
          <ProtectedRoute>
            <ParentHorariosPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/filhos",
        element: (
          <ProtectedRoute>
            <ParentFilhosPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/rematricula",
        element: (
          <ProtectedRoute>
            <ParentRematriculaPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "pais/galeria",
        element: (
          <ProtectedRoute>
            <ParentGaleriaPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "aluno/hoje",
        element: (
          <ProtectedRoute>
            <AlunoHojePage />
          </ProtectedRoute>
        ),
      },
      {
        path: "aluno/desempenho",
        element: (
          <ProtectedRoute>
            <AlunoDesempenhoPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "aluno/comunicacao",
        element: (
          <ProtectedRoute>
            <AlunoCommunicationPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "aluno/horarios",
        element: (
          <ProtectedRoute>
            <AlunoHorariosPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "aluno/galeria",
        element: (
          <ProtectedRoute>
            <AlunoGaleriaPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "perfil",
        element: (
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        ),
      },
      {
        path: "professor",
        element: (
          <ProtectedRoute>
            <ProfessorLayout />
          </ProtectedRoute>
        ),
        children: [
          { index: true, element: <Navigate replace to="hoje" /> },
          { path: "hoje", element: <ProfessorHojePage /> },
          { path: "inicio", element: <ProfessorInicioPage /> },
          { path: "diario", element: <ProfessorDiarioPage /> },
          { path: "ata", element: <ProfessorAtaPage /> },
          { path: "notas", element: <ProfessorNotasPage /> },
          { path: "frequencia", element: <ProfessorFrequenciaPage /> },
          { path: "ocorrencias", element: <ProfessorOcorrenciasPage /> },
          { path: "galeria", element: <ProfessorGaleriaPage /> },
          { path: "horarios", element: <ProfessorHorariosPage /> },
        ],
      },
      {
        path: "coordenacao",
        element: (
          <ProtectedRoute>
            <CoordenacaoPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "direcao",
        element: (
          <ProtectedRoute>
            <DirecaoPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "psicologia",
        element: (
          <ProtectedRoute>
            <PsicologiaPage />
          </ProtectedRoute>
        ),
      },
      {
        path: "nutricao",
        element: (
          <ProtectedRoute>
            <NutricaoPage />
          </ProtectedRoute>
        ),
      },
      ...gestaoEscolarRoutes("secretaria"),
      ...gestaoEscolarRoutes("direcao"),
      {
        path: "*",
        element: <div className="text-sm text-slate-600">Selecione um modulo no menu.</div>,
      },
    ],
  },
]);
