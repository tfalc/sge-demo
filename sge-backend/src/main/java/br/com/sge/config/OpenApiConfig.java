package br.com.sge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /** Nome do esquema no OpenAPI (Authorization do Swagger UI). */
    public static final String SCHEME_BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI sgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGE API")
                        .description(
                                """
                                Sistema de Gestão Escolar.

                                **Menu *Select a definition* (canto superior direito):** as opções aparecem em português:
                                - **Autenticação** — é o módulo de login (internamente `auth`); use este para **POST /api/auth/login** e obter o JWT.
                                - **Financeiro** — cobranças, PIX, relatórios.
                                - **Acadêmico** — reservado para `/api/academico/**`; **ainda não há rotas**, por isso mostra "No operations defined".

                                **Como obter o JWT para o botão Authorize**
                                1. Selecione **Autenticação** no menu e execute **POST /api/auth/login** (ex.: `admin@sge.com` / `admin123`).
                                2. Na resposta JSON, copie **`data.accessToken`**.
                                3. **Authorize** no topo → cole só o token (sem a palavra `Bearer`).
                                4. Troque o menu para **Financeiro** para testar cobranças etc.

                                O token permanece no navegador após recarregar a página (persist-authorization).""")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                SCHEME_BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                """
                                                Cole aqui o **accessToken** retornado em `POST /api/auth/login`
                                                → campo `data.accessToken` da resposta. Somente o token, sem prefixo Bearer.""")));
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("Autenticação (login / JWT)")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi financeiroGroup() {
        return GroupedOpenApi.builder()
                .group("financeiro")
                .displayName("Financeiro")
                .pathsToMatch("/api/financeiro/**")
                .build();
    }

    @Bean
    public GroupedOpenApi academicoGroup() {
        return GroupedOpenApi.builder()
                .group("academico")
                .displayName("Acadêmico (sem endpoints ainda)")
                .pathsToMatch("/api/academico/**")
                .build();
    }
}
