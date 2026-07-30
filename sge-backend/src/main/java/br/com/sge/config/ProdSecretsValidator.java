package br.com.sge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Em producao, impede subir com secrets padrao do repositorio (OWASP A02 / 12-Factor III).
 */
@Component
@Profile("prod")
public class ProdSecretsValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdSecretsValidator.class);

    private static final String DEFAULT_JWT = "change-this-secret-with-at-least-32-chars";
    private static final String DEFAULT_DB_PASSWORD = "sge123";

    private final Environment environment;

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public ProdSecretsValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.matchesProfiles("prod")) {
            return;
        }
        if (jwtSecret == null || jwtSecret.isBlank() || DEFAULT_JWT.equals(jwtSecret)) {
            throw new IllegalStateException(
                    "Profile prod: defina JWT_SECRET com valor forte (min. 32 caracteres)");
        }
        if (dbPassword == null || dbPassword.isBlank() || DEFAULT_DB_PASSWORD.equals(dbPassword)) {
            throw new IllegalStateException(
                    "Profile prod: defina SPRING_DATASOURCE_PASSWORD (nao use sge123)");
        }
        log.info("[Seguranca] Secrets de producao validados");
    }
}
