package com.arely.pagospublicos.poliza.repository;

import com.arely.pagospublicos.poliza.model.TramitePoliza;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TramitePolizaRepository extends JpaRepository<TramitePoliza, Long> {

    boolean existsBySumaVerificacion(String sumaVerificacion);

    long countByCreadoEnBetween(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);

    List<TramitePoliza> findByArchivoPolizaIsNull();

    List<TramitePoliza> findByNombreProveedorSapContainingIgnoreCase(String fragmento);

    Optional<TramitePoliza> findFirstByNombreProveedorSapIgnoreCaseOrderByIdDesc(String nombre);

}