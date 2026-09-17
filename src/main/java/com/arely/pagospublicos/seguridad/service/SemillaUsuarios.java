package com.arely.pagospublicos.seguridad.service;

import com.arely.pagospublicos.seguridad.model.EstatusUsuario;
import com.arely.pagospublicos.seguridad.model.Usuario;
import com.arely.pagospublicos.seguridad.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario inicial SOLO si la tabla está vacía.
 * En arranques posteriores no hace nada: el usuario vive en BD,
 * se cambia por la app, y esta semilla jamás lo pisa.
 */
@Component
public class SemillaUsuarios implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SemillaUsuarios.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String usuarioInicial;
    private final String contrasenaInicial;

    public SemillaUsuarios(UsuarioRepository repository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seguridad.usuario-inicial}") String usuarioInicial,
                           @Value("${app.seguridad.contrasena-inicial}") String contrasenaInicial) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioInicial = usuarioInicial;
        this.contrasenaInicial = contrasenaInicial;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombreUsuario(usuarioInicial);
        admin.setContrasenaHash(passwordEncoder.encode(contrasenaInicial));
        admin.setNombreCompleto("Administrador");
        admin.setEstatus(EstatusUsuario.ACTIVO);
        repository.save(admin);
        log.warn("Usuario inicial '{}' creado con contraseña temporal. "
                + "En producción defina POLIZAS_ADMIN_PASS y cámbiela tras el primer login.",
                usuarioInicial);
    }
}