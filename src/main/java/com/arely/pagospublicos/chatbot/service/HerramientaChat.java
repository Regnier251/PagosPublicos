package com.arely.pagospublicos.chatbot.service;

import java.util.Map;

/** Una consulta segura que el modelo puede ELEGIR ejecutar. Los datos salen de la BD. */
public interface HerramientaChat {

    String nombre();
    String descripcion();
    Map<String, Object> esquemaParametros();
    String ejecutar(Map<String, Object> argumentos);
}