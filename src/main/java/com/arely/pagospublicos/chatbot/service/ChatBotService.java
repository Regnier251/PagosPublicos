package com.arely.pagospublicos.chatbot.service;

import com.arely.pagospublicos.chatbot.client.ChatBotClient;
import com.arely.pagospublicos.chatbot.client.ChatBotClient.RespuestaModelo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatBotService {

    private static final int MAX_RONDAS = 3;

    private static final String PROMPT_SISTEMA = """
            Eres el asistente interno del sistema de trámites de pago «Pagos Públicos».
            Reglas estrictas:
            1. Responde SIEMPRE en español, conciso: máximo 4 líneas salvo que te pidan detalle.
            2. NUNCA inventes cifras, folios ni nombres: cada dato que reportes debe venir
               de una herramienta ejecutada en ESTE turno.
            3. Si la pregunta continúa la conversación, identifica el sujeto del contexto previo y VUELVE A LLAMAR
               la herramienta correspondiente con esos datos (el nombre completo del proveedor,
               el periodo, etc.). El historial te sirve para saber A QUIÉN referirte,
               no para citar números de memoria.
            4. Al reportar una póliza, prioriza: folio, neto a pagar y verificación contable.
            5. Si ninguna herramienta aplica, dilo y menciona qué sí puedes consultar
               (trámites por periodo, trámites sin póliza, datos de una póliza,
               proveedores no registrados).
            """;

    private final ChatBotClient client;
    private final CatalogoHerramientas catalogo;

    public ChatBotService(ChatBotClient client, CatalogoHerramientas catalogo) {
        this.client = client;
        this.catalogo = catalogo;
    }

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ChatBotService.class);

    public String conversar(String pregunta, List<Map<String, Object>> historial) {
        log.info("Chatbot → historial recibido: {} mensajes", historial == null ? 0 : historial.size());

        List<Map<String, Object>> mensajes = new ArrayList<>();
        mensajes.add(Map.of("role", "system", "content", PROMPT_SISTEMA));
        if (historial != null) {
            mensajes.addAll(historial);
        }
        mensajes.add(Map.of("role", "user", "content", pregunta));

        for (int ronda = 0; ronda < MAX_RONDAS; ronda++) {
            RespuestaModelo respuesta = client.enviar(mensajes, catalogo.definiciones());

            if (!respuesta.hayLlamadas()) {
                return respuesta.contenido();
            }

            mensajes.add(respuesta.mensajeAsistente());
            for (ChatBotClient.LlamadaHerramienta llamada : respuesta.llamadas()) {
                String resultado = catalogo.ejecutar(llamada.nombre(), llamada.argumentosJson());
                mensajes.add(Map.of(
                        "role", "tool",
                        "tool_call_id", llamada.id(),
                        "content", resultado));
            }
        }
        return "No pude completar la consulta en el número de pasos permitido. Intenta una pregunta más simple.";
    }
}