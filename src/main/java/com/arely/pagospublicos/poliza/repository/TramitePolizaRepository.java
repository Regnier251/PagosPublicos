package com.arely.pagospublicos.poliza.repository;

import com.arely.pagospublicos.poliza.model.TramitePoliza;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramitePolizaRepository extends JpaRepository<TramitePoliza, Long> {

    boolean existsBySumaVerificacion(String sumaVerificacion);
}