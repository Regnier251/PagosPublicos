package com.arely.pagospublicos.chatbot.service;

import com.arely.pagospublicos.poliza.repository.TramitePolizaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Component
public class HerramientaTramitesPeriodo implements HerramientaChat {

    private final TramitePolizaRepository repository;

    public HerramientaTramitesPeriodo(TramitePolizaRepository repository) {
        this.repository = repository;
    }

    public String nombre() { return "tramites_periodo"; }

    public String descripcion() {
        return "Cuenta cuántos trámites de póliza se registraron en un mes. Sin parámetros usa el mes actual.";
    }

    public Map<String, Object> esquemaParametros() {
        return Map.of("type", "object",
                "properties", Map.of(
                        "mes", Map.of("type", "integer", "description", "1-12"),
                        "anio", Map.of("type", "integer", "description", "ej. 2026")),
                "required", List.of());
    }

    public String ejecutar(Map<String, Object> args) {
        YearMonth periodo = YearMonth.of(
                args.get("anio") != null ? ((Number) args.get("anio")).intValue() : YearMonth.now().getYear(),
                args.get("mes") != null ? ((Number) args.get("mes")).intValue() : YearMonth.now().getMonthValue());
        long total = repository.countByCreadoEnBetween(
                periodo.atDay(1).atStartOfDay(),
                periodo.atEndOfMonth().atTime(LocalTime.MAX));
        return "Trámites registrados en " + periodo.getMonthValue() + "/" + periodo.getYear() + ": " + total;
    }
}