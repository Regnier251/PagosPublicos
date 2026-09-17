package com.arely.pagospublicos.poliza.controller;

import com.arely.pagospublicos.poliza.service.RutaPolizaBuilder;
import com.arely.pagospublicos.poliza.service.RutaPolizaBuilder.RutaTramite;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
// import java.time.format.DateTimeFormatter;

/**
 * TEMPORAL: existe solo para verificar el armado de rutas en Fase A.
 * Se elimina cuando el flujo real de subida (Fase C) esté en pie.
 */
@RestController
@RequestMapping("/polizas/debug")
public class PolizaDebugController {

    private final RutaPolizaBuilder builder;

    public PolizaDebugController(RutaPolizaBuilder builder) {
        this.builder = builder;
    }

    @GetMapping("/ruta")
    public RutaTramite ruta(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate fecha,
            @RequestParam(defaultValue = "#{T(java.time.LocalTime).now()}") LocalTime hora,
            @RequestParam String persona,
            @RequestParam String referencia,
            @RequestParam String proveedor) {
        return builder.construir(fecha, hora, persona, referencia, proveedor);
    }
}