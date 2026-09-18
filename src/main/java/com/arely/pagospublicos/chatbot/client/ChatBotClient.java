package com.arely.pagospublicos.chatbot.client;

import com.arely.pagospublicos.chatbot.config.ChatBotProperties;
// import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatBotClient {

    private final ChatBotProperties props;
    private final RestClient restClient;
    // private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatBotClient(ChatBotProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    @SuppressWarnings("unchecked")
    public RespuestaModelo enviar(List<Map<String, Object>> mensajes,
                                  List<Map<String, Object>> herramientas) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("model", props.getModelo());
        cuerpo.put("messages", mensajes);
        cuerpo.put("temperature", 0.2);
        cuerpo.put("max_tokens", 500);
        if (herramientas != null && !herramientas.isEmpty()) {
            cuerpo.put("tools", herramientas);
        }

        Map<String, Object> respuesta = restClient.post()
                .uri("/chat/completions")
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

        Map<String, Object> message = (Map<String, Object>)
                ((List<Map<String, Object>>) respuesta.get("choices")).get(0).get("message");

        List<Map<String, Object>> toolCalls =
                (List<Map<String, Object>>) message.get("tool_calls");

        if (toolCalls == null || toolCalls.isEmpty()) {
            return RespuestaModelo.deTexto((String) message.get("content"));
        }
        List<LlamadaHerramienta> llamadas = toolCalls.stream().map(tc -> {
            Map<String, Object> fn = (Map<String, Object>) tc.get("function");
            return new LlamadaHerramienta(
                    (String) tc.get("id"),
                    (String) fn.get("name"),
                    (String) fn.get("arguments"));
        }).toList();
        return RespuestaModelo.deLlamadas(message, llamadas);
    }

    public record LlamadaHerramienta(String id, String nombre, String argumentosJson) {}

    public record RespuestaModelo(String contenido,
                                  Map<String, Object> mensajeAsistente,
                                  List<LlamadaHerramienta> llamadas) {
        static RespuestaModelo deTexto(String texto) {
            return new RespuestaModelo(texto, null, List.of());
        }
        static RespuestaModelo deLlamadas(Map<String, Object> mensaje, List<LlamadaHerramienta> llamadas) {
            return new RespuestaModelo(null, mensaje, llamadas);
        }
        public boolean hayLlamadas() { return !llamadas.isEmpty(); }
    }
}