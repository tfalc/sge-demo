package br.com.sge.config;

import java.util.List;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({JwtConfig.class, CorsConfig.class, SchoolProperties.class, OpenPixProperties.class})
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final boolean spaEnabled;

    public SecurityConfig(
            CorsConfig corsConfig,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.spa.enabled:false}") boolean spaEnabled) {
        this.corsConfig = corsConfig;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.spaEnabled = spaEnabled;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    if (spaEnabled) {
                        auth.requestMatchers(spaGetRequestMatcher()).permitAll();
                    }
                    auth.requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                                .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/school/config")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/school/normativa",
                                "/api/school/normativa/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/school/normativa",
                                "/api/school/normativa/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/esqueci-senha")
                        .permitAll()
                        .requestMatchers("/api/auth/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/financeiro/webhook/pix").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/financeiro/cobrancas/*/simular-pagamento")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/financeiro/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/financeiro/inadimplentes",
                                "/api/financeiro/relatorio-mensal",
                                "/api/financeiro/contratos",
                                "/api/financeiro/planos")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/financeiro/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cadastro/meus-filhos")
                        .hasRole("PAI")
                        .requestMatchers(HttpMethod.POST, "/api/cadastro/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/cadastro/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.PUT, "/api/cadastro/escola")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.PUT, "/api/cadastro/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/cadastro/alunos")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR", "PSICOLOGA")
                        .requestMatchers(HttpMethod.GET, "/api/cadastro/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR")
                        .requestMatchers(HttpMethod.POST, "/api/academico/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.PUT, "/api/academico/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/academico/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/academico/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR", "COORDENADOR", "DIRETOR")
                        .requestMatchers(HttpMethod.POST, "/api/notas/**", "/api/presencas/**", "/api/atas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR")
                        .requestMatchers(HttpMethod.PUT, "/api/notas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR")
                        .requestMatchers(HttpMethod.GET, "/api/notas/**", "/api/presencas/**", "/api/atas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR", "COORDENADOR", "DIRETOR")
                        .requestMatchers(HttpMethod.POST, "/api/ocorrencias/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR")
                        .requestMatchers(HttpMethod.PUT, "/api/ocorrencias/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "COORDENADOR", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/ocorrencias/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR", "COORDENADOR", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/turmas/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARIA",
                                "PROFESSOR",
                                "COORDENADOR",
                                "DIRETOR",
                                "NUTRICIONISTA",
                                "PAI")
                        .requestMatchers("/api/turmas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PROFESSOR")
                        .requestMatchers(HttpMethod.GET, "/api/alunos/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/horarios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.PUT, "/api/horarios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/horarios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/horarios/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/relatorios/boletim/*/gerar-pdf")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/periodos-avaliacao/**")
                        .hasAnyRole(
                                "ADMIN",
                                "SECRETARIA",
                                "PROFESSOR",
                                "COORDENADOR",
                                "DIRETOR",
                                "PAI",
                                "ALUNO")
                        .requestMatchers(HttpMethod.POST, "/api/comunicados/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.PUT, "/api/comunicados/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/comunicados/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/comunicados/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cardapio/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "NUTRICIONISTA")
                        .requestMatchers(HttpMethod.DELETE, "/api/cardapio/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "NUTRICIONISTA")
                        .requestMatchers(HttpMethod.GET, "/api/cardapio/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/agenda/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.PUT, "/api/agenda/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/agenda/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers(HttpMethod.GET, "/api/agenda/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/saude/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "PSICOLOGA", "PAI")
                        .requestMatchers(HttpMethod.GET, "/api/saude/**")
                        .authenticated()
                        .requestMatchers("/api/relatorios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "COORDENADOR", "DIRETOR")
                        .requestMatchers("/api/notificacoes/**")
                        .authenticated()
                        .requestMatchers("/api/rematricula/**")
                        .authenticated()
                        .requestMatchers("/api/matricula-nova/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")
                        .requestMatchers("/api/colegiados/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "COORDENADOR", "DIRETOR", "PROFESSOR")
                        .requestMatchers(HttpMethod.GET, "/api/galeria/**")
                        .hasAnyRole(
                                "PAI",
                                "ALUNO",
                                "PROFESSOR",
                                "COORDENADOR",
                                "ADMIN",
                                "SECRETARIA",
                                "DIRETOR")
                        .requestMatchers(HttpMethod.POST, "/api/galeria/**")
                        .hasAnyRole("PROFESSOR", "COORDENADOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/galeria/**")
                        .hasAnyRole("PROFESSOR", "COORDENADOR", "ADMIN")
                        .anyRequest().authenticated();
                })
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> patterns = corsConfig.effectiveOriginPatterns();
        if (!patterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(patterns);
        } else {
            configuration.setAllowedOrigins(corsConfig.allowedOrigins());
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private static RequestMatcher spaGetRequestMatcher() {
        Predicate<jakarta.servlet.http.HttpServletRequest> predicate = request -> {
            if (!HttpMethod.GET.matches(request.getMethod())) {
                return false;
            }
            String uri = request.getRequestURI();
            return !uri.startsWith("/api/")
                    && !uri.startsWith("/v3/")
                    && !uri.startsWith("/swagger")
                    && !uri.startsWith("/h2-console");
        };
        return predicate::test;
    }
}
