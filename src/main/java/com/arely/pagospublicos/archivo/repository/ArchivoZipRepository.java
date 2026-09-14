package com.arely.pagospublicos.archivo.repository;

import com.arely.pagospublicos.archivo.model.ArchivoZip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivoZipRepository extends JpaRepository<ArchivoZip, Long> {

    boolean existsBySumaVerificacion(String sumaVerificacion);
}