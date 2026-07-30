package br.com.sge.modules.notificacoes.service;

import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.notificacoes.dto.NotificacaoResponse;
import br.com.sge.modules.notificacoes.entity.Notificacao;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.repository.NotificacaoRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            UsuarioRepository usuarioRepository,
            AlunoRepository alunoRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public void criar(
            Usuario usuario,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String link,
            UUID referenciaId) {
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            return;
        }
        Notificacao n = new Notificacao();
        n.setUsuario(usuario);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensagem(mensagem);
        n.setLink(link);
        n.setReferenciaId(referenciaId);
        n.setLida(false);
        n.setCriadoEm(Instant.now());
        notificacaoRepository.save(n);
    }

    @Transactional
    public void notificarResponsaveisDoAluno(
            UUID alunoId,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String link,
            UUID referenciaId) {
        alunoRepository.findDetalhadoComResponsaveis(alunoId).ifPresent(aluno -> {
            Set<UUID> enviados = new HashSet<>();
            for (Responsavel responsavel : aluno.getResponsaveis()) {
                Usuario usuario = responsavel.getUsuario();
                if (usuario != null && enviados.add(usuario.getId())) {
                    criar(usuario, tipo, titulo, mensagem, link, referenciaId);
                }
            }
        });
    }

    @Transactional
    public void notificarPorPerfil(
            PerfilUsuario perfil,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String link,
            UUID referenciaId) {
        for (Usuario usuario : usuarioRepository.findByPerfilAndAtivoTrue(perfil)) {
            criar(usuario, tipo, titulo, mensagem, link, referenciaId);
        }
    }

    @Transactional
    public void notificarComunicado(String visivelPara, UUID turmaId, String tituloComunicado, UUID comunicadoId) {
        String titulo = "Novo comunicado";
        String mensagem = tituloComunicado;
        String link = "/pais/comunicacao";
        String audiencia = visivelPara != null ? visivelPara.trim().toUpperCase() : "TODOS";

        if ("PAIS".equals(audiencia)) {
            if (turmaId != null) {
                Set<UUID> enviados = new HashSet<>();
                for (Aluno aluno : alunoRepository.findAtivosByTurmaId(turmaId)) {
                    alunoRepository.findDetalhadoComResponsaveis(aluno.getId()).ifPresent(detalhe -> {
                        for (Responsavel responsavel : detalhe.getResponsaveis()) {
                            Usuario usuario = responsavel.getUsuario();
                            if (usuario != null && enviados.add(usuario.getId())) {
                                criar(usuario, TipoNotificacao.COMUNICADO_NOVO, titulo, mensagem, link, comunicadoId);
                            }
                        }
                    });
                }
            } else {
                notificarPorPerfil(PerfilUsuario.PAI, TipoNotificacao.COMUNICADO_NOVO, titulo, mensagem, link, comunicadoId);
            }
            return;
        }

        for (Usuario usuario : usuarioRepository.findByAtivoTrue()) {
            criar(usuario, TipoNotificacao.COMUNICADO_NOVO, titulo, mensagem, link, comunicadoId);
        }
    }

    @Transactional
    public void notificarGaleria(String visivelPara, UUID turmaId, String tituloAlbum, UUID albumId) {
        String titulo = "Nova galeria de fotos";
        String mensagem = tituloAlbum;
        String link = "/pais/galeria";
        String audiencia = visivelPara != null ? visivelPara.trim().toUpperCase() : "TODOS";

        if (audiencia.contains("PAIS")) {
            if (turmaId != null) {
                Set<UUID> enviados = new HashSet<>();
                for (Aluno aluno : alunoRepository.findAtivosByTurmaId(turmaId)) {
                    alunoRepository.findDetalhadoComResponsaveis(aluno.getId()).ifPresent(detalhe -> {
                        for (Responsavel responsavel : detalhe.getResponsaveis()) {
                            Usuario usuario = responsavel.getUsuario();
                            if (usuario != null && enviados.add(usuario.getId())) {
                                criar(usuario, TipoNotificacao.GALERIA_NOVA, titulo, mensagem, link, albumId);
                            }
                        }
                    });
                }
            } else {
                notificarPorPerfil(PerfilUsuario.PAI, TipoNotificacao.GALERIA_NOVA, titulo, mensagem, link, albumId);
            }
        }

        if (audiencia.contains("ALUNOS") || "TODOS".equals(audiencia)) {
            notificarPorPerfil(PerfilUsuario.ALUNO, TipoNotificacao.GALERIA_NOVA, titulo, mensagem, "/aluno/galeria", albumId);
        }

        if (audiencia.contains("PROFESSORES") || "TODOS".equals(audiencia)) {
            notificarPorPerfil(
                    PerfilUsuario.PROFESSOR, TipoNotificacao.GALERIA_NOVA, titulo, mensagem, "/professor/galeria", albumId);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarParaUsuario(String email) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        return notificacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> resumoParaUsuario(String email) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        long naoLidas = notificacaoRepository.countByUsuarioIdAndLidaFalse(usuario.getId());
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("naoLidas", naoLidas);
        return resumo;
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(UUID id, String email) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        Notificacao notificacao = notificacaoRepository
                .findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notificacao nao encontrada"));
        notificacao.setLida(true);
        return toResponse(notificacaoRepository.save(notificacao));
    }

    @Transactional
    public int marcarTodasComoLidas(String email) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        return notificacaoRepository.marcarTodasLidas(usuario.getId());
    }

    private NotificacaoResponse toResponse(Notificacao n) {
        return new NotificacaoResponse(
                n.getId(),
                n.getTipo().name(),
                n.getTitulo(),
                n.getMensagem(),
                n.getLink(),
                n.getReferenciaId(),
                Boolean.TRUE.equals(n.getLida()),
                n.getCriadoEm().toString());
    }
}
