package br.com.sge.modules.rematricula.service;

import br.com.sge.modules.rematricula.dto.FormularioRematriculaDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RematriculaFormularioMapper {

    private final ObjectMapper objectMapper;

    public RematriculaFormularioMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FormularioRematriculaDto parseFormulario(String json) {
        if (json == null || json.isBlank()) {
            return new FormularioRematriculaDto(List.of());
        }
        try {
            return objectMapper.readValue(json, FormularioRematriculaDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Formulario de rematricula invalido");
        }
    }

    public String toJson(FormularioRematriculaDto formulario) {
        try {
            return objectMapper.writeValueAsString(formulario);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Formulario de rematricula invalido");
        }
    }

    public Map<String, Object> parseRespostas(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Respostas de rematricula invalidas");
        }
    }

    public String respostasToJson(Map<String, Object> respostas) {
        try {
            return objectMapper.writeValueAsString(respostas != null ? respostas : Map.of());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Respostas de rematricula invalidas");
        }
    }

    public List<String> parseSugestoes(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    public String sugestoesToJson(List<String> sugestoes) {
        try {
            return objectMapper.writeValueAsString(sugestoes != null ? sugestoes : List.of());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Sugestoes invalidas");
        }
    }
}
