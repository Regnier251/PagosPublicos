package com.arely.pagospublicos.calendario.service;

import com.arely.pagospublicos.calendario.repository.DiaInhabilRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Reglas de días hábiles y cortes del trámite:
 *  - Inhábiles: sábados, domingos y fechas de la tabla dias_inhabiles.
 *  - Corte matutino (llegada ~10am, antes de las 13:00) → SIN sufijo.
 *  - Corte vespertino (1pm–4pm) → sufijo "a".
 * La carpeta del trámite siempre lleva el día HÁBIL SIGUIENTE a la compensación.
 */
@Service
public class CalculadorDiaHabil {

    private static final LocalTime INICIO_CORTE_VESPERTINO = LocalTime.of(13, 0);

    private final DiaInhabilRepository repository;

    public CalculadorDiaHabil(DiaInhabilRepository repository) {
        this.repository = repository;
    }

    public boolean esInhabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return true;
        }
        return repository.existsById(fecha);
    }

    /** El primer día hábil estrictamente posterior a la fecha dada. */
    public LocalDate siguienteHabil(LocalDate fecha) {
        LocalDate candidata = fecha.plusDays(1);
        while (esInhabil(candidata)) {
            candidata = candidata.plusDays(1);
        }
        return candidata;
    }

    /** Sufijo del nombre de carpeta según la hora del corte: "" o "a". */
    public String sufijoCorte(LocalTime hora) {
        return hora.isBefore(INICIO_CORTE_VESPERTINO) ? "" : "a";
    }
}