package com.arely.pagospublicos.seguridad.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
//import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Pone el usuario autenticado en TODAS las vistas sin que cada
 * controller lo repita: @ControllerAdvice + @ModelAttribute corren
 * antes de cualquier handler que devuelva una vista.
 */
@ControllerAdvice
public class UsuarioEnSesionAdvice {

    @ModelAttribute("usuarioEnSesion")
    public String usuarioEnSesion(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}