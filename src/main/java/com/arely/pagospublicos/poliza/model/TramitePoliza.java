package com.arely.pagospublicos.poliza.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramites_poliza")
public class TramitePoliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre exacto de la carpeta/ZIP generado por SAP (sin .zip) */
    @Column(name = "nombre_proveedor_sap", nullable = false, length = 255)
    private String nombreProveedorSap;

    @Column(name = "persona_tramite", nullable = false)
    private String personaTramite;

    @Column(name = "referencia", nullable = false, length = 50)
    private String referencia;

    @Column(name = "fecha_compensacion", nullable = false)
    private LocalDate fechaCompensacion;

    /** Día hábil que da nombre a la carpeta (derivado, guardado para listar) */
    @Column(name = "fecha_carpeta", nullable = false)
    private LocalDate fechaCarpeta;

    /** "" (matutino) o "a" (vespertino) */
    @Column(name = "sufijo_corte", nullable = false, length = 1)
    private String sufijoCorte;

    /** Ruta relativa desde la raíz configurada (ej. 2026/092026/18092026a/Alejandro/03/SH_0101...) */
    @Column(name = "ruta_relativa", nullable = false, length = 1000)
    private String rutaRelativa;

    /** SHA-256 del ZIP — identidad del contenido, anti-duplicados */
    @Column(name = "suma_verificacion", nullable = false, unique = true, length = 64)
    private String sumaVerificacion;

    /** Ruta relativa (dentro de la carpeta descomprimida) de la póliza detectada; null si no se encontró */
    @Column(name = "archivo_poliza", length = 500)
    private String archivoPoliza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusTramite estatus;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreProveedorSap() { return nombreProveedorSap; }
    public void setNombreProveedorSap(String nombreProveedorSap) { this.nombreProveedorSap = nombreProveedorSap; }

    public String getPersonaTramite() { return personaTramite; }
    public void setPersonaTramite(String personaTramite) { this.personaTramite = personaTramite; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDate getFechaCompensacion() { return fechaCompensacion; }
    public void setFechaCompensacion(LocalDate fechaCompensacion) { this.fechaCompensacion = fechaCompensacion; }

    public LocalDate getFechaCarpeta() { return fechaCarpeta; }
    public void setFechaCarpeta(LocalDate fechaCarpeta) { this.fechaCarpeta = fechaCarpeta; }

    public String getSufijoCorte() { return sufijoCorte; }
    public void setSufijoCorte(String sufijoCorte) { this.sufijoCorte = sufijoCorte; }

    public String getRutaRelativa() { return rutaRelativa; }
    public void setRutaRelativa(String rutaRelativa) { this.rutaRelativa = rutaRelativa; }

    public String getSumaVerificacion() { return sumaVerificacion; }
    public void setSumaVerificacion(String sumaVerificacion) { this.sumaVerificacion = sumaVerificacion; }

    public EstatusTramite getEstatus() { return estatus; }
    public void setEstatus(EstatusTramite estatus) { this.estatus = estatus; }

    public LocalDateTime getCreadoEn() { return creadoEn; }

    public String getArchivoPoliza() { return archivoPoliza; }
    public void setArchivoPoliza(String archivoPoliza) { this.archivoPoliza = archivoPoliza; }
}