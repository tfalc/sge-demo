import { useCallback, useEffect, useState } from "react";
import type { SectionNavItem } from "../layout/SectionNav";
import { SectionNav } from "../layout/SectionNav";
import { GaleriaFotoImage } from "./GaleriaFotoImage";
import { Button } from "../ui/Button";
import { EmptyState, PageSkeleton } from "../ui/EmptyState";
import { Input } from "../ui/Input";
import { getTurmas } from "../../services/academicoService";
import {
  criarAlbumGaleria,
  excluirAlbumGaleria,
  excluirFotoGaleria,
  listarAlbunsGaleria,
  obterAlbumGaleria,
  uploadFotoGaleria,
  type GaleriaAlbum,
  type GaleriaAlbumDetalhe,
} from "../../services/galeriaService";
import type { Turma } from "../../types";
import { formatDateTime } from "../../utils/dateRange";

type Props = {
  /** Se omitido, o cabeçalho não é renderizado (útil quando o layout pai já tem título). */
  title?: string;
  subtitle?: string;
  /** Só passe se a página/layout ainda não renderizou o SectionNav. */
  sectionNav?: SectionNavItem[];
  canManage: boolean;
  audiencia?: string;
  turmaId?: string;
  gestao?: boolean;
  /** LGPD: false oculta álbuns que exigem consentimento de imagem. */
  autorizadoImagem?: boolean;
};

