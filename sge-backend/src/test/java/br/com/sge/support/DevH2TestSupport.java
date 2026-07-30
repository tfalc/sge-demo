package br.com.sge.support;

import java.util.UUID;
import org.springframework.test.context.DynamicPropertyRegistry;

/** H2 em memoria isolado por classe de teste (evita conflito de seed). */
public final class DevH2TestSupport {

    private DevH2TestSupport() {}

    public static void registerIsolatedH2(DynamicPropertyRegistry registry) {
        String dbName = "sge_test_" + UUID.randomUUID().toString().replace("-", "");
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    }
}
