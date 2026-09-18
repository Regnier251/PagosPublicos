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
            Eres un asistente virtual especializado en la consulta y gestión de información del sistema.

            Reglas:
            1. Idioma y Brevedad: Responde siempre en español. Mantén respuestas concisas (máximo 4 líneas) a menos que el usuario solicite explícitamente un desglose detallado.
            2. Veracidad y Fundamento: NUNCA inventes cifras, identificadores, estatus ni nombres. Toda información objetiva debe provenir del resultado de una herramienta.
            3. Resolución de Contexto: Si el usuario hace referencia a un objeto o entidad previa (pronombres, "aquel", "el registro anterior", etc.), resuelve el valor correcto usando el historial y ejecuta la herramienta correspondiente con el parámetro completo.
            4. Revalidación de Datos: Ante preguntas sobre montos, estados o detalles clave de un registro previo, ejecuta NUEVAMENTE la herramienta correspondiente para confirmar el dato antes de responder, sin confiar únicamente en el historial de texto.
            5. Fuera de Alcance: Si la solicitud no se puede resolver con ninguna herramienta disponible, indícalo directamente y menciona cuáles son las consultas o acciones que sí puedes realizar.

            Ejemplo de comportamiento correcto:
            Usuario: "Dame la información del registro [ID_ENTIDAD_123]"
            → Ejecutas: consultar_informacion(id="[ID_ENTIDAD_123]")
            Usuario: "¿Y de cuánto fue el monto total?"
            → Ejecutas NUEVAMENTE: consultar_informacion(id="[ID_ENTIDAD_123]")
            → Respondes: "El monto total es: $X,XXX.XX"
            """;

    private final ChatBotClient client;
    private final CatalogoHerramientas catalogo;
    private final ChatRegistroService registroService;

    public ChatBotService(ChatBotClient client,
                          CatalogoHerramientas catalogo,
                          ChatRegistroService registroService) {
        this.client = client;
        this.catalogo = catalogo;
        this.registroService = registroService;
    }

    /** CAMBIO 2: tercer parámetro nombreUsuario */
    public String conversar(String pregunta,
                            List<Map<String, Object>> historial,
                            String nombreUsuario) {
        long inicio = System.currentTimeMillis();

        List<Map<String, Object>> mensajes = new ArrayList<>();
        mensajes.add(Map.of("role", "system", "content", PROMPT_SISTEMA));
        if (historial != null) {
            mensajes.addAll(historial);
        }
        mensajes.add(Map.of("role", "user", "content", pregunta));

        List<String> herramientasUsadas = new ArrayList<>();

        for (int ronda = 0; ronda < MAX_RONDAS; ronda++) {
            RespuestaModelo respuesta = client.enviar(mensajes, catalogo.definiciones());

            if (!respuesta.hayLlamadas()) {
                String texto = respuesta.contenido();
                registrar(inicio, nombreUsuario, pregunta, texto, herramientasUsadas);
                return texto;
            }

            mensajes.add(respuesta.mensajeAsistente());
            for (ChatBotClient.LlamadaHerramienta llamada : respuesta.llamadas()) {
                herramientasUsadas.add(llamada.nombre()); 
                String resultado = catalogo.ejecutar(llamada.nombre(), llamada.argumentosJson());
                mensajes.add(Map.of(
                        "role", "tool",
                        "tool_call_id", llamada.id(),
                        "content", resultado));
            }
        }

        String texto = "No pude completar la consulta en el número de pasos permitido. "
                + "Intenta una pregunta más simple.";
        registrar(inicio, nombreUsuario, pregunta, texto, herramientasUsadas);
        return texto;
    }

    /** método privado que delega en ChatRegistroService */
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