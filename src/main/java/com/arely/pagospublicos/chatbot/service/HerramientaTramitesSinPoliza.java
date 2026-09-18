package com.arely.pagospublicos.chatbot.service;

import com.arely.pagospublicos.poliza.model.TramitePoliza;
import com.arely.pagospublicos.poliza.repository.TramitePolizaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HerramientaTramitesSinPoliza implements HerramientaChat {

    private static final int LIMITE = 20;

    private final TramitePolizaRepository repository;

    public HerramientaTramitesSinPoliza(TramitePolizaRepository repository) {
        this.repository = repository;
    }

    public String nombre() { return "tramites_sin_poliza"; }

    public String descripcion() {
        return "Lista los trámites cuyo ZIP no contenía una póliza detectada. No requiere parámetros.";
    }

    public Map<String, Object> esquemaParametros() {
        return Map.of("type", "object", "properties", Map.of());
    }

    public String ejecutar(Map<String, Object> args) {
        List<TramitePoliza> pendientes = repository.findByArchivoPolizaIsNull();
        if (pendientes.isEmpty()) {
            return "Ninguno: todos los trámites tienen póliza detectada.";
        }
        String detalle = pendientes.stream()
                .limit(LIMITE)
                .map(t -> "- id " + t.getId() + ": " + t.getNombreProveedorSap()
                        + " (" + t.getPersonaTramite() + ", carpeta " + t.getFechaCarpeta() + ")")
                .collect(Collectors.joining("\n"));
        long extras = pendientes.size() - Math.min(pendientes.size(), LIMITE);
        return "Trámites sin póliza detectada: " + pendientes.size() + "\n" + detalle
                + (extras > 0 ? "\n... y " + extras + " más." : "");
    }
}