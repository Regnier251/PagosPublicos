package com.arely.pagospublicos.calendario.repository;

import com.arely.pagospublicos.calendario.model.DiaInhabil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DiaInhabilRepository extends JpaRepository<DiaInhabil, LocalDate> {
}