package com.arely.pagospublicos.poliza.repository;

import com.arely.pagospublicos.poliza.model.ProveedorSap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorSapRepository extends JpaRepository<ProveedorSap, Long> {

    boolean existsByNombreSapIgnoreCase(String nombreSap);

    boolean existsByNombreSapIgnoreCaseAndIdNot(String nombreSap, Long id);
}