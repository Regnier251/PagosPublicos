package com.arely.pagospublicos.archivo.service;

import com.arely.pagospublicos.archivo.model.ArchivoZip;
import com.arely.pagospublicos.archivo.model.EstatusArchivoZip;
import com.arely.pagospublicos.archivo.repository.ArchivoZipRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipFile;

@Service
public class ArchivoZipService {

    private final ArchivoZipRepository repository;
    private final String directorioRaiz;

    public ArchivoZipService(ArchivoZipRepository repository,
                             @Value("${app.archivos.directorio-raiz}") String directorioRaiz) {
        this.repository = repository;
        this.directorioRaiz = directorioRaiz;
    }

    public List<ArchivoZip> listar() {
        return repository.findAll(Sort.by("subidoEn").descending());
    }

    public ArchivoZip buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado: " + id));
    }

    /** Ruta absoluta en disco de un archivo guardado (para lectura/descarga futura). */
    public Path rutaAbsoluta(ArchivoZip archivo) {
        return Path.of(directorioRaiz, archivo.getRutaAlmacenada());
    }

    @Transactional
    public ArchivoZip subir(MultipartFile archivo) {
        // ── 1. Validaciones básicas ──
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se recibió ningún archivo");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Solo se aceptan archivos .zip");
        }

        // ── 2. Huella SHA-256: identidad del contenido ──
        String checksum = calcularSha256(archivo);
        if (repository.existsBySumaVerificacion(checksum)) {
            throw new IllegalArgumentException(
                    "Este archivo ya fue subido anteriormente (mismo contenido)");
        }

        // ── 3. Ruta técnica determinista: {año}/{checksum}.zip ──
        String rutaRelativa = LocalDate.now().getYear() + "/" + checksum + ".zip";
        Path destino = Path.of(directorioRaiz, rutaRelativa);

        // ── 4. Guardar en disco ──
        try {
            Files.createDirectories(destino.getParent());
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, destino);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo en disco", e);
        }

        // ── 5. Validar integridad real: apertura completa del ZIP ──
        int totalInternos;
        try (ZipFile zip = new ZipFile(destino.toFile())) {
            totalInternos = (int) zip.stream().filter(entrada -> !entrada.isDirectory()).count();
        } catch (IOException e) {
            eliminarArchivoSilenciosamente(destino);
            throw new IllegalArgumentException(
                    "El archivo no es un ZIP válido o está dañado");
        }
        if (totalInternos == 0) {
            eliminarArchivoSilenciosamente(destino);
            throw new IllegalArgumentException("El ZIP está vacío (no contiene archivos)");
        }

        // ── 6. UN solo insert, con la fila completa ──
        ArchivoZip entidad = new ArchivoZip();
        entidad.setNombreOriginal(nombre);
        entidad.setRutaAlmacenada(rutaRelativa);
        entidad.setTamanoBytes(archivo.getSize());
        entidad.setSumaVerificacion(checksum);
        entidad.setTotalArchivosInternos(totalInternos);
        entidad.setEstatus(EstatusArchivoZip.RECIBIDO);
        return repository.save(entidad);
    }

    // ── helpers ──

    private String calcularSha256(MultipartFile archivo) {
        try (InputStream in = archivo.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                md.update(buffer, 0, leidos);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular la huella del archivo", e);
        }
    }

    private void eliminarArchivoSilenciosamente(Path ruta) {
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            // No enmascara el error de negocio original
        }
    }
}