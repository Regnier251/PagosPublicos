package com.arely.pagospublicos.seguridad.repository;

import com.arely.pagospublicos.seguridad.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);
}