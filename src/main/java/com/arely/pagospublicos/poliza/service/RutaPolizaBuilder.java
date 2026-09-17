package com.arely.pagospublicos.poliza.service;

import com.arely.pagospublicos.calendario.service.CalculadorDiaHabil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;

/**
 * ÚNICO lugar del sistema donde se arma la convención de carpetas:
 *   {raíz}/{año}/{MMyyyy}/{ddMMyyyy}{sufijo}/{persona}/{referencia}/{proveedor}
 * Si la convención de la empresa cambia, se cambia AQUÍ y en ningún otro lado.
 */
@Component
public class RutaPolizaBuilder {

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMyyyy");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final CalculadorDiaHabil calculador;
    private final String directorioRaiz;

    public RutaPolizaBuilder(CalculadorDiaHabil calculador,
                             @Value("${app.polizas.directorio-raiz}") String directorioRaiz) {
        this.calculador = calculador;
        this.directorioRaiz = directorioRaiz;
    }

    public Path raiz() {
        return Path.of(directorioRaiz);
    }

    /**
     * @param fechaCompensacion el día en que se hizo la compensación en SAP
     * @param horaCorte         la hora en que se procesa el trámite
     * @param persona           quien tramita (máximo 60, normalizado)
     * @param referencia        número libre de referencia (ej. "03", "32")
     * @param nombreProveedor   nombre de la carpeta/ZIP que genera SAP (SIN extensión)
     */
    public RutaTramite construir(LocalDate fechaCompensacion, LocalTime horaCorte,
                                 String persona, String referencia, String nombreProveedor) {
        String personaLimpia = normalizar(persona, "persona");
        String referenciaLimpia = normalizar(referencia, "referencia");
        String proveedorLimpio = normalizar(nombreProveedor, "proveedor");

        LocalDate fechaCarpeta = calculador.siguienteHabil(fechaCompensacion);
        String sufijo = calculador.sufijoCorte(horaCorte);
        String carpetaDia = fechaCarpeta.format(FORMATO_DIA) + sufijo;

        // Carpeta de referencia: contenedor del trámite.
        // El ZIP va DIRECTO aquí; la carpeta del proveedor nace al descomprimir (C.3)
        Path carpetaReferencia = Path.of(
                directorioRaiz,
                String.valueOf(fechaCarpeta.getYear()),
                fechaCarpeta.format(FORMATO_MES),
                carpetaDia,
                personaLimpia,
                referenciaLimpia);

        String rutaRelativa = Path.of(
                String.valueOf(fechaCarpeta.getYear()),
                fechaCarpeta.format(FORMATO_MES),
                carpetaDia,
                personaLimpia,
                referenciaLimpia).toString();

        return new RutaTramite(
                carpetaReferencia,
                carpetaReferencia.resolve(proveedorLimpio + ".zip"),
                carpetaReferencia.resolve(proveedorLimpio),
                fechaCarpeta,
                sufijo,
                rutaRelativa);
    }

    /**
     * Windows prohíbe \ / : * ? " < > | en nombres de carpeta.
     * Los sustituimos por "_" y rechazamos vacíos — defensa antes de tocar disco.
     */
    private String normalizar(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El campo «" + campo + "» es obligatorio");
        }
        return texto.strip().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /** Resultado inmutable: las 3 rutas que la Fase C consumirá. */
    public record RutaTramite(
            Path carpetaReferencia,
            Path archivoZip,
            Path carpetaDescomprimida,
            LocalDate fechaCarpeta,
            String sufijoCorte,
            String rutaRelativa) {}
    
    

}