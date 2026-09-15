package com.arely.pagospublicos.archivo.repository;

import com.arely.pagospublicos.archivo.model.ArchivoZip;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ArchivoZipRepository
        extends JpaRepository<ArchivoZip, Long>, JpaSpecificationExecutor<ArchivoZip> {

    boolean existsBySumaVerificacion(String sumaVerificacion);

    List<ArchivoZip> findBySubidoEnBetween(LocalDateTime desde, LocalDateTime hasta, Sort sort);

    long countBySubidoEnBetween(LocalDateTime desde, LocalDateTime hasta);
}