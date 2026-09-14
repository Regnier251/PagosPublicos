package com.arely.pagospublicos.tercero.repository;

import com.arely.pagospublicos.tercero.model.CuentaBancaria;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

    List<CuentaBancaria> findByTerceroId(Long terceroId, Sort sort);

    boolean existsByClabe(String clabe);

    boolean existsByClabeAndIdNot(String clabe, Long id);

    long countByTerceroId(Long terceroId);
}