export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthTokensResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface FilhoResumo {
  alunoId: string;
  nome: string;
  matricula?: string;
  turmaNome: string | null;
  turmaId: string | null;
  /** Autorização LGPD para uso de imagem (galeria). Default true. */
  autorizaUsoImagem?: boolean;
}

export interface Comunicado {
  id: string;
  titulo: string;
  conteudo: string;
  publicadoEm: string;
  visivelPara: string;
  turmaId: string | null;
  turmaNome: string | null;
  publicadoPorNome: string | null;
}

export type TipoRefeicao = "ALMOCO" | "LANCHE";

export interface CardapioItem {
  id: string;
  dataRefeicao: string;
  tipoRefeicao: TipoRefeicao;
  descricao: string;
  calorias: number | null;
  nutricionistaNome: string | null;
}

export type TipoEvento = "REUNIAO" | "FERIADO" | "PROVA" | "EVENTO";

export interface AgendaEvent {
  id: string;
  titulo: string;
  descricao: string | null;
  dataInicio: string;
  dataFim: string | null;
  tipo: TipoEvento | null;
  turmaId: string | null;
  turmaNome: string | null;
}

export interface UserMe {
  usuarioId: string;
  nome: string;
  email: string;
  telefone: string | null;
  perfil: string;
  responsavelId: string | null;
  professorId: string | null;
  profissionalSaudeId: string | null;
  alunoId: string | null;
  turmaId: string | null;
  turmaNome: string | null;
  filhos: FilhoResumo[];
  /** Áreas do menu superior habilitadas para o perfil. */
  areasMenu?: string[];
}

export interface HorarioAula {
  id: string;
  turmaId: string;
  turmaNome: string;
  diaSemana: number;
  horaInicio: string;
  horaFim: string;
  disciplinaId: string;
  disciplinaNome: string;
  professorId: string | null;
  professorNome: string | null;
}

export interface TurmaDesempenho {
  turmaId: string;
  mediaTurma: number;
  alunosEmRisco: number;
  alunos: { alunoId: string; alunoNome: string; mediaGeral: number; emRisco: boolean }[];
}

export interface TurmaFrequencia {
  turmaId: string;
  alunosEmRisco: number;
  alunos: { alunoId: string; alunoNome: string; percentual: number; emRisco: boolean }[];
}

export interface AnaliseAluno {
  alunoId: string;
  alunoNome: string;
  turmaNome: string | null;
  mediaGeral: number;
  percentualFrequencia: number;
  emRisco: boolean;
  alertas: string[];
  relatorio: string;
  modo: string;
  score?: number;
  situacao?: "OTIMO" | "ATENCAO" | "CRITICO";
  tags?: string[];
  pontosFortes?: string[];
  pontosAtencao?: string[];
  sugestoesCoordenacao?: string[];
  sugestoesFamilia?: string[];
  disciplinasCriticas?: { disciplinaNome: string; media: number }[];
}

export interface InadimplenciaEscola {
  mes: string;
  totalVencido: number;
  totalPendente: number;
  totalRecebido: number;
  quantidadeInadimplentes: number;
}

export interface AlunoResumoVinculo {
  responsavelId: string;
  nome: string;
  grauParentesco: string | null;
}

export interface AlunoCadastro {
  id: string;
  nome: string;
  matricula: string;
  status: string;
  turmaId: string | null;
  turmaNome: string | null;
  responsaveis?: AlunoResumoVinculo[];
}

export interface ResponsavelAlunoVinculo {
  alunoId: string;
  nome: string;
}

export interface ResponsavelCadastro {
  id: string;
  nome: string;
  email: string | null;
  grauParentesco: string | null;
  usuarioEmail: string | null;
  alunos?: ResponsavelAlunoVinculo[];
}

export interface EscolaCadastro {
  id: string;
  nome: string;
  cnpj: string | null;
  slug?: string | null;
  municipio?: string | null;
  uf?: string | null;
  packageId?: string | null;
  notaMinimaAprovacao: number;
  frequenciaMinima: number;
}

export interface PlanoPagamentoCadastro {
  id: string;
  nome: string;
  valorMensalidade: number;
  diaVencimento: number;
}

