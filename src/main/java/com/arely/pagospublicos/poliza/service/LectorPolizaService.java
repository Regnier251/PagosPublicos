package com.arely.pagospublicos.poliza.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

/** Abre la póliza (PDF) y extrae su texto. Patrones de negocio vienen después. */
@Service
public class LectorPolizaService {

    public String extraerTexto(Path archivoPoliza) {
        try (PDDocument documento = Loader.loadPDF(archivoPoliza.toFile())) {
            return new PDFTextStripper().getText(documento);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "El PDF de la póliza no se pudo leer (¿dañado o protegido con contraseña?)", e);
        }
    }
}