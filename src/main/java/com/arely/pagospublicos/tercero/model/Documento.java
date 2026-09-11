package com.arely.pagospublicos.tercero.model;

import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tercero_id", nullable = false)
    private Tercero tercero;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private TipoDocumento tipo;

    @NotBlank(message = "El folio es obligatorio")
    @Size(max = 50, message = "El folio no puede exceder 50 caracteres")
    @Column(name = "folio", nullable = false, length = 50)
    private String folio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @Digits(integer = 16, fraction = 2,
            message = "Máximo 16 dígitos enteros y 2 decimales")
    @Column(name = "monto_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "saldo_pendiente", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoPendiente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusDocumento estatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tercero getTercero() { return tercero; }
    public void setTercero(Tercero tercero) { this.tercero = tercero; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public EstatusDocumento getEstatus() { return estatus; }
    public void setEstatus(EstatusDocumento estatus) { this.estatus = estatus; }
}