export interface DisciplinaCadastro {
  id: string;
  nome: string;
  codigo: string | null;
}

export interface ProfessorCadastro {
  id: string;
  nome: string;
  email: string | null;
  registroMec: string | null;
}

export interface SerieCadastro {
  id: string;
  nome: string;
  nivelNome: string;
}

export interface GerarCobrancasMesResult {
  competencia: string;
  criadas: number;
  ignoradas: number;
}

export interface AgendamentoSaude {
  id: string;
  alunoId: string;
  alunoNome: string;
  dataHora: string;
  status: string;
  privado: boolean;
  observacoes: string | null;
}

export interface Turma {
  id: string;
  nome: string;
  serieId?: string;
  serieNome: string;
  nivelNome: string;
  anoLetivo: number;
}

export interface TurmaAluno {
  id: string;
  nome: string;
  matricula: string;
}

export interface DisciplinaVinculo {
  id: string;
  disciplinaId: string;
  disciplinaNome: string;
  professorNome: string;
}

export interface PeriodoAvaliacao {
  id: string;
  nome: string;
  dataInicio: string | null;
  dataFim: string | null;
}

export type TipoNota = "PROVA" | "TRABALHO" | "PARTICIPACAO" | "FINAL" | "COMPLEMENTAR";

export type DiarioNotaCelula = {
  periodoId: string;
  notaId: string | null;
  valor: number | null;
  origem: string | null;
};

export type DiarioNotaLinha = {
  alunoId: string;
  nome: string;
  matricula: string;
  periodos: DiarioNotaCelula[];
  complemento: {
    periodoId: string | null;
    periodoNome: string | null;
    notaId: string | null;
    valor: number | null;
  };
};

export type DiarioNotas = {
  turmaDisciplinaProfessorId: string;
  disciplinaNome: string;
  periodos: PeriodoAvaliacao[];
  periodoComplementoId: string | null;
  alunos: DiarioNotaLinha[];
};

export type TipoOcorrencia = "ADVERTENCIA" | "ATENCAO" | "ELOGIO" | "OUTRO";

export type OcorrenciaDisciplinar = {
  id: string;
  alunoId: string;
  alunoNome: string;
  turmaDisciplinaProfessorId: string;
  disciplinaNome: string;
  dataOcorrencia: string;
  tipo: TipoOcorrencia;
  descricao: string;
  status: "REGISTRADA" | "VISTA";
  criadoEm: string;
};

export type AtaAulaResumo = {
  id: string;
  dataAula: string;
  conteudoResumo: string | null;
  temTarefa: boolean;
  atualizadoEm: string;
};

export interface BoletimNota {
  id: string;
  tipo: TipoNota;
  valor: number;
  observacao: string | null;
}

export interface BoletimDisciplina {
  disciplinaId: string;
  disciplinaNome: string;
  notas: BoletimNota[];
  media: number;
  aprovado: boolean;
}

export interface BoletimPeriodo {
  periodoId: string;
  periodoNome: string;
  disciplinas: BoletimDisciplina[];
  mediaGeral: number;
  aprovado: boolean;
}

export interface Boletim {
  alunoId: string;
  alunoNome: string;
  turmaNome: string | null;
  notaMinimaAprovacao: number;
  periodos: BoletimPeriodo[];
}

export interface FrequenciaDisciplina {
  disciplinaId: string;
  disciplinaNome: string;
  totalAulas: number;
  presencas: number;
  faltas: number;
  percentual: number;
  aprovado: boolean;
}

export interface Frequencia {
  alunoId: string;
  percentualGeral: number;
  frequenciaMinima: number;
  aprovadoFrequencia: boolean;
  porDisciplina: FrequenciaDisciplina[];
}

export type ChargeStatus = "PENDENTE" | "PAGO" | "VENCIDO" | "CANCELADO";

export interface Charge {
  id: string;
  responsavelId: string;
  alunoNome: string;
  competencia: string;
  valor: number;
  vencimento: string;
  status: ChargeStatus;
  pagoEm: string | null;
}

export type PixModo = "SIMULACAO" | "OPENPIX";

export interface PixConfig {
  modo: PixModo;
}

