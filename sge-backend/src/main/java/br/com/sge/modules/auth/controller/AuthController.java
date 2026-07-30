package br.com.sge.modules.auth.controller;

import br.com.sge.modules.auth.dto.AtualizarPerfilRequest;
import br.com.sge.modules.auth.dto.AuthTokensResponse;
import br.com.sge.modules.auth.dto.EsqueciSenhaResponse;
import br.com.sge.modules.auth.dto.LoginRequest;
import br.com.sge.modules.auth.dto.TrocarSenhaRequest;
import br.com.sge.modules.auth.dto.UserMeResponse;
import br.com.sge.modules.auth.service.AuthService;
import br.com.sge.config.OpenApiConfig;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "Login, refresh token e fluxos de sessão")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Usuario logado", description = "Retorna perfil e responsavelId (quando aplicavel).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        return ResponseEntity.ok(ApiResponse.ok("Usuario atual", authService.me(authentication.getName())));
    }

    @Operation(summary = "Atualizar perfil", description = "Nome, e-mail e telefone do usuario logado.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UserMeResponse>> atualizarPerfil(
            Authentication authentication, @Valid @RequestBody AtualizarPerfilRequest request) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        return ResponseEntity.ok(
                ApiResponse.ok("Perfil atualizado", authService.atualizarPerfil(authentication.getName(), request)));
    }

    @Operation(summary = "Trocar senha")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PutMapping("/senha")
    public ResponseEntity<ApiResponse<Void>> trocarSenha(
            Authentication authentication, @Valid @RequestBody TrocarSenhaRequest request) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        authService.trocarSenha(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Senha alterada", null));
    }

    @Operation(summary = "Login", description = "Autentica por e-mail e senha; retorna access e refresh JWT.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", authService.login(request)));
    }

    @Operation(summary = "Renovar tokens", description = "Emite novo par de tokens a partir do refresh token.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.getOrDefault("refreshToken", "");
        return ResponseEntity.ok(ApiResponse.ok("Token renovado", authService.refresh(refreshToken)));
    }

    @Operation(summary = "Logout", description = "Encerra sessão no cliente (stateless; invalidação opcional no servidor).")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logout realizado", null));
    }

    @Operation(summary = "Esqueci a senha", description = "Solicita recuperação; envia cabeçalho `X-Email` com o e-mail.")
    @PostMapping("/esqueci-senha")
    public ResponseEntity<ApiResponse<EsqueciSenhaResponse>> forgotPassword(@RequestHeader("X-Email") String email) {
        EsqueciSenhaResponse result = authService.esqueciSenha(email);
        return ResponseEntity.ok(ApiResponse.ok("Solicitacao de recuperacao registrada", result));
    }
}
