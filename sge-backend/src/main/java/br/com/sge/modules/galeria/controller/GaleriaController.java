package br.com.sge.modules.galeria.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.galeria.dto.CriarGaleriaAlbumRequest;
import br.com.sge.modules.galeria.service.GaleriaService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/galeria")
@Tag(name = "galeria", description = "Galeria de fotos escolar")
public class GaleriaController {

    private final GaleriaService galeriaService;

    public GaleriaController(GaleriaService galeriaService) {
        this.galeriaService = galeriaService;
    }

    @Operation(summary = "Listar albuns da galeria")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PAI', 'ALUNO', 'PROFESSOR', 'COORDENADOR', 'ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/albuns")
    public ResponseEntity<ApiResponse<Object>> listarAlbuns(
            @RequestParam(required = false) String audiencia,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(defaultValue = "false") boolean gestao,
            @RequestParam(defaultValue = "true") boolean autorizadoImagem) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Albuns da galeria",
                        galeriaService.listarAlbuns(audiencia, turmaId, gestao, autorizadoImagem)));
    }

    @Operation(summary = "Detalhe do album com fotos")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PAI', 'ALUNO', 'PROFESSOR', 'COORDENADOR', 'ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/albuns/{id}")
    public ResponseEntity<ApiResponse<Object>> obterAlbum(
            @PathVariable UUID id,
            @RequestParam(required = false) String audiencia,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(defaultValue = "false") boolean gestao,
            @RequestParam(defaultValue = "true") boolean autorizadoImagem) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Album da galeria",
                        galeriaService.obterAlbum(id, audiencia, turmaId, gestao, autorizadoImagem)));
    }

    @Operation(summary = "Criar album")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMIN')")
    @PostMapping("/albuns")
    public ResponseEntity<ApiResponse<Object>> criarAlbum(
            @Valid @RequestBody CriarGaleriaAlbumRequest request, Principal principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Album criado", galeriaService.criarAlbum(request, principal.getName())));
    }

    @Operation(summary = "Upload de foto no album")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMIN')")
    @PostMapping(value = "/albuns/{albumId}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> uploadFoto(
            @PathVariable UUID albumId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String legenda) {
        return ResponseEntity.ok(ApiResponse.ok("Foto enviada", galeriaService.uploadFoto(albumId, file, legenda)));
    }

    @Operation(summary = "Download / visualizacao da foto")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PAI', 'ALUNO', 'PROFESSOR', 'COORDENADOR', 'ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/fotos/{fotoId}/arquivo")
    public ResponseEntity<byte[]> downloadFoto(
            @PathVariable UUID fotoId,
            @RequestParam(required = false) String audiencia,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(defaultValue = "false") boolean gestao,
            @RequestParam(defaultValue = "true") boolean autorizadoImagem) {
        GaleriaService.FotoDownload dl =
                galeriaService.downloadFoto(fotoId, audiencia, turmaId, gestao, autorizadoImagem);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dl.filename() + "\"")
                .contentType(MediaType.parseMediaType(dl.contentType()))
                .body(dl.bytes());
    }

    @Operation(summary = "Excluir album")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMIN')")
    @DeleteMapping("/albuns/{id}")
    public ResponseEntity<ApiResponse<Object>> excluirAlbum(@PathVariable UUID id) {
        galeriaService.excluirAlbum(id);
        return ResponseEntity.ok(ApiResponse.ok("Album excluido", null));
    }

    @Operation(summary = "Excluir foto")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMIN')")
    @DeleteMapping("/fotos/{id}")
    public ResponseEntity<ApiResponse<Object>> excluirFoto(@PathVariable UUID id) {
        galeriaService.excluirFoto(id);
        return ResponseEntity.ok(ApiResponse.ok("Foto excluida", null));
    }
}
