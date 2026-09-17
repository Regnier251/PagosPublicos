package com.arely.pagospublicos.poliza.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Localiza la PÓLIZA dentro de la carpeta descomprimida.
 * Regla del negocio: siempre es PDF, siempre contiene «poliza» y siempre
 * lleva el «02» — sin importar mayúsculas, acentos o separadores
 * (02_Póliza.pdf, 02 POLIZA.pdf, 02-poliza.pdf...).
 * NUNCA se renombra: el usuario manda en el nombre.
 */
@Component
public class DetectorPoliza {

    /** Busca recursivamente y devuelve la ruta de la primera coincidencia. */
    public Optional<Path> buscar(Path carpetaDescomprimida) {
        try (Stream<Path> archivos = Files.walk(carpetaDescomprimida)) {
            return archivos
                    .filter(Files::isRegularFile)
                    .filter(p -> esPoliza(p.getFileName().toString()))
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo explorar la carpeta descomprimida", e);
        }
    }

    /** Público para cuando entren más reglas de reconocimiento (anexos, etc.). */
    public boolean esPoliza(String nombreArchivo) {
        String normalizado = normalizar(nombreArchivo);
        return normalizado.endsWith(".pdf")
                && normalizado.contains("poliza")
                && normalizado.contains("02");
    }

    /** Minúsculas, sin acentos y sin separadores: «02_Póliza» → «02poliza». */
    private String normalizar(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase()
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");
    }
}