package com.arely.pagospublicos.chatbot.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Traduce las herramientas al formato OpenAI y ejecuta las llamadas del modelo. */
@Component
public class CatalogoHerramientas {

    private final List<HerramientaChat> herramientas;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CatalogoHerramientas(List<HerramientaChat> herramientas) {
        this.herramientas = herramientas;
    }

    public List<Map<String, Object>> definiciones() {
        return herramientas.stream()
                .map(h -> Map.<String, Object>of(
                        "type", "function",
                        "function", Map.of(
                                "name", h.nombre(),
                                "description", h.descripcion(),
                                "parameters", h.esquemaParametros())))
                .toList();
    }

    public String ejecutar(String nombre, String argumentosJson) {
        HerramientaChat herramienta = herramientas.stream()
                .filter(h -> h.nombre().equals(nombre))
                .findFirst()
                .orElse(null);
        if (herramienta == null) {
            return "Herramienta desconocida: " + nombre;
        }
        try {
            Map<String, Object> args = (argumentosJson == null || argumentosJson.isBlank())
                    ? Map.of()
                    : objectMapper.readValue(argumentosJson, new TypeReference<>() {});
            return herramienta.ejecutar(args);
        } catch (Exception e) {
            return "Error ejecutando la herramienta: " + e.getMessage();
        }
    }
}