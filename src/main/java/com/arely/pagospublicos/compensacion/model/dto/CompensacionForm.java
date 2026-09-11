package com.arely.pagospublicos.compensacion.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CompensacionForm {

    private List<Linea> lineas = new ArrayList<>();
    private Long cuentaBancariaId;
    private BigDecimal saldoResidual;
    private String observaciones;

    /** Crea el form con N líneas vacías, una por documento vigente a mostrar */
    public static CompensacionForm conLineas(int cantidad) {
        CompensacionForm form = new CompensacionForm();
        for (int i = 0; i < cantidad; i++) {
            form.lineas.add(new Linea());
        }
        return form;
    }

    /** Solo interesan las líneas donde el usuario capturó un monto */
    public List<Linea> lineasConMonto() {
        return lineas.stream().filter(l -> l.getMonto() != null).toList();
    }

    public List<Linea> getLineas() { return lineas; }
    public void setLineas(List<Linea> lineas) { this.lineas = lineas; }

    public Long getCuentaBancariaId() { return cuentaBancariaId; }
    public void setCuentaBancariaId(Long cuentaBancariaId) { this.cuentaBancariaId = cuentaBancariaId; }

    public BigDecimal getSaldoResidual() { return saldoResidual; }
    public void setSaldoResidual(BigDecimal saldoResidual) { this.saldoResidual = saldoResidual; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public static class Linea {
        private Long documentoId;
        private BigDecimal monto;

        public Long getDocumentoId() { return documentoId; }
        public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }
    }
}