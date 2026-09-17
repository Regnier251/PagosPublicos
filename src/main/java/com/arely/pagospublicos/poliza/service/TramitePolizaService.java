package com.arely.pagospublicos.poliza.service;

import com.arely.pagospublicos.poliza.model.EstatusTramite;
import com.arely.pagospublicos.poliza.model.PolizaDatos;
import com.arely.pagospublicos.poliza.model.TramiteForm;
import com.arely.pagospublicos.poliza.model.TramitePoliza;
import com.arely.pagospublicos.poliza.repository.TramitePolizaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class TramitePolizaService {

    private final TramitePolizaRepository repository;
    private final RutaPolizaBuilder builder;
    private final DescompresorZip descompresor;
    private final DetectorPoliza detectorPoliza;
    private final LectorPolizaService lectorPoliza;
    private final ParserPoliza parserPoliza;

    public TramitePolizaService(TramitePolizaRepository repository,
                                RutaPolizaBuilder builder,
                                DescompresorZip descompresor,
                                DetectorPoliza detectorPoliza,
                                LectorPolizaService lectorPoliza,
                                ParserPoliza parserPoliza) {
        this.repository = repository;
        this.builder = builder;
        this.descompresor = descompresor;
        this.detectorPoliza = detectorPoliza;
        this.lectorPoliza = lectorPoliza;
        this.parserPoliza = parserPoliza;
    }

        @Transactional
    public TramitePoliza recibir(TramiteForm form) {
        // ── 1. Validaciones del archivo ──
        MultipartFile archivo = form.getArchivo();
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se recibió ningún archivo ZIP");
        }
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Solo se aceptan archivos .zip");
        }
        String nombreProveedor = nombreOriginal
                .substring(0, nombreOriginal.length() - 4).strip();
        if (nombreProveedor.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo no es válido");
        }

        // ── 2. Contenido duplicado ──
        String checksum = calcularSha256(archivo);
        if (repository.existsBySumaVerificacion(checksum)) {
            throw new IllegalArgumentException(
                    "Este archivo ZIP ya fue registrado anteriormente (mismo contenido)");
        }

        // ── 3. Ruta según convención ──
        RutaPolizaBuilder.RutaTramite ruta = builder.construir(
                form.getFechaCompensacion(),
                java.time.LocalTime.now(),
                form.getPersona(),
                form.getReferencia(),
                nombreProveedor);

        // ── 4. Destino ocupado → rechazo ──
        if (Files.exists(ruta.archivoZip()) || Files.exists(ruta.carpetaDescomprimida())) {
            throw new IllegalArgumentException(String.format(
                    "Ya existe un trámite para este proveedor en la ruta destino (%s). "
                    + "Verifica si ya fue procesado en este corte.",
                    ruta.carpetaReferencia()));
        }

        // ── 5–7. Disco: guardar ZIP → descomprimir → detectar póliza ──
        Optional<Path> polizaDetectada;
        try {
            Files.createDirectories(ruta.carpetaReferencia());
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, ruta.archivoZip());
            }
            descompresor.extraer(ruta.archivoZip(), ruta.carpetaDescomprimida());
            polizaDetectada = detectorPoliza.buscar(ruta.carpetaDescomprimida());
        } catch (IOException e) {
            limpiarTramiteFallido(ruta);
            throw new IllegalStateException("No se pudo crear el trámite en disco", e);
        } catch (RuntimeException e) {
            limpiarTramiteFallido(ruta);
            throw e; // ZIP dañado, vacío, zip-slip... mensaje original al usuario
        }

        // ── 8. Registrar en BD ──
        TramitePoliza tramite = new TramitePoliza();
        tramite.setNombreProveedorSap(nombreProveedor);
        tramite.setPersonaTramite(form.getPersona().strip());
        tramite.setReferencia(form.getReferencia().strip());
        tramite.setFechaCompensacion(form.getFechaCompensacion());
        tramite.setFechaCarpeta(ruta.fechaCarpeta());
        tramite.setSufijoCorte(ruta.sufijoCorte());
        tramite.setRutaRelativa(ruta.rutaRelativa());
        tramite.setSumaVerificacion(checksum);
        tramite.setArchivoPoliza(polizaDetectada
                .map(p -> ruta.carpetaDescomprimida().relativize(p).toString())
                .orElse(null));
        tramite.setEstatus(EstatusTramite.RECIBIDO);
        return repository.save(tramite);
    }

    /**
     * Limpieza tras fallo: se elimina el ZIP y la descompresión parcial.
     * ⚠ NUNCA se borra la carpeta de referencia: puede contener trámites
     * de otros proveedores del mismo corte.
     */
    private void limpiarTramiteFallido(RutaPolizaBuilder.RutaTramite ruta) {
        UtilArchivos.eliminarSilenciosamente(ruta.carpetaDescomprimida());
        UtilArchivos.eliminarSilenciosamente(ruta.archivoZip());
    }

    public java.util.List<TramitePoliza> listarRecientes() {
        return repository.findAll(
                org.springframework.data.domain.Sort.by("creadoEn").descending());
    }

    public TramitePoliza buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trámite no encontrado: " + id));
    }

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

    public Path rutaAbsolutaZip(TramitePoliza tramite) {
        return builder.raiz().resolve(tramite.getRutaRelativa())
                .resolve(tramite.getNombreProveedorSap() + ".zip");
    }

    public Path rutaAbsolutaPoliza(TramitePoliza tramite) {
        if (tramite.getArchivoPoliza() == null) {
            throw new IllegalArgumentException("Este trámite no tiene póliza detectada");
        }
        return builder.raiz().resolve(tramite.getRutaRelativa())
                .resolve(tramite.getNombreProveedorSap())
                .resolve(tramite.getArchivoPoliza());
    }

    public String textoPoliza(TramitePoliza tramite) {
        return lectorPoliza.extraerTexto(rutaAbsolutaPoliza(tramite));
    }

    public PolizaDatos datosPoliza(TramitePoliza tramite) {
        return parserPoliza.parsear(lectorPoliza.extraerTexto(rutaAbsolutaPoliza(tramite)));
    }
}