export interface ChargeQrCode {
  cobrancaId: string;
  qrCode: string;
  pixCopyPaste: string;
  qrCodeImageUrl?: string | null;
  pixModo?: PixModo;
  valor: number;
  status: ChargeStatus;
}

export interface MonthlyReport {
  mes: string;
  totalRecebido: number;
  totalPendente: number;
  totalVencido: number;
}

export interface Defaulter {
  id: string;
  contratoId: string;
  alunoNome: string;
  competencia: string;
  valor: number;
  vencimento: string;
  status: ChargeStatus;
  diasAtraso: number;
}

export interface Contract {
  id: string;
  alunoId?: string;
  alunoNome: string;
  matricula: string;
  planoNome: string;
  valorMensalidade: number;
  diaVencimento: number;
  dataInicio?: string;
}

export type CampoRematriculaTipo = "TEXTO" | "TEXTO_LONGO" | "BOOLEAN" | "DATA" | "SELECAO";

export interface CampoFormularioRematricula {
  id: string;
  rotulo: string;
  tipo: CampoRematriculaTipo;
  obrigatorio: boolean;
  ordem: number;
  opcoes: string[] | null;
}

export interface SecaoFormularioRematricula {
  id: string;
  titulo: string;
  ordem: number;
  campos: CampoFormularioRematricula[];
}

export interface FormularioRematricula {
  secoes: SecaoFormularioRematricula[];
}

export interface RematriculaConfig {
  id: string;
  titulo: string;
  habilitada: boolean;
  anoLetivo: number | null;
  anoLetivoId: string | null;
  possuiModeloPdf: boolean;
  pdfModeloNome: string | null;
  formulario: FormularioRematricula;
  sugestoesExtracao: string[];
  publicadoEm: string | null;
  atualizadoEm: string;
}

export interface AlunoRematriculaPortal {
  alunoId: string;
  alunoNome: string;
  turmaNome: string | null;
  statusSubmissao: string | null;
  respostas: Record<string, unknown>;
}

export interface RematriculaPortal {
  habilitada: boolean;
  titulo: string;
  formulario: FormularioRematricula;
  alunos: AlunoRematriculaPortal[];
}

export interface CampoRevisaoRematricula {
  rotulo: string;
  valorExibido: string;
  campoId: string;
}

export interface SecaoRevisaoRematricula {
  titulo: string;
  campos: CampoRevisaoRematricula[];
}

export interface RematriculaRevisao {
  alunoId: string;
  alunoNome: string;
  tituloFormulario: string;
  secoes: SecaoRevisaoRematricula[];
  erros: string[];
}

export interface RematriculaSubmissaoResumo {
  id: string;
  alunoId: string;
  alunoNome: string;
  turmaNome: string | null;
  status: string;
  enviadoEm: string | null;
  validadoSecretariaEm: string | null;
}

export type StatusMatriculaProcesso =
  | "RASCUNHO"
  | "EM_ANALISE"
  | "APROVADO"
  | "REJEITADO"
  | "CONCLUIDO";

export type TipoDocumentoMatricula =
  | "RG"
  | "CPF"
  | "COMPROVANTE_RESIDENCIA"
  | "CERTIDAO_NASCIMENTO"
  | "FOTO"
  | "OUTRO";

export type MatriculaDocumentoResumo = {
  id: string;
  tipo: TipoDocumentoMatricula;
  nomeArquivo: string;
  contentType: string;
  tamanhoBytes: number;
  enviadoEm: string;
};

export type MatriculaProcessoResumo = {
  id: string;
  status: StatusMatriculaProcesso;
  candidatoNome: string;
  matriculaSugerida: string | null;
  observacoes: string | null;
  motivoRejeicao: string | null;
  responsavelNome: string | null;
  responsavelEmail: string | null;
  responsavelTelefone: string | null;
  anoLetivoId: string;
  anoLetivo: number;
  turmaPretendidaId?: string;
  turmaPretendidaNome?: string;
  responsavelId?: string;
  alunoId?: string;
  enviadoEm: string | null;
  aprovadoEm: string | null;
  concluidoEm: string | null;
  criadoEm: string;
  documentos?: MatriculaDocumentoResumo[];
};

export type AnoLetivoResumo = {
  id: string;
  ano: number;
};
