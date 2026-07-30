package br.com.sge;

import br.com.sge.support.DevH2TestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("dev")
class SgeApplicationTests {

    @DynamicPropertySource
    static void isolatedH2(DynamicPropertyRegistry registry) {
        DevH2TestSupport.registerIsolatedH2(registry);
    }

    @Test
    void contextLoads() {
        // smoke test: garante que o contexto Spring sobe com o profile dev (H2)
    }
}
