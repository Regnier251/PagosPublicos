package com.arely.pagospublicos.compensacion.model;

import com.arely.pagospublicos.tercero.model.CuentaBancaria;
import com.arely.pagospublicos.tercero.model.Tercero;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "compensaciones")
public class Compensacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tercero_id", nullable = false)
    private Tercero tercero;

    @ManyToOne
    @JoinColumn(name = "cuenta_bancaria_id")
    private CuentaBancaria cuentaBancaria;

    @Column(name = "monto_compensado", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoCompensado;

    @Column(name = "saldo_residual", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoResidual;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatusCompensacion estatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public Tercero getTercero() { return tercero; }
    public void setTercero(Tercero tercero) { this.tercero = tercero; }

    public CuentaBancaria getCuentaBancaria() { return cuentaBancaria; }
    public void setCuentaBancaria(CuentaBancaria cuentaBancaria) { this.cuentaBancaria = cuentaBancaria; }

    public BigDecimal getMontoCompensado() { return montoCompensado; }
    public void setMontoCompensado(BigDecimal montoCompensado) { this.montoCompensado = montoCompensado; }

    public BigDecimal getSaldoResidual() { return saldoResidual; }
    public void setSaldoResidual(BigDecimal saldoResidual) { this.saldoResidual = saldoResidual; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public EstatusCompensacion getEstatus() { return estatus; }
    public void setEstatus(EstatusCompensacion estatus) { this.estatus = estatus; }
}