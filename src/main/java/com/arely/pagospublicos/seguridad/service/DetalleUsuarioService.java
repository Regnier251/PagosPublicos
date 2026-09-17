package com.arely.pagospublicos.seguridad.service;

import com.arely.pagospublicos.seguridad.model.EstatusUsuario;
import com.arely.pagospublicos.seguridad.model.Usuario;
import com.arely.pagospublicos.seguridad.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security pregunta AQUÍ por cada intento de login.
 * Nosotros traducimos nuestra tabla `usuarios` al lenguaje de Spring:
 * el hash BCrypt guardado se valida solo (PasswordEncoder lo compara).
 */
@Service
public class DetalleUsuarioService implements UserDetailsService {

    private final UsuarioRepository repository;

    public DetalleUsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Usuario usuario = repository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasenaHash())
                .disabled(usuario.getEstatus() != EstatusUsuario.ACTIVO)
                .build();
    }

    /** Nombre completo del usuario en sesión (registra los trámites a su nombre). */
    @Transactional(readOnly = true)
    public String nombreCompletoDe(String nombreUsuario) {
        return repository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + nombreUsuario))
                .getNombreCompleto();
    }
}