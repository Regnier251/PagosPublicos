package com.arely.pagospublicos.poliza.service;

import com.arely.pagospublicos.poliza.model.PolizaDatos;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reconoce los datos del negocio en el texto crudo del reporte
 * «REPORTE DE DOCUMENTOS CONTABLES PARA PAGO» (PAE-400 de SAP).
 * Los patrones se escribieron contra una póliza real; si SAP cambia
 * el formato o aparece una variante, los ajustes viven SOLO aquí.
 */
@Component
public class ParserPoliza {

    private static final Pattern IMPORTE_NETO  = Pattern.compile("importe de \\$([\\d,]+\\.\\d{2})");
    private static final Pattern FOLIO         = Pattern.compile("No\\. Documento (\\d+)");
    private static final Pattern DEBE_HABER    = Pattern.compile("Debe/haber\\s+([\\d,]+\\.\\d{2})");
    private static final Pattern RETENCION     = Pattern.compile("RETENCION\\s+([\\d,]+\\.\\d{2})");
    private static final Pattern NUM_ACREEDOR  = Pattern.compile("Numero del Acreedor:\\s*(\\S+)");
    private static final Pattern NOM_ACREEDOR  = Pattern.compile("Nombre del Acreedor:\\s*(.+)");
    private static final Pattern FECHA_CONTAB  = Pattern.compile("Fecha contab\\.\\s*([\\d.]+)");
    // CLABE/beneficiario: 18 dígitos exactos (no parte de un número mayor) + nombre en mayúsculas
    private static final Pattern BENEFICIARIO  = Pattern.compile(
            "(?<!\\d)(\\d{18})(?!\\d)\\s+([A-ZÁÉÍÓÚÜÑ][A-ZÁÉÍÓÚÜÑ ]+)");
    private static final Pattern REFERENCIA    = Pattern.compile("Referencia:\\s*(\\S+)");
    private static final Pattern TEXTO_CAB     = Pattern.compile("Texto Cabecera:\\s*(.+)");
    private static final Pattern TEXTO_EXPL    = Pattern.compile("Texto Explicativo:\\s*(.+)");

    public PolizaDatos parsear(String texto) {
        BigDecimal bruto = importe(texto, DEBE_HABER);
        BigDecimal neto = importe(texto, IMPORTE_NETO);
        BigDecimal retenciones = sumaRetenciones(texto);

        // Verificación contable: bruto − retenciones = neto (compareTo, jamás equals)
        boolean cuadran = bruto != null && neto != null
                && bruto.subtract(retenciones).compareTo(neto) == 0;

        String cabecera = primerGrupo(texto, TEXTO_CAB);
        if (cabecera != null) {
            // La línea del PDF trae el texto y, pegado a la derecha, el Debe/haber
            cabecera = cabecera.split("Debe/haber")[0].strip();
        }

        return new PolizaDatos(
                primerGrupo(texto, FOLIO),
                bruto,
                neto,
                retenciones,
                primerGrupo(texto, NUM_ACREEDOR),
                recortar(primerGrupo(texto, NOM_ACREEDOR)),
                primerGrupo(texto, FECHA_CONTAB),
                beneficiario(texto)[0],
                beneficiario(texto)[1],
                primerGrupo(texto, REFERENCIA),
                cabecera,
                recortar(primerGrupo(texto, TEXTO_EXPL)),
                cuadran);
    }

    private BigDecimal importe(String texto, Pattern patron) {
        Matcher m = patron.matcher(texto);
        return m.find() ? monto(m.group(1)) : null;
    }

    private BigDecimal sumaRetenciones(String texto) {
        Matcher m = RETENCION.matcher(texto);
        BigDecimal suma = BigDecimal.ZERO;
        while (m.find()) {
            suma = suma.add(monto(m.group(1)));
        }
        return suma;
    }

    private String[] beneficiario(String texto) {
        Matcher m = BENEFICIARIO.matcher(texto);
        return m.find()
                ? new String[]{m.group(1), m.group(2).strip()}
                : new String[]{null, null};
    }

    private String primerGrupo(String texto, Pattern patron) {
        Matcher m = patron.matcher(texto);
        return m.find() ? m.group(1).strip() : null;
    }

    private BigDecimal monto(String conComas) {
        return new BigDecimal(conComas.replace(",", ""));
    }

    private String recortar(String texto) {
        return texto == null ? null : texto.strip();
    }
}