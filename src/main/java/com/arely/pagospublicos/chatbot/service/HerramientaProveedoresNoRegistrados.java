package com.arely.pagospublicos.chatbot.service;

import com.arely.pagospublicos.poliza.repository.ProveedorSapRepository;
import com.arely.pagospublicos.poliza.repository.TramitePolizaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HerramientaProveedoresNoRegistrados implements HerramientaChat {

    private final TramitePolizaRepository tramiteRepository;
    private final ProveedorSapRepository proveedorRepository;

    public HerramientaProveedoresNoRegistrados(TramitePolizaRepository tramiteRepository,
                                               ProveedorSapRepository proveedorRepository) {
        this.tramiteRepository = tramiteRepository;
        this.proveedorRepository = proveedorRepository;
    }

    public String nombre() { return "proveedores_no_registrados"; }

    public String descripcion() {
        return "Lista los proveedores con trámites cuyo nombre no figura en el catálogo SAP. "
                + "No requiere parámetros.";
    }

    public Map<String, Object> esquemaParametros() {
        return Map.of("type", "object", "properties", Map.of());
    }

    public String ejecutar(Map<String, Object> args) {
        List<String> noRegistrados = tramiteRepository.findAll().stream()
                .map(t -> t.getNombreProveedorSap())
                .distinct()
                .filter(n -> !proveedorRepository.existsByNombreSapIgnoreCase(n))
                .toList();
        if (noRegistrados.isEmpty()) {
            return "Todos los proveedores con trámites están registrados en el catálogo.";
        }
        return "Proveedores con trámites NO registrados en el catálogo:\n"
                + noRegistrados.stream().map(n -> "- " + n).collect(Collectors.joining("\n"));
    }
}