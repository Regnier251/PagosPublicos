package com.arely.pagospublicos.poliza.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Extrae el contenido de un ZIP en la carpeta destino con dos garantías:
 *  1. ZIP-slip: ninguna entrada puede resolver FUERA de la carpeta destino
 *     (normalize + startsWith — una entrada «..\..\evilito.txt» es rechazada).
 *  2. Aplanado: si el ZIP trae una única carpeta raíz (fue creado comprimiendo
 *     LA CARPETA del proveedor), su contenido sube un nivel — el resultado
 *     es idéntico al acomodo manual, venga el ZIP como venga.
 */
@Component
public class DescompresorZip {

    /** Devuelve la cantidad de archivos extraídos. */
    public int extraer(Path archivoZip, Path carpetaDestino) {
        Path base = carpetaDestino.toAbsolutePath().normalize();
        int extraidos = 0;
        try (ZipFile zip = new ZipFile(archivoZip.toFile())) {
            List<? extends ZipEntry> entradas = zip.stream().toList();
            for (ZipEntry entrada : entradas) {
                if (entrada.isDirectory()) {
                    continue;
                }
                Path destino = base.resolve(entrada.getName()).normalize();
                if (!destino.startsWith(base)) {
                    throw new IllegalArgumentException(
                            "El ZIP intenta escribir fuera de su carpeta destino (entrada rechazada): "
                            + entrada.getName());
                }
                Files.createDirectories(destino.getParent());
                try (InputStream in = zip.getInputStream(entrada)) {
                    Files.copy(in, destino);
                }
                extraidos++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("El ZIP está dañado y no se pudo descomprimir", e);
        }
        if (extraidos == 0) {
            throw new IllegalArgumentException("El ZIP está vacío (no contiene archivos)");
        }
        aplanarCarpetaRaizUnica(base);
        return extraidos;
    }

    private void aplanarCarpetaRaizUnica(Path destino) {
        try (Stream<Path> hijos = Files.list(destino)) {
            List<Path> raices = hijos.toList();
            boolean unicaCarpetaSinSuelto = raices.size() == 1 && Files.isDirectory(raices.get(0));
            if (!unicaCarpetaSinSuelto) {
                return;
            }
            Path carpetaRaiz = raices.get(0);
            try (Stream<Path> contenido = Files.walk(carpetaRaiz)) {
                for (Path ruta : contenido.filter(Files::isRegularFile).toList()) {
                    Path destinoFinal = destino.resolve(carpetaRaiz.relativize(ruta).toString());
                    Files.createDirectories(destinoFinal.getParent());
                    Files.move(ruta, destinoFinal);
                }
            }
            UtilArchivos.eliminarRecursivamente(carpetaRaiz); // ya vacía
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo acomodar el contenido del ZIP", e);
        }
    }
}