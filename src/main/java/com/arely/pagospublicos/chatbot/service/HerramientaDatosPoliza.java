package com.arely.pagospublicos.chatbot.service;

import com.arely.pagospublicos.poliza.model.PolizaDatos;
import com.arely.pagospublicos.poliza.model.TramitePoliza;
import com.arely.pagospublicos.poliza.service.TramitePolizaService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class HerramientaDatosPoliza implements HerramientaChat {

    private final TramitePolizaService tramiteService;

    public HerramientaDatosPoliza(TramitePolizaService tramiteService) {
        this.tramiteService = tramiteService;
    }

    public String nombre() { return "datos_poliza"; }

    public String descripcion() {
        return "Devuelve los datos reconocidos de la póliza de un trámite: folio SAP, montos, "
             + "verificación contable y beneficiario. El nombre del proveedor es UNA SOLA cadena "
             + "que puede contener espacios y números (ejemplo: «SH_0101 GO 309 1900773421 1900773424»). "
             + "Pasa el nombre completo o un fragmento distintivo; NUNCA lo dividas por palabras.";
    }

    public Map<String, Object> esquemaParametros() {
        return Map.of("type", "object",
                "properties", Map.of("nombre_proveedor",
                        Map.of("type", "string", "description", "Nombre exacto de la carpeta/ZIP del proveedor")),
                "required", List.of("nombre_proveedor"));
    }
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(HerramientaDatosPoliza.class);

    public String ejecutar(Map<String, Object> args) {
        String nombre = String.valueOf(args.get("nombre_proveedor")).strip();
        log.info("Chatbot → datos_poliza con nombre_proveedor='{}'", nombre);

        if (nombre.isBlank()) {
            return "Debes indicar el nombre del proveedor.";
        }

        // 1) Coincidencia exacta (ignorando mayúsculas)
        TramitePoliza tramite = tramiteService.buscarPorNombreProveedor(nombre);

        // 2) Si no, por contención — el modelo a veces manda solo un fragmento
        if (tramite == null) {
            List<TramitePoliza> coincidencias = tramiteService.buscarPorFragmentoProveedor(nombre);
            if (coincidencias.size() == 1) {
                tramite = coincidencias.get(0);
            } else if (coincidencias.size() > 1) {
                return "Encontré " + coincidencias.size() + " trámites que coinciden parcialmente:\n"
                     + coincidencias.stream()
                           .map(t -> "- id " + t.getId() + ": " + t.getNombreProveedorSap())
                           .collect(java.util.stream.Collectors.joining("\n"))
                     + "\nEspecifica el nombre completo de uno para ver sus datos.";
            }
        }

        if (tramite == null) {
            return "No existe ningún trámite registrado con el proveedor: " + nombre;
        }

        if (tramite.getArchivoPoliza() == null) {
            return "El trámite existe (id " + tramite.getId() + ") pero no tiene póliza detectada.";
        }
        PolizaDatos d = tramiteService.datosPoliza(tramite);
        return "Trámite id " + tramite.getId() + " — " + tramite.getNombreProveedorSap()
                + "\nFolio documento: " + d.folioDocumento()
                + "\nBruto: " + monto(d.totalDebeHaber())
                + " | Retenciones: " + monto(d.totalRetenciones())
                + " | Neto a pagar: " + monto(d.importeNetoPagar())
                + "\nVerificación contable: " + (d.montosCuadran() ? "CUADRA" : "NO CUADRA")
                + "\nBeneficiario: " + d.cuentaBeneficiario() + " — " + d.nombreBeneficiario()
                + "\nAcreedor: " + d.numeroAcreedor() + " — " + d.nombreAcreedor();
    }

        private String monto(BigDecimal valor) {
        return valor != null
                ? String.format(java.util.Locale.US, "$%,.2f", valor)
                : "no reconocido";
    }
}