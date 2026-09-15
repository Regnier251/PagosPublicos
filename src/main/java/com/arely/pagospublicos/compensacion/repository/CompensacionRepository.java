package com.arely.pagospublicos.compensacion.repository;

import com.arely.pagospublicos.compensacion.model.Compensacion;
import com.arely.pagospublicos.compensacion.model.EstatusCompensacion;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompensacionRepository extends JpaRepository<Compensacion, Long> {

    List<Compensacion> findByTerceroId(Long terceroId, Sort sort);

    Optional<Compensacion> findFirstByFolioStartingWithOrderByFolioDesc(String prefijo);

    long countByTerceroId(Long terceroId);

    long countByEstatus(EstatusCompensacion estatus);
}