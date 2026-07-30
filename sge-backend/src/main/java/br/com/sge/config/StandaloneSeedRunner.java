package br.com.sge.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@Profile("standalone")
@Order(10)
public class StandaloneSeedRunner implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public StandaloneSeedRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer escolas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM escola", Integer.class);
        if (escolas != null && escolas > 0) {
            return;
        }

        var populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("dev-data.sql"));
        populator.execute(dataSource);
    }
}
