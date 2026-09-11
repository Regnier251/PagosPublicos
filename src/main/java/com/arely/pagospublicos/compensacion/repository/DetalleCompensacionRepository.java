package com.arely.pagospublicos.compensacion.repository;

import com.arely.pagospublicos.compensacion.model.DetalleCompensacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCompensacionRepository extends JpaRepository<DetalleCompensacion, Long> {

    List<DetalleCompensacion> findByCompensacionId(Long compensacionId);
}