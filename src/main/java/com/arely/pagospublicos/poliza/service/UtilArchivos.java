package com.arely.pagospublicos.poliza.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/** Operaciones de archivos con tolerancia a fallos, para limpieza tras errores. */
public final class UtilArchivos {

    private UtilArchivos() {}

    /** Elimina un archivo o carpeta con todo su contenido. */
    public static void eliminarRecursivamente(Path ruta) {
        if (!Files.exists(ruta)) {
            return;
        }
        try (Stream<Path> caminos = Files.walk(ruta)) {
            caminos.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    /** Elimina sin propagar errores — solo para limpieza tras un fallo. */
    public static void eliminarSilenciosamente(Path ruta) {
        try {
            eliminarRecursivamente(ruta);
        } catch (RuntimeException ignored) {
        }
    }
}