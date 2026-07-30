package br.com.sge.modules.galeria.service;

import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.galeria.dto.CriarGaleriaAlbumRequest;
import br.com.sge.modules.galeria.entity.GaleriaAlbum;
import br.com.sge.modules.galeria.entity.GaleriaFoto;
import br.com.sge.modules.galeria.repository.GaleriaAlbumRepository;
import br.com.sge.modules.galeria.repository.GaleriaFotoRepository;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.shared.storage.DocumentStorageService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GaleriaService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final GaleriaAlbumRepository albumRepository;
    private final GaleriaFotoRepository fotoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final DocumentStorageService documentStorage;
    private final NotificacaoService notificacaoService;

    public GaleriaService(
            GaleriaAlbumRepository albumRepository,
            GaleriaFotoRepository fotoRepository,
            UsuarioRepository usuarioRepository,
            TurmaRepository turmaRepository,
            DocumentStorageService documentStorage,
            NotificacaoService notificacaoService) {
        this.albumRepository = albumRepository;
        this.fotoRepository = fotoRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.documentStorage = documentStorage;
        this.notificacaoService = notificacaoService;
    }

    public record FotoDownload(String filename, String contentType, byte[] bytes) {}

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAlbuns(
            String audiencia, UUID turmaId, boolean gestao, boolean autorizadoImagem) {
        List<GaleriaAlbum> lista;
        if (gestao) {
            lista = albumRepository.findAllDetalhados();
        } else if (audiencia == null || audiencia.isBlank()) {
            lista = albumRepository.findAllDetalhados();
        } else if (turmaId != null) {
            lista = albumRepository.findVisiveisParaAudienciaETurma(audiencia.trim().toUpperCase(), turmaId);
        } else {
            lista = albumRepository.findVisiveisParaAudiencia(audiencia.trim().toUpperCase());
        }
        return lista.stream()
                .filter(a -> albumPermitidoPorLgpd(a, gestao, autorizadoImagem))
                .map(this::toAlbumMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterAlbum(
            UUID albumId, String audiencia, UUID turmaId, boolean gestao, boolean autorizadoImagem) {
        GaleriaAlbum album = albumRepository
                .findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album nao encontrado"));
        if (!gestao && !albumVisivel(album, audiencia, turmaId)) {
            throw new IllegalArgumentException("Album nao disponivel para este perfil");
        }
        if (!albumPermitidoPorLgpd(album, gestao, autorizadoImagem)) {
            throw new IllegalArgumentException("Album requer consentimento de uso de imagem (LGPD)");
        }
        Map<String, Object> map = toAlbumMap(album);
        map.put(
                "fotos",
                fotoRepository.findByAlbumId(albumId).stream().map(this::toFotoMap).toList());
        return map;
    }

    @Transactional
    public Map<String, Object> criarAlbum(CriarGaleriaAlbumRequest req, String emailAutor) {
        Usuario autor = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(emailAutor)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        GaleriaAlbum album = new GaleriaAlbum();
        album.setTitulo(req.titulo().trim());
        album.setDescricao(req.descricao() != null ? req.descricao().trim() : null);
        album.setVisivelPara(req.visivelPara().trim().toUpperCase());
        album.setExigirConsentimentoImagem(
                req.exigirConsentimentoImagem() != null && req.exigirConsentimentoImagem());
        album.setPublicadoPor(autor);
        album.setPublicadoEm(Instant.now());

        if (req.turmaId() != null) {
            Turma turma = turmaRepository
                    .findById(req.turmaId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
            album.setTurma(turma);
        }

        GaleriaAlbum saved = albumRepository.save(album);
        notificacaoService.notificarGaleria(
                saved.getVisivelPara(),
                saved.getTurma() != null ? saved.getTurma().getId() : null,
                saved.getTitulo(),
                saved.getId());
        return toAlbumMap(saved);
    }

    @Transactional
    public Map<String, Object> uploadFoto(UUID albumId, MultipartFile file, String legenda) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo obrigatorio");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Arquivo excede 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato invalido. Use JPEG, PNG, WebP ou GIF.");
        }

        GaleriaAlbum album = albumRepository
                .findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album nao encontrado"));

        UUID fotoId = UUID.randomUUID();
        String nome = sanitizeFilename(file.getOriginalFilename());
        String storageKey = "galeria/" + albumId + "/" + fotoId + "/" + nome;

        try {
            documentStorage.store(storageKey, file.getBytes(), contentType);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao armazenar imagem", ex);
        }

        GaleriaFoto foto = new GaleriaFoto();
        foto.setAlbum(album);
        foto.setNomeArquivo(nome);
        foto.setContentType(contentType);
        foto.setTamanhoBytes(file.getSize());
        foto.setStorageKey(storageKey);
        foto.setLegenda(legenda != null && !legenda.isBlank() ? legenda.trim() : null);
        foto.setOrdem(fotoRepository.countByAlbumId(albumId));
        foto.setEnviadoEm(Instant.now());

        return toFotoMap(fotoRepository.save(foto));
    }

    @Transactional(readOnly = true)
    public FotoDownload downloadFoto(
            UUID fotoId, String audiencia, UUID turmaId, boolean gestao, boolean autorizadoImagem) {
        GaleriaFoto foto = fotoRepository
                .findDetalhada(fotoId)
                .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada"));
        GaleriaAlbum album = foto.getAlbum();
        if (!gestao && !albumVisivel(album, audiencia, turmaId)) {
            throw new IllegalArgumentException("Foto nao disponivel para este perfil");
        }
        if (!albumPermitidoPorLgpd(album, gestao, autorizadoImagem)) {
            throw new IllegalArgumentException("Foto requer consentimento de uso de imagem (LGPD)");
        }
        byte[] bytes = documentStorage.load(foto.getStorageKey());
        return new FotoDownload(foto.getNomeArquivo(), foto.getContentType(), bytes);
    }

    @Transactional
    public void excluirAlbum(UUID albumId) {
        GaleriaAlbum album = albumRepository
                .findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album nao encontrado"));
        for (GaleriaFoto foto : fotoRepository.findByAlbumId(albumId)) {
            documentStorage.delete(foto.getStorageKey());
        }
        albumRepository.delete(album);
    }

    @Transactional
    public void excluirFoto(UUID fotoId) {
        GaleriaFoto foto = fotoRepository
                .findDetalhada(fotoId)
                .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada"));
        documentStorage.delete(foto.getStorageKey());
        fotoRepository.delete(foto);
    }

    private boolean albumPermitidoPorLgpd(GaleriaAlbum album, boolean gestao, boolean autorizadoImagem) {
        if (gestao || !album.isExigirConsentimentoImagem()) {
            return true;
        }
        return autorizadoImagem;
    }

    private boolean albumVisivel(GaleriaAlbum album, String audiencia, UUID turmaId) {
        if (audiencia == null || audiencia.isBlank()) {
            return true;
        }
        String visivel = album.getVisivelPara() != null ? album.getVisivelPara().toUpperCase() : "TODOS";
        String aud = audiencia.trim().toUpperCase();
        boolean audienciaOk = "TODOS".equals(visivel) || visivel.contains(aud);
        if (!audienciaOk) {
            return false;
        }
        if (album.getTurma() == null) {
            return true;
        }
        return turmaId != null && album.getTurma().getId().equals(turmaId);
    }

    private Map<String, Object> toAlbumMap(GaleriaAlbum album) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", album.getId());
        map.put("titulo", album.getTitulo());
        map.put("descricao", album.getDescricao());
        map.put("visivelPara", album.getVisivelPara());
        map.put("exigirConsentimentoImagem", album.isExigirConsentimentoImagem());
        map.put("turmaId", album.getTurma() != null ? album.getTurma().getId() : null);
        map.put("turmaNome", album.getTurma() != null ? album.getTurma().getNome() : null);
        map.put("publicadoEm", album.getPublicadoEm());
        map.put("quantidadeFotos", fotoRepository.countByAlbumId(album.getId()));
        if (album.getPublicadoPor() != null && album.getPublicadoPor().getPessoa() != null) {
            map.put("publicadoPorNome", album.getPublicadoPor().getPessoa().getNome());
        }
        return map;
    }

    private Map<String, Object> toFotoMap(GaleriaFoto foto) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", foto.getId());
        map.put("albumId", foto.getAlbum().getId());
        map.put("nomeArquivo", foto.getNomeArquivo());
        map.put("contentType", foto.getContentType());
        map.put("tamanhoBytes", foto.getTamanhoBytes());
        map.put("legenda", foto.getLegenda());
        map.put("ordem", foto.getOrdem());
        map.put("enviadoEm", foto.getEnviadoEm());
        return map;
    }

    private static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "foto.jpg";
        }
        String name = original.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return name.isBlank() ? "foto.jpg" : name;
    }
}
