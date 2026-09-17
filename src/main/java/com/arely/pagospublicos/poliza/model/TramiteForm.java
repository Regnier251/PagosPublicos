package com.arely.pagospublicos.poliza.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/** Datos del formulario de trámite: el ZIP + el contexto. La persona viene de la sesión. */
public class TramiteForm {

    @NotNull(message = "Debes seleccionar o arrastrar el archivo ZIP")
    private MultipartFile archivo;

    @NotBlank(message = "La referencia es obligatoria")
    private String referencia;

    @NotNull(message = "La fecha de compensación es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCompensacion;

    public MultipartFile getArchivo() { return archivo; }
    public void setArchivo(MultipartFile archivo) { this.archivo = archivo; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDate getFechaCompensacion() { return fechaCompensacion; }
    public void setFechaCompensacion(LocalDate fechaCompensacion) { this.fechaCompensacion = fechaCompensacion; }
}