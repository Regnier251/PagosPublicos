package com.arely.pagospublicos.seguridad.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeguridadController {

    @GetMapping("/seguridad")
    public String login(Authentication authentication) {
        // Si ya inició sesión, al login no vuelve (evita ver el form estando dentro)
        if (authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "seguridad/login";
    }
}