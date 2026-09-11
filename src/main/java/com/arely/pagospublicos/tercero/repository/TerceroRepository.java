package com.arely.pagospublicos.tercero.repository;

import com.arely.pagospublicos.tercero.model.Tercero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerceroRepository extends JpaRepository<Tercero, Long> {

    boolean existsByRfc(String rfc);

    boolean existsByRfcAndIdNot(String rfc, Long id);
}