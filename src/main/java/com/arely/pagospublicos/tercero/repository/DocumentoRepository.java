package com.arely.pagospublicos.tercero.repository;

import com.arely.pagospublicos.tercero.model.Documento;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByTerceroId(Long terceroId, Sort sort);
}