package br.com.sge.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Portable (standalone) desativa Flyway; este runner aplica a neutralizacao de dados CEM no H2
 * persistido em ./data/ quando o usuario ja tinha uma instalacao anterior.
 */
@Component
@Profile("standalone")
@Order(20)
public class StandaloneDemoDataNormalizer implements ApplicationRunner {

    private final DataSource dataSource;

    public StandaloneDemoDataNormalizer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        var populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
        populator.addScript(new ClassPathResource("standalone-data-normalize.sql"));
        populator.execute(dataSource);
    }
}
