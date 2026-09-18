package com.arely.pagospublicos.chatbot.controller;

import com.arely.pagospublicos.chatbot.service.ChatBotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
public class ChatBotController {

    /** Contexto máximo que viaja al modelo en cada pregunta (control de tokens) */
    private static final int MAX_MENSAJES_CONTEXTO = 8;

    private final ChatBotService service;

    public ChatBotController(ChatBotService service) {
        this.service = service;
    }

    @GetMapping("/consultar")
    public String consultar(@RequestParam String pregunta, HttpSession session) {
        Object historialSesion = session.getAttribute("chat_historial");
        List<Map<String, Object>> historial = new ArrayList<>();
        if (historialSesion instanceof List<?> lista) {
            for (Object elemento : lista) {
                if (elemento instanceof Map<?, ?> mapa) {
                    Map<String, Object> mensaje = new HashMap<>();
                    for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                        if (entrada.getKey() instanceof String clave) {
                            mensaje.put(clave, entrada.getValue());
                        }
                    }
                    historial.add(mensaje);
                }
            }
        }

        String respuesta = service.conversar(pregunta, historial);

        historial.add(Map.of("role", "user", "content", pregunta));
        historial.add(Map.of("role", "assistant", "content", respuesta));
        while (historial.size() > MAX_MENSAJES_CONTEXTO) {
            historial.remove(0);
        }
        session.setAttribute("chat_historial", historial);
        return respuesta;
    }
}