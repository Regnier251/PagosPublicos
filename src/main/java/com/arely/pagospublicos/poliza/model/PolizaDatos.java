package com.arely.pagospublicos.poliza.model;

import java.math.BigDecimal;

/** Datos reconocidos en el texto de una póliza (reporte PAE-400 de SAP). */
public record PolizaDatos(
        String folioDocumento,
        BigDecimal totalDebeHaber,
        BigDecimal importeNetoPagar,
        BigDecimal totalRetenciones,
        String numeroAcreedor,
        String nombreAcreedor,
        String fechaContable,
        String cuentaBeneficiario,
        String nombreBeneficiario,
        String referencia,
        String textoCabecera,
        String textoExplicativo,
        boolean montosCuadran) {}