export function GaleriaPanel({
  title,
  subtitle,
  sectionNav,
  canManage,
  audiencia,
  turmaId,
  gestao = false,
  autorizadoImagem = true,
}: Props) {
  const listParams = {
    audiencia,
    turmaId,
    gestao: gestao || canManage,
    autorizadoImagem,
  };
  const [albuns, setAlbuns] = useState<GaleriaAlbum[]>([]);
  const [turmas, setTurmas] = useState<Turma[]>([]);
  const [selecionado, setSelecionado] = useState<GaleriaAlbumDetalhe | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");
  const [visivelPara, setVisivelPara] = useState("TODOS");
  const [novoTurmaId, setNovoTurmaId] = useState("");
  const [exigirConsentimento, setExigirConsentimento] = useState(true);
  const [legenda, setLegenda] = useState("");

  const loadAlbuns = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [lista, t] = await Promise.all([
        listarAlbunsGaleria(listParams),
        canManage ? getTurmas() : Promise.resolve([] as Turma[]),
      ]);
      setAlbuns(lista);
      setTurmas(t);
    } catch {
      setError("Não foi possível carregar a galeria. Confirme a API e tente novamente.");
    } finally {
      setLoading(false);
    }
  }, [canManage, audiencia, turmaId, gestao, autorizadoImagem]);

  const loadAlbum = useCallback(
    async (id: string) => {
      setError(null);
      try {
        const detalhe = await obterAlbumGaleria(id, listParams);
        setSelecionado(detalhe);
      } catch {
        setError("Não foi possível abrir o álbum. Verifique o consentimento de imagem (LGPD).");
      }
    },
    [audiencia, turmaId, gestao, canManage, autorizadoImagem],
  );

  useEffect(() => {
    void loadAlbuns();
  }, [loadAlbuns]);

  async function handleCriarAlbum(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const album = await criarAlbumGaleria({
        titulo,
        descricao: descricao || undefined,
        visivelPara,
        turmaId: novoTurmaId || null,
        exigirConsentimentoImagem: exigirConsentimento,
      });
      setTitulo("");
      setDescricao("");
      setNovoTurmaId("");
      setSuccess("Álbum criado.");
      await loadAlbuns();
      await loadAlbum(album.id);
    } catch {
      setError("Falha ao criar álbum. Preencha título e tente novamente.");
    }
  }

  async function handleUpload(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!selecionado) return;
    const input = e.currentTarget.elements.namedItem("foto") as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      setError("Selecione uma imagem.");
      return;
    }
    setUploading(true);
    setError(null);
    setSuccess(null);
    try {
      await uploadFotoGaleria(selecionado.id, file, legenda || undefined);
      setLegenda("");
      input.value = "";
      setSuccess("Foto publicada.");
      await loadAlbum(selecionado.id);
      await loadAlbuns();
    } catch {
      setError("Falha ao enviar foto. Use JPEG, PNG, WebP ou GIF (máx. 10 MB).");
    } finally {
      setUploading(false);
    }
  }

  async function handleExcluirAlbum(id: string) {
    if (!window.confirm("Excluir álbum e todas as fotos?")) return;
    try {
      await excluirAlbumGaleria(id);
      setSelecionado(null);
      setSuccess("Álbum excluído.");
      await loadAlbuns();
    } catch {
      setError("Falha ao excluir álbum.");
    }
  }

  async function handleExcluirFoto(id: string) {
    if (!selecionado) return;
    if (!window.confirm("Excluir esta foto?")) return;
    try {
      await excluirFotoGaleria(id);
      setSuccess("Foto excluída.");
      await loadAlbum(selecionado.id);
      await loadAlbuns();
    } catch {
      setError("Falha ao excluir foto.");
    }
  }

  return (
    <div className="space-y-6">
      {title ? (
        <div>
          <h2 className="text-xl font-semibold text-slate-900">{title}</h2>
          {subtitle ? <p className="mt-1 text-sm text-slate-600">{subtitle}</p> : null}
        </div>
      ) : null}

      {sectionNav ? <SectionNav items={sectionNav} /> : null}

      {error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">{error}</div>
      ) : null}
      {success ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {success}
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)]">
        <section className="space-y-4">
          {canManage ? (
            <form
              className="space-y-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
              onSubmit={(e) => void handleCriarAlbum(e)}
            >
              <h3 className="font-semibold text-slate-900">Novo álbum</h3>
              <Input label="Título" value={titulo} onChange={(e) => setTitulo(e.target.value)} required />
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Descrição</span>
                <textarea
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  rows={2}
                  value={descricao}
                  onChange={(e) => setDescricao(e.target.value)}
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Visível para</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={visivelPara}
                  onChange={(e) => setVisivelPara(e.target.value)}
                >
                  <option value="TODOS">Todos</option>
                  <option value="PAIS">Pais</option>
                  <option value="ALUNOS">Alunos</option>
                  <option value="PROFESSORES">Professores</option>
                  <option value="PAIS,ALUNOS">Pais e alunos</option>
                </select>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block font-medium text-slate-700">Turma (opcional)</span>
                <select
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={novoTurmaId}
                  onChange={(e) => setNovoTurmaId(e.target.value)}
                >
                  <option value="">Toda a escola</option>
                  {turmas.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.nome}
                    </option>
                  ))}
                </select>
              </label>
              <label className="flex items-start gap-2 text-sm text-slate-800">
                <input
                  type="checkbox"
                  className="mt-1"
                  checked={exigirConsentimento}
                  onChange={(e) => setExigirConsentimento(e.target.checked)}
                />
                <span>
                  Exigir consentimento de uso de imagem (LGPD). Famílias sem autorização não verão este álbum.
                </span>
              </label>
              <Button type="submit">Criar álbum</Button>
            </form>
          ) : null}

          <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="font-semibold text-slate-900">Álbuns</h3>
            {loading ? (
              <div className="mt-3">
                <PageSkeleton />
              </div>
            ) : albuns.length === 0 ? (
              <div className="mt-3">
                <EmptyState
                  title="Nenhum álbum publicado"
                  description={
                    canManage
                      ? "Crie o primeiro álbum ao lado para compartilhar fotos com a comunidade."
                      : "Quando a escola publicar fotos, elas aparecem aqui."
                  }
                />
              </div>
            ) : (
              <ul className="mt-3 space-y-2">
                {albuns.map((album) => (
                  <li key={album.id}>
                    <button
                      type="button"
                      onClick={() => void loadAlbum(album.id)}
                      className={[
                        "w-full rounded-lg border px-3 py-2.5 text-left transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue",
                        selecionado?.id === album.id
                          ? "border-brand-blue bg-sky-50"
                          : "border-slate-200 hover:bg-slate-50",
                      ].join(" ")}
                    >
                      <p className="font-medium text-slate-900">{album.titulo}</p>
                      <p className="text-xs text-slate-500">
                        {album.quantidadeFotos} foto(s) · {formatDateTime(album.publicadoEm)}
                        {album.exigirConsentimentoImagem ? " · LGPD" : ""}
                      </p>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>

        <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          {!selecionado ? (
            <EmptyState
              title="Selecione um álbum"
              description="Escolha um álbum à esquerda para ver as fotos."
            />
          ) : (
            <div className="space-y-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <h3 className="text-lg font-semibold text-slate-900">{selecionado.titulo}</h3>
                  {selecionado.descricao ? (
                    <p className="mt-1 text-sm text-slate-600">{selecionado.descricao}</p>
                  ) : null}
                  <p className="mt-1 text-xs text-slate-500">
                    {selecionado.visivelPara}
                    {selecionado.turmaNome ? ` · ${selecionado.turmaNome}` : ""}
                    {selecionado.publicadoPorNome ? ` · ${selecionado.publicadoPorNome}` : ""}
                    {selecionado.exigirConsentimentoImagem ? " · exige consentimento" : ""}
                  </p>
                </div>
                {canManage ? (
                  <Button type="button" variant="danger" onClick={() => void handleExcluirAlbum(selecionado.id)}>
                    Excluir álbum
                  </Button>
                ) : null}
              </div>

              {canManage ? (
                <form
                  className="flex flex-wrap items-end gap-3 border-b border-slate-200 pb-4"
                  onSubmit={(e) => void handleUpload(e)}
                >
                  <label className="text-sm">
                    <span className="mb-1 block font-medium text-slate-700">Nova foto</span>
                    <input name="foto" type="file" accept="image/jpeg,image/png,image/webp,image/gif" required />
                  </label>
                  <Input label="Legenda" value={legenda} onChange={(e) => setLegenda(e.target.value)} />
                  <Button type="submit" disabled={uploading}>
                    {uploading ? "Enviando..." : "Publicar foto"}
                  </Button>
                </form>
              ) : null}

              {selecionado.fotos.length === 0 ? (
                <EmptyState
                  title="Álbum sem fotos"
                  description={canManage ? "Envie a primeira foto acima." : "Aguarde a publicação de fotos."}
                />
              ) : (
                <div className="grid gap-4 sm:grid-cols-2">
                  {selecionado.fotos.map((foto) => (
                    <figure key={foto.id} className="overflow-hidden rounded-lg border border-slate-200">
                      <GaleriaFotoImage
                        fotoId={foto.id}
                        audiencia={audiencia}
                        turmaId={turmaId}
                        gestao={gestao || canManage}
                        autorizadoImagem={autorizadoImagem}
                        alt={foto.legenda ?? foto.nomeArquivo}
                        className="aspect-[4/3] w-full object-cover"
                      />
                      <figcaption className="flex items-start justify-between gap-2 p-2 text-sm">
                        <span className="text-slate-700">{foto.legenda ?? foto.nomeArquivo}</span>
                        {canManage ? (
                          <button
                            type="button"
                            className="text-red-600 hover:underline focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-blue"
                            onClick={() => void handleExcluirFoto(foto.id)}
                          >
                            Excluir
                          </button>
                        ) : null}
                      </figcaption>
                    </figure>
                  ))}
                </div>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
