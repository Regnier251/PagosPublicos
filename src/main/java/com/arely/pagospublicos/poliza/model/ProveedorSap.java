package com.arely.pagospublicos.poliza.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores_sap")
public class ProveedorSap {

    /** Nombre de carpeta/ZIP que genera SAP — llave de reconocimiento. */
    @NotBlank(message = "El nombre SAP es obligatorio")
    @Size(max = 255, message = "El nombre SAP no puede exceder 255 caracteres")
    @Column(name = "nombre_sap", nullable = false, unique = true)
    private String nombreSap;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 255, message = "La razón social no puede exceder 255 caracteres")
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusProveedor estatus;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public String getNombreSap() { return nombreSap; }
    public void setNombreSap(String nombreSap) { this.nombreSap = nombreSap; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EstatusProveedor getEstatus() { return estatus; }
    public void setEstatus(EstatusProveedor estatus) { this.estatus = estatus; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}