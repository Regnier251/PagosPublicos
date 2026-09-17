package com.arely.pagospublicos.calendario.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "dias_inhabiles")
public class DiaInhabil {

    /** La fecha ES la llave primaria: no hay dos festivos el mismo día. */
    @Id
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}