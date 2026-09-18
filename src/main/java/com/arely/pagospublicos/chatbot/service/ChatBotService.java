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
            Reglas:
            1. Responde SIEMPRE en español, conciso (máximo 4 líneas salvo que pidan detalle).
            2. NUNCA inventes cifras, folios ni nombres: cada dato proviene de una herramienta.
            3. Si la pregunta se refiere a algo ya mencionado (el proveedor, la póliza, el periodo),
               resuelve el referente con el historial y VUELVE A LLAMAR la herramienta con el dato completo.
            4. Preguntas como «¿cuánto pagó?», «¿de cuánto es?», «¿cuánto se le va a pagar?»
               sobre una póliza se responden SIEMPRE ejecutando datos_poliza y reportando
               el neto a pagar — aunque el dato aparezca en la conversación previa.
            5. Si ninguna herramienta aplica, dilo y menciona qué sí puedes consultar
               (trámites por periodo, trámites sin póliza, datos de una póliza,
               proveedores no registrados).

            Ejemplo de comportamiento correcto:
            Usuario: "datos de la póliza del proveedor SH_0101 GO 309 1900773421 1900773424"
            → ejecutas datos_poliza(nombre_proveedor="SH_0101 GO 309 1900773421 1900773424")
            Usuario: "¿y cuánto pagó?"
            → ejecutas OTRA VEZ datos_poliza(nombre_proveedor="SH_0101 GO 309 1900773421 1900773424")
            → respondes: "Neto a pagar: $7,492.99"
            """;

    private final ChatBotClient client;
    private final CatalogoHerramientas catalogo;
    private final ChatRegistroService registroService;   // ← CAMBIO 1: campo nuevo

    public ChatBotService(ChatBotClient client,
                          CatalogoHerramientas catalogo,
                          ChatRegistroService registroService) {  // ← CAMBIO 1: constructor
        this.client = client;
        this.catalogo = catalogo;
        this.registroService = registroService;
    }

    /** CAMBIO 2: tercer parámetro nombreUsuario */
    public String conversar(String pregunta,
                            List<Map<String, Object>> historial,
                            String nombreUsuario) {
        long inicio = System.currentTimeMillis();        // ← CAMBIO 3: medir duración

        List<Map<String, Object>> mensajes = new ArrayList<>();
        mensajes.add(Map.of("role", "system", "content", PROMPT_SISTEMA));
        if (historial != null) {
            mensajes.addAll(historial);
        }
        mensajes.add(Map.of("role", "user", "content", pregunta));

        List<String> herramientasUsadas = new ArrayList<>();   // ← CAMBIO 3: acumulador

        for (int ronda = 0; ronda < MAX_RONDAS; ronda++) {
            RespuestaModelo respuesta = client.enviar(mensajes, catalogo.definiciones());

            if (!respuesta.hayLlamadas()) {
                String texto = respuesta.contenido();
                registrar(inicio, nombreUsuario, pregunta, texto, herramientasUsadas);  // ← CAMBIO 3
                return texto;
            }

            mensajes.add(respuesta.mensajeAsistente());
            for (ChatBotClient.LlamadaHerramienta llamada : respuesta.llamadas()) {
                herramientasUsadas.add(llamada.nombre());      // ← CAMBIO 3: registrar uso
                String resultado = catalogo.ejecutar(llamada.nombre(), llamada.argumentosJson());
                mensajes.add(Map.of(
                        "role", "tool",
                        "tool_call_id", llamada.id(),
                        "content", resultado));
            }
        }

        String texto = "No pude completar la consulta en el número de pasos permitido. "
                + "Intenta una pregunta más simple.";
        registrar(inicio, nombreUsuario, pregunta, texto, herramientasUsadas);  // ← CAMBIO 3
        return texto;
    }

    /** CAMBIO 3: método privado que delega en ChatRegistroService */
    private void registrar(long inicio, String nombreUsuario, String pregunta,
                           String respuesta, List<String> herramientasUsadas) {
        registroService.registrar(
                nombreUsuario,
                pregunta,
                respuesta,
                herramientasUsadas.isEmpty() ? null : String.join(", ", herramientasUsadas),
                System.currentTimeMillis() - inicio);
    }
}