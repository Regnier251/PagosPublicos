package com.arely.pagospublicos.archivo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity 
@Table(name = "archivos_zip")
public class ArchivoZip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre con el que subió el usuario (solo informativo, nunca se usa para guardar en disco) */
    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    /** Ruta relativa dentro del directorio raíz (ej. "2026/00042.zip") */
    @Column(name = "ruta_almacenada", nullable = false)
    private String rutaAlmacenada;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    /** SHA-256 en hexadecimal (64 caracteres) — identidad del contenido */
    @Column(name = "suma_verificacion", nullable = false, unique = true, length = 64)
    private String sumaVerificacion;

    @Column(name = "total_archivos_internos", nullable = false)
    private Integer totalArchivosInternos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusArchivoZip estatus;

    @CreationTimestamp
    @Column(name = "subido_en", nullable = false, updatable = false)
    private LocalDateTime subidoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public String getRutaAlmacenada() { return rutaAlmacenada; }
    public void setRutaAlmacenada(String rutaAlmacenada) { this.rutaAlmacenada = rutaAlmacenada; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getSumaVerificacion() { return sumaVerificacion; }
    public void setSumaVerificacion(String sumaVerificacion) { this.sumaVerificacion = sumaVerificacion; }

    public Integer getTotalArchivosInternos() { return totalArchivosInternos; }
    public void setTotalArchivosInternos(Integer totalArchivosInternos) { this.totalArchivosInternos = totalArchivosInternos; }

    public EstatusArchivoZip getEstatus() { return estatus; }
    public void setEstatus(EstatusArchivoZip estatus) { this.estatus = estatus; }

    public LocalDateTime getSubidoEn() { return subidoEn; }
}