package com.arely.pagospublicos.chatbot.service;

import com.arely.pagospublicos.chatbot.model.ChatConsulta;
import com.arely.pagospublicos.chatbot.repository.ChatConsultaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRegistroService {

    private static final Logger log = LoggerFactory.getLogger(ChatRegistroService.class);

    private final ChatConsultaRepository repository;

    public ChatRegistroService(ChatConsultaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrar(String nombreUsuario, String pregunta, String respuesta,
                          String herramientasUsadas, long duracionMs) {
        try {
            ChatConsulta consulta = new ChatConsulta();
            consulta.setNombreUsuario(nombreUsuario);
            consulta.setPregunta(pregunta);
            consulta.setRespuesta(respuesta);
            consulta.setHerramientasUsadas(herramientasUsadas);
            consulta.setDuracionMs(duracionMs);
            repository.save(consulta);
        } catch (Exception e) {
            // El registro NO rompe el servicio: se reporta para investigación manual
            log.error("No se pudo registrar la consulta del chat de '{}': {}",
                    nombreUsuario, e.getMessage());
        }
    }
}