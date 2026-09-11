package com.arely.pagospublicos.compensacion.model;

import com.arely.pagospublicos.tercero.model.Documento;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compensacion")
public class DetalleCompensacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "compensacion_id", nullable = false)
    private Compensacion compensacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Column(name = "monto_aplicado", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoAplicado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Compensacion getCompensacion() { return compensacion; }
    public void setCompensacion(Compensacion compensacion) { this.compensacion = compensacion; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public BigDecimal getMontoAplicado() { return montoAplicado; }
    public void setMontoAplicado(BigDecimal montoAplicado) { this.montoAplicado = montoAplicado; }
}