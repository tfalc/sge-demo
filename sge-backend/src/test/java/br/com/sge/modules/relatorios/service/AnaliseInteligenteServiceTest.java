package br.com.sge.modules.relatorios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AnaliseInteligenteServiceTest {

    private final AnaliseInteligenteService service = new AnaliseInteligenteService();

    @Test
    void alunoEmRiscoReduzScoreEMarcaCritico() {
        List<Map<String, Object>> notas =
                List.of(Map.of("disciplinaNome", "Matematica", "media", 4.5, "aprovado", false));
        List<Map<String, Object>> freq =
                List.of(Map.of("disciplinaNome", "Matematica", "percentual", 60.0, "aprovado", false));

        Map<String, Object> result =
                service.analisar("Joao", "3A", 5.0, 70.0, 6.0, 75.0, notas, freq);

        assertEquals("EMBUTIDA", result.get("modo"));
        assertTrue((int) result.get("score") < 50);
        assertEquals("CRITICO", result.get("situacao"));
        assertTrue(((List<?>) result.get("pontosAtencao")).size() >= 2);
    }